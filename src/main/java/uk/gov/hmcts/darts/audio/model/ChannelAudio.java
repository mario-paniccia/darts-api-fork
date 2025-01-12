package uk.gov.hmcts.darts.audio.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelAudio {
    Integer channel;
    List<AudioFileInfo> audioFiles;

    public ChannelAudio(Integer channel) {
        this.channel = channel;
    }

    public void addAudioFile(AudioFileInfo audioFileInfo) {
        if (audioFiles == null) {
            audioFiles = new ArrayList<>();
        }
        audioFiles.add(audioFileInfo);
    }
}
