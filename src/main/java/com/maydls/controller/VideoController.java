package com.maydls.controller;

import com.maydls.entity.Video;
import com.maydls.repository.VideoRepository;
import com.maydls.service.YtDlpService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/info")
    public String getVideoInfo(
        @RequestParam String url)
    throws Exception
    {
        return ytDlpService.getVideoInfo(url);
    }
}
