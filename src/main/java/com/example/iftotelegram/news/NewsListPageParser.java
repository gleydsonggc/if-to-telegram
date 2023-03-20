package com.example.iftotelegram.news;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NewsListPageParser {

	public static NewsListPage parsePage(int pageNumber) {
		NewsListPage newsListPage = NewsListPage.of(pageNumber);
		for (int attempt = 0; attempt < 5; attempt++) {
			log.info("Getting content from: {}", newsListPage.getUrl());
			try {
				List<String> urls = Jsoup.connect(newsListPage.getUrl().toString())
						.timeout((int) Duration.ofMinutes(5).toMillis())
						.get()
						.select(NewsListPage.CSS_SELECTOR_NEWS_LINK).stream()
						.map(link -> link.absUrl("href"))
						.collect(Collectors.toList());
				Collections.reverse(urls); // Oldest to newest
				urls.forEach(newsListPage::addNewsUrl);
				break;
			} catch (IOException e) {
				log.error("Failed to get content from {}", newsListPage.getUrl());
				e.printStackTrace();
			}
		}
		return newsListPage;
	}

}
