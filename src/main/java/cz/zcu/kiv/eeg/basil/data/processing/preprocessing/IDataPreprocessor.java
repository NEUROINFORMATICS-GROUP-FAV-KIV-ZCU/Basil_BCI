package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;

/**
 * Created by Tomas Prokop on 31.07.2017.
 */
public interface IDataPreprocessor {

    /**
     * Performs the data processing methods
     * @return list of data packages containing processed data
     */
    List<EEGDataPackage> preprocessData();
}
