package com.maydls.repository;

import com.maydls.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository
extends JpaRepository<Video, Long>
{
    List<Video> findAllByOrderByCreatedAtDesc();

    List<Video> findByYoutubeUrl(String youtubeUrl);
}
