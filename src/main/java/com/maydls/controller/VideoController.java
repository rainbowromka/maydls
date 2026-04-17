package com.maydls.controller;

import com.maydls.dto.VideoInfoResponseDTO;
import com.maydls.entity.Video;
import com.maydls.repository.VideoRepository;
import com.maydls.service.YtDlpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController
{
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private YtDlpService ytDlpService;

    @GetMapping
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping(value = "/info", produces = "application/json")
    public ResponseEntity<VideoInfoResponseDTO> getVideoInfoParsed(@RequestParam String url) {
        try {
            VideoInfoResponseDTO info = ytDlpService.getVideoInfoParsed(url);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadVideo(
        @RequestParam String url,
        @RequestParam String formatId)
    {
        try {
            Video saved = ytDlpService.downloadVideoToDatabase(url, formatId);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Download failed: " + e.getMessage());
        }
    }
}
