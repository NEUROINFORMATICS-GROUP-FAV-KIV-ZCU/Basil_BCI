package cz.zcu.kiv.eeg.basil.data.providers.xdf;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Created by Tomas Prokop on 25.02.2019.
 */
public class ChannelsInfo {
    private String type;

    @JacksonXmlProperty(localName = "channel")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Channel> channels;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
}
