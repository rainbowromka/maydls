package com.maydls.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maydls.dto.VideoFormatDTO;
import com.maydls.dto.VideoInfoResponseDTO;
import com.maydls.entity.Video;
import com.maydls.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class YtDlpService
{
    private static final String DOCKER_CONTAINER = "maydls-yt-dlp";
    private static final String DOWNLOADS_PATH_IN_CONTAINER = "/downloads";

    private final VideoRepository videoRepository;

    public YtDlpService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public String getVideoInfo(String youtubeUrl) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", DOCKER_CONTAINER,
            "yt-dlp", "-j", youtubeUrl
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line=reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("yt-dlp failed with exit code: "
                + exitCode + ", output: " + output);
        }

        return output.toString();
    }

    public VideoInfoResponseDTO getVideoInfoParsed(String youtubeUrl) throws Exception {
        String jsonOutput = getVideoInfo(youtubeUrl);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonOutput);

        // Extract basic info
        String title = root.path("title").asText();
        String videoId = root.path("id").asText();
        String thumbnail = root.path("thumbnail").asText();
        String description = root.path("description").asText();
        Integer duration = root.path("duration").asInt();
        String uploader = root.path("uploader").asText();
        Long viewCount = root.path("view_count").asLong();

        // Parse formats
        List<VideoFormatDTO> formats = new ArrayList<>();
        JsonNode formatsNode = root.path("formats");

        for (JsonNode format : formatsNode) {
            String vcodec = format.path("vcodec").asText();
            String acodec = format.path("acodec").asText();

            // Skip formats that are not downloadable (both codecs "none")
            if ("none".equals(vcodec) && "none".equals(acodec)) {
                continue;
            }

            VideoFormatDTO dto = VideoFormatDTO.builder()
                .formatId(format.path("format_id").asText())
                .ext(format.path("ext").asText())
                .resolution(format.path("resolution").asText())
                .quality(format.path("quality").asText())
                .filesize(format.path("filesize").asLong())
                .fps(format.path("fps").isDouble() ? format.path("fps").asDouble() : null)
                .vcodec(vcodec)
                .acodec(acodec)
                .audioChannels(format.path("audio_channels").isInt() ? format.path("audio_channels").asInt() : null)
                .tbr(format.path("tbr").isDouble() ? format.path("tbr").asDouble() : null)
                .build();

            formats.add(dto);
        }

        return VideoInfoResponseDTO.builder()
            .title(title)
            .videoId(videoId)
            .thumbnail(thumbnail)
            .description(description)
            .duration(duration)
            .uploader(uploader)
            .viewCount(viewCount)
            .formats(formats)
            .build();
    }

    public String downloadVideo(
        String youtubeUrl,
        String formatCode,
        String outputFileName)
    throws Exception
    {
        String outputPath = DOWNLOADS_PATH_IN_CONTAINER + "/" + outputFileName;

        ProcessBuilder pb = new ProcessBuilder(
          "docker", "exec", DOCKER_CONTAINER,
          "yt-dlp", "-f", formatCode, "-o", outputPath, youtubeUrl
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader((
            new InputStreamReader(process.getInputStream())));

        String line;
        while ((line=reader.readLine()) != null) {
            System.out.println("[yt-dlp]" + line);
        }

        int exitCode = process.waitFor();
        if (exitCode !=0) {
            throw new RuntimeException("yt-dlp download failed with exit code: "
                + exitCode);
        }

        String hostDownloadsPath = new java.io.File("downloads").getAbsolutePath();
        return hostDownloadsPath + "/" + outputFileName;
    }

    public Video downloadVideoToDatabase(String youtubeUrl, String formatId) throws Exception {
        // First, get video info to extract title
        VideoInfoResponseDTO info = getVideoInfoParsed(youtubeUrl);

        // Find the selected format
        VideoFormatDTO selectedFormat = info.getFormats().stream()
            .filter(f -> f.getFormatId().equals(formatId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Format not found: " + formatId));

        // Generate safe filename
        String safeTitle = info.getTitle()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", "_")
            .toLowerCase();
        String filename = safeTitle + "_" + formatId + "." + selectedFormat.getExt();

        // Download using yt-dlp
        String hostPath = downloadVideo(youtubeUrl, formatId, filename);

        // Create and save Video entity
        Video video = Video.builder()
            .youtubeUrl(youtubeUrl)
            .title(info.getTitle())
            .filePath(hostPath)
            .fileSizeBytes(selectedFormat.getFilesize())
            .durationSeconds(info.getDuration())
            .quality(selectedFormat.getResolution())
            .isAudioOnly("none".equals(selectedFormat.getVcodec()))
            .createdAt(LocalDateTime.now())
            .build();

        return videoRepository.save(video);
    }
}
