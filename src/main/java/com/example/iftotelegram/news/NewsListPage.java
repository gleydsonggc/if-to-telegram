package com.example.iftotelegram.news;

import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class NewsListPage {

	public static final int MIN_PAGE_NUMBER = 1;

	public static final int MAX_PAGE_NUMBER = 50;

	public static final String CSS_SELECTOR_NEWS_LINK = ".tileContent > a";

	private final URL url;

	private final List<URL> newsUrls;

	public static NewsListPage of(int pageNumber) {
		return new NewsListPage(pageNumber);
	}

	public List<URL> getNewsUrls() {
		return Collections.unmodifiableList(newsUrls);
	}

	@SneakyThrows
	public boolean addNewsUrl(String newsUrl) {
		return newsUrls.add(new URL(newsUrl));
	}

	@SneakyThrows
	private NewsListPage(int pageNumber) {
		if (pageNumber < MIN_PAGE_NUMBER || pageNumber > MAX_PAGE_NUMBER) {
			throw new IllegalArgumentException(String.format("Page number must be between %d and %d.", MIN_PAGE_NUMBER, MAX_PAGE_NUMBER));
		}
		url = new URL("https://www.ifpe.edu.br/campus/recife/noticias/todas-as-noticias?b_start:int=" + ((pageNumber - 1) * 20));
		newsUrls = new ArrayList<>();
	}

}
