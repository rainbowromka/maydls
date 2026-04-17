package com.maydls.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoResponseDTO {
    private String title;
    private String videoId;
    private String thumbnail;
    private String description;
    private Integer duration;
    private String uploader;
    private Long viewCount;
    private List<VideoFormatDTO> formats;
}