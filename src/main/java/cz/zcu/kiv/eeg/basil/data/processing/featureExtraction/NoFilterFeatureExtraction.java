package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

public class NoFilterFeatureExtraction implements IFeatureExtraction {

    private final int downSmplFactor;  /* subsampling factor */

    private int numOfChannels = 0;

    private int epochSize = 0;

    public NoFilterFeatureExtraction(int downSmplFactor) {
        this.downSmplFactor = downSmplFactor;
    }

    @Override
	public FeatureVector extractFeatures(EEGDataPackage data) {
		double[][] epoch = data.getData();
        numOfChannels = epoch.length;
        epochSize = epoch[0].length;
        double[] features = new double[epochSize * numOfChannels];
        int i = 0;

        
        for (double[] channel : epoch) {
            for (int j = 0; j < epochSize; j++) {
                features[i * epochSize + j] = channel[j];
            }
            i++;
        }

        // subsample the filtered data and return the feature vectors after vector normalization
        features = SignalProcessing.decimate(features, downSmplFactor);
        features = SignalProcessing.normalize(features);

        FeatureVector fv = new FeatureVector(features);
        return fv;
    }

    @Override
    public int getFeatureDimension() {
        return numOfChannels * epochSize / downSmplFactor /* subsampling */;
    }

}
