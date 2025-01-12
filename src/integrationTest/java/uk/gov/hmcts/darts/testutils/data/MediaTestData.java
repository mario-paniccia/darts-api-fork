package uk.gov.hmcts.darts.testutils.data;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.darts.common.entity.CourtroomEntity;
import uk.gov.hmcts.darts.common.entity.MediaEntity;

import java.time.OffsetDateTime;

import static uk.gov.hmcts.darts.common.entity.MediaEntity.MEDIA_TYPE_DEFAULT;

@UtilityClass
@SuppressWarnings({"HideUtilityClassConstructor"})
public class MediaTestData {

    public static MediaEntity someMinimalMedia() {
        return new MediaEntity();
    }

    public static MediaEntity createMediaFor(CourtroomEntity courtroomEntity) {
        MediaEntity media = new MediaEntity();
        media.setChannel(1);
        media.setTotalChannels(2);
        media.setStart(OffsetDateTime.now());
        media.setEnd(OffsetDateTime.now());
        media.setCourtroom(courtroomEntity);
        media.setMediaFile("a-media-file");
        media.setChecksum("a-checksum");
        media.setFileSize(1000L);
        media.setMediaFormat("mp3");
        media.setMediaType(MEDIA_TYPE_DEFAULT);
        return media;
    }

    public static MediaEntity createMediaWith(OffsetDateTime startTime, OffsetDateTime endTime, int channel) {
        var mediaEntity = someMinimalMedia();
        mediaEntity.setStart(startTime);
        mediaEntity.setEnd(endTime);
        mediaEntity.setChannel(channel);
        mediaEntity.setTotalChannels(2);
        mediaEntity.setMediaFile("a-media-file");
        mediaEntity.setChecksum("a-checksum");
        mediaEntity.setFileSize(1000L);
        mediaEntity.setMediaFormat("mp3");
        mediaEntity.setMediaType(MEDIA_TYPE_DEFAULT);
        return mediaEntity;
    }

    public static MediaEntity createMediaWith(CourtroomEntity courtroomEntity, OffsetDateTime startTime, OffsetDateTime endTime, int channel) {
        var mediaEntity = someMinimalMedia();
        mediaEntity.setCourtroom(courtroomEntity);
        mediaEntity.setStart(startTime);
        mediaEntity.setEnd(endTime);
        mediaEntity.setChannel(channel);
        mediaEntity.setTotalChannels(2);
        mediaEntity.setMediaFormat("mp3");
        mediaEntity.setMediaFile("a-media-file");
        mediaEntity.setFileSize(1000L);
        mediaEntity.setChecksum("a-checksum");
        mediaEntity.setMediaType(MEDIA_TYPE_DEFAULT);
        return mediaEntity;
    }
}
