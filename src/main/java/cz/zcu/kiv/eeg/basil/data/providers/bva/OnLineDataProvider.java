package cz.zcu.kiv.eeg.basil.data.providers.bva;

import org.apache.log4j.Logger;

import cz.zcu.kiv.eeg.basil.data.listeners.DataProviderListener;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_Marker;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageData;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageStart;
import cz.zcu.kiv.eeg.basil.data.providers.bva.RDA.RDA_MessageStop;
import cz.zcu.kiv.eeg.basil.data.providers.bva.online.DataTokenizer;
import cz.zcu.kiv.eeg.basil.data.providers.bva.online.TCPIPClient;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.*;

public class OnLineDataProvider extends AbstractDataProvider {

    private final Logger logger = Logger.getLogger(OnLineDataProvider.class);
    private final TCPIPClient client;
    private final DataTokenizer dtk;

    private int channelCnt = 0;
    private boolean isRunning;

    public OnLineDataProvider(String ipAddress, int port) throws Exception {
        super();
        client = new TCPIPClient(ipAddress, port);
        client.start();
        dtk = new DataTokenizer(client);
        dtk.start();
        isRunning = true;
    }

    @Override
    public void run() {
        synchronized (super.dataProviderListeners) {
            for (DataProviderListener ls : super.dataProviderListeners) {
                ls.dataReadStart();
            }
        }

        boolean stopped = false;
        int count = 0;

        try {
            while (isRunning) {
                Object o = dtk.retrieveDataBlock();
                if (o instanceof RDA_MessageData) {
                    RDA_MessageData rda = (RDA_MessageData) o;
                    double[][] data = new double[channelCnt][(int) rda.getnPoints()];
                    EEGMarker[] markers = new EEGMarker[(int) rda.getnMarkers()];

                    float[] frdaDta = rda.getfData();
                    double[] rdaDta = new double[frdaDta.length];
                    for (int i = 0; i < frdaDta.length; i++) {
                        rdaDta[i] = frdaDta[i];
                    }
                    int pts = (int) rda.getnPoints();
                    for (int i = 0; i < channelCnt; i++) {
                        System.arraycopy(rdaDta, i * pts, data[i], 0, pts);
                    }

                    int i = 0;
                    if (rda.getMarkers() != null) {
                        for (RDA_Marker m : rda.getMarkers()) {
                            markers[i] = new EEGMarker(m.getsTypeDesc(), (int) m.getnPosition());
                            i++;
                        }
                    }

                    EEGDataMessage msg = new EEGDataMessage(count, markers, data);
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.dataMessageSent(msg);
                    }
                } else if (o instanceof RDA_MessageStart) {
                    RDA_MessageStart rda = (RDA_MessageStart) o;
                    String[] chNames = rda.getsChannelNames();
                    channelCnt = chNames.length;

                    EEGStartMessage msg = new EEGStartMessage(count, chNames, rda.getdResolutions(), rda.getdSamplingInterval());
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.startMessageSent(msg);
                    }
                } else if (o instanceof RDA_MessageStop) {
                    EEGStopMessage msg = new EEGStopMessage(count);
                    for (EEGMessageListener ls : super.eegMessageListeners) {
                        ls.stopMessageSent(msg);
                    }

                    client.requestStop();
                    dtk.requestStop();
                    isRunning = false;
                    stopped = true;
                }
            }

            if (!stopped) {
                client.requestStop();
                dtk.requestStop();
            }
        } catch (Exception e) {
            synchronized (super.dataProviderListeners) {
                for (DataProviderListener ls : super.dataProviderListeners) {
                    ls.dataReadError(e);
                }
            }
        }

        synchronized (super.dataProviderListeners) {
            for (DataProviderListener ls : super.dataProviderListeners) {
                ls.dataReadEnd();
            }
        }

        logger.info("Experiment has ended.");
    }

    @Override
    public synchronized void stop() {
        isRunning = false;
    }
}
