package com.example.iftotelegram.repository;

import com.example.iftotelegram.news.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

	Optional<News> findByUrl(String url);

	List<News> findAllBySentToTelegramIsNullOrderByPublishedDate();

}
