package uk.gov.hmcts.darts.audio.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.darts.audio.component.SystemCommandExecutor;
import uk.gov.hmcts.darts.audio.config.AudioConfigurationProperties;
import uk.gov.hmcts.darts.audio.model.AudioFileInfo;
import uk.gov.hmcts.darts.audio.service.AudioOperationService;
import uk.gov.hmcts.darts.audio.util.AudioConstants;
import uk.gov.hmcts.darts.audio.util.AudioConstants.AudioOperationTypes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioOperationServiceImpl implements AudioOperationService {

    private final AudioConfigurationProperties audioConfigurationProperties;
    private final SystemCommandExecutor systemCommandExecutor;

    CommandLine generateConcatenateCommand(final List<AudioFileInfo> audioFileInfos,
                                           final Path outputPath) {
        StringBuilder command = new StringBuilder(audioConfigurationProperties.getFfmpegExecutable());

        for (final AudioFileInfo audioFileInfo : audioFileInfos) {
            command.append(" -i ").append(audioFileInfo.getFileName());
        }

        command.append(" -filter_complex ");

        int concatNumberOfSegments = audioFileInfos.size();
        StringBuilder inputFileAudioStreams = new StringBuilder();
        for (int i = 0; i < concatNumberOfSegments; i++) {
            inputFileAudioStreams.append('[').append(i).append(":a]");
        }

        command.append(String.format("\"%sconcat=n=%d:v=0:a=1\"", inputFileAudioStreams, concatNumberOfSegments))
            .append(' ').append(outputPath.toString());

        return CommandLine.parse(command.toString());
    }

    @Override
    public AudioFileInfo concatenate(final String workspaceDir, final List<AudioFileInfo> audioFileInfos)
        throws ExecutionException, InterruptedException, IOException {

        Path basePath = Path.of(audioConfigurationProperties.getConcatWorkspace(), workspaceDir);

        Integer channel = getFirstChannel(audioFileInfos);

        Path outputPath = generateOutputPath(basePath,
                                             AudioOperationTypes.CONCATENATE,
                                             channel,
                                             AudioConstants.AudioFileFormats.MP2
        );

        CommandLine command = generateConcatenateCommand(audioFileInfos, outputPath);
        systemCommandExecutor.execute(command);

        return new AudioFileInfo(
            getEarliestStartTime(audioFileInfos),
            getLatestEndTime(audioFileInfos),
            outputPath.toString(),
            channel
        );
    }

    @Override
    public AudioFileInfo merge(final List<AudioFileInfo> audioFilesInfo, String workspaceDir)
        throws ExecutionException, InterruptedException, IOException {

        Path basePath = Path.of(audioConfigurationProperties.getMergeWorkspace(), workspaceDir);

        Path outputPath = generateOutputPath(basePath,
                                             AudioOperationTypes.MERGE,
                                             0,
                                             AudioConstants.AudioFileFormats.MP2
        );

        Integer numberOfChannels = audioFilesInfo.size();

        CommandLine command = new CommandLine(audioConfigurationProperties.getFfmpegExecutable());
        for (AudioFileInfo audioFileInfo : audioFilesInfo) {
            command.addArgument("-i").addArgument(audioFileInfo.getFileName());
        }
        command.addArgument("-filter_complex")
            .addArgument(String.format("amix=inputs=%d:duration=longest", numberOfChannels))
            .addArgument(outputPath.toString());

        systemCommandExecutor.execute(command);

        return new AudioFileInfo(
            getEarliestStartTime(audioFilesInfo),
            getLatestEndTime(audioFilesInfo),
            outputPath.toString(),
            0
        );
    }

    @Override
    public AudioFileInfo trim(String workspaceDir, AudioFileInfo audioFileInfo, Duration startDuration, Duration endDuration)
        throws ExecutionException, InterruptedException, IOException {

        Path basePath = Path.of(audioConfigurationProperties.getTrimWorkspace(), workspaceDir);

        Path outputPath = generateOutputPath(basePath,
                                             AudioOperationTypes.TRIM,
                                             audioFileInfo.getChannel(),
                                             AudioConstants.AudioFileFormats.MP2
        );

        CommandLine command = new CommandLine(audioConfigurationProperties.getFfmpegExecutable());
        command.addArgument("-i").addArgument(audioFileInfo.getFileName());
        command.addArgument("-ss").addArgument(toTimeString(startDuration));
        command.addArgument("-to").addArgument(toTimeString(endDuration));
        command.addArgument("-c").addArgument("copy").addArgument(outputPath.toString());

        systemCommandExecutor.execute(command);

        return new AudioFileInfo(
            adjustTimeDuration(audioFileInfo.getStartTime(), startDuration),
            adjustTimeDuration(audioFileInfo.getStartTime(), endDuration),
            outputPath.toString(),
            audioFileInfo.getChannel()
        );
    }

    private String toTimeString(Duration duration) {
        // Format per http://ffmpeg.org/ffmpeg-utils.html#Time-duration
        return String.format("%s%02d:%02d:%02d",
                             duration.isNegative() ? "-" : StringUtils.EMPTY,
                             Math.abs(duration.toHours()),
                             Math.abs(duration.toMinutesPart()),
                             Math.abs(duration.toSecondsPart()));
    }

    @Override
    public AudioFileInfo reEncode(String workspaceDir, AudioFileInfo audioFileInfo)
        throws ExecutionException, InterruptedException, IOException {

        Path basePath = Path.of(audioConfigurationProperties.getReEncodeWorkspace(), workspaceDir);

        Path outputPath = generateOutputPath(basePath,
                                             AudioOperationTypes.ENCODE,
                                             audioFileInfo.getChannel(),
                                             AudioConstants.AudioFileFormats.MP3
        );

        CommandLine command = new CommandLine(audioConfigurationProperties.getFfmpegExecutable());
        command.addArgument("-i").addArgument(audioFileInfo.getFileName());
        command.addArgument(outputPath.toString());

        systemCommandExecutor.execute(command);

        return new AudioFileInfo(
            audioFileInfo.getStartTime(),
            audioFileInfo.getEndTime(),
            outputPath.toString(),
            audioFileInfo.getChannel()
        );
    }

    Instant adjustTimeDuration(Instant time, Duration timeDuration) {
        return time.plus(timeDuration);
    }

    private Instant getEarliestStartTime(final List<AudioFileInfo> audioFilesInfo) {
        var earliestStartDate = audioFilesInfo.stream()
            .map(AudioFileInfo::getStartTime)
            .min(Instant::compareTo);

        return earliestStartDate.orElseThrow();
    }

    private Instant getLatestEndTime(final List<AudioFileInfo> audioFilesInfo) {
        var latestEndTime = audioFilesInfo.stream()
            .map(AudioFileInfo::getEndTime)
            .max(Instant::compareTo);

        return latestEndTime.orElseThrow();
    }

    private Integer getFirstChannel(final List<AudioFileInfo> audioFilesInfo) {
        var firstChannelNumber = audioFilesInfo.stream()
            .map(AudioFileInfo::getChannel)
            .findFirst();

        return firstChannelNumber.orElseThrow();
    }

    private String generateOutputFilename(AudioConstants.AudioOperationTypes operationType,
                                          Integer channel,
                                          AudioConstants.AudioFileFormats outputFileFormat) {
        String currentTimeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH).format(new Date());
        return String.format(
            "C%s-%s-%s.%s",
            channel,
            operationType.name().toLowerCase(Locale.ENGLISH),
            currentTimeStamp,
            outputFileFormat.name().toLowerCase(Locale.ENGLISH)
        );
    }

    private Path generateOutputPath(Path basePath,
                                    AudioConstants.AudioOperationTypes operationType,
                                    Integer channel,
                                    AudioConstants.AudioFileFormats outputFileFormat) throws IOException {
        Files.createDirectories(basePath);

        String filename = generateOutputFilename(operationType, channel, outputFileFormat);

        return basePath.resolve(filename);
    }

}
