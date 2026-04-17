package com.maydls.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFormatDTO {
    private String formatId;      // e.g., "248"
    private String ext;           // e.g., "webm", "mp4"
    private String resolution;    // e.g., "1920x1080"
    private String quality;       // e.g., "1080p"
    private Long filesize;        // in bytes
    private Double fps;           // frames per second
    private String vcodec;        // video codec, "none" if audio-only
    private String acodec;        // audio codec, "none" if video-only
    private Integer audioChannels;
    private Double tbr;           // total bitrate
}