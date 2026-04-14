package com.maydls.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class YtDlpService
{
    private static final String DOCKER_CONTAINER = "maydls-yt-dlp";
    private static final String DOWNLOADS_PATH_IN_CONTAINER = "/downloads";

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

}
