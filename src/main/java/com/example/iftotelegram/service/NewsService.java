package com.example.iftotelegram.service;

import com.example.iftotelegram.news.News;
import com.example.iftotelegram.news.NewsListPage;
import com.example.iftotelegram.news.NewsListPageParser;
import com.example.iftotelegram.news.NewsPageParser;
import com.example.iftotelegram.news.NewsTelegramFormatter;
import com.example.iftotelegram.repository.NewsRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

	private final NewsRepository newsRepository;

	private final NewsPageParser newsPageParser;

	private final TelegramBot telegramBot;

	private final NewsTelegramFormatter newsTelegramFormatter;

	@Value("${bot.chat_id}")
	private String chatId;

	@SneakyThrows
	@EventListener(ApplicationReadyEvent.class)
	public void startup() {
		log.info("--- STARTUP ---");
		int newsQuantityMin = 1000;
		if (newsRepository.count() < newsQuantityMin) {
			saveAllNews();
		}
		sendAllUnsentNewsToTelegram();
	}

	public News save(News news) {
		Optional<News> newsWithSameUrl = newsRepository.findByUrl(news.getUrl());
		if (newsWithSameUrl.isPresent() && newsWithSameUrl.get().getHtml() != null) {
			log.info("Returning already saved news with this url: {}", news.getUrl());
			return newsWithSameUrl.get();
		}
		log.info("Saving news with this url: {}", news.getUrl());
		return newsRepository.save(news);
	}

	public News update(News news) {
		Optional<News> newsWithSameUrl = newsRepository.findByUrl(news.getUrl());
		if (newsWithSameUrl.isEmpty()) {
			log.info("Doesn't exists news with this url: {}", news.getUrl());
			return news;
		}
		news = news.withId(newsWithSameUrl.get().getId());
		log.info("Updating news with this url: {}", news.getUrl());
		return newsRepository.save(news);
	}

	private void saveNewsListPage(int pageNumber) {
		log.info("Saving page number: {}", pageNumber);
		NewsListPageParser.parsePage(pageNumber)
				.getNewsUrls()
				.stream()
				.filter(url -> newsRepository.findByUrl(url.toString()).isEmpty())
				.map(newsPageParser::toNews)
				.forEach(this::save);
	}

	private void saveAllNews() {
		for (int pageNumber = NewsListPage.MAX_PAGE_NUMBER; pageNumber >= NewsListPage.MIN_PAGE_NUMBER; pageNumber--) {
			saveNewsListPage(pageNumber);
		}
	}

	@SneakyThrows
	private void sendAllUnsentNewsToTelegram() {
		List<News> unsentNews = newsRepository.findAllBySentToTelegramIsNullOrderByPublishedDate();
		for (News news : unsentNews) {
			List<String> messages = newsTelegramFormatter.format(news);
			for (int index = 0; index < messages.size(); index++) {
				String message = messages.get(index);
				boolean isLastMessage = index == messages.size() - 1;
				if (messages.size() > 1) {
					String messageNumberInfo = String.format("%n%s(%d de %d)", isLastMessage ? "" : "... ", index + 1, messages.size());
					message += messageNumberInfo;
				}
				for (int attempt = 0; attempt < 5; attempt++) {
					log.info("Sending message: {}", message);
					SendMessage telegramMessage = new SendMessage(chatId, message)
							.parseMode(ParseMode.HTML)
							.disableWebPagePreview(index > 0)
							.disableNotification(true);
					SendResponse response = telegramBot.execute(telegramMessage);
					if (response.isOk()) {
						if (isLastMessage) {
							newsRepository.save(news.withSentToTelegram(LocalDateTime.now()));
							log.info("News updated: {}", news.getUrl());
						}
						Thread.sleep(3000); // Telegram rate limit of message to channel is 20 msg/minute
						break;
					} else {
						log.info("[Attempt {} of 5] Error while sending this message: {}{}", attempt + 1, (messages.size() > 1 ? String.format("(%s/%s) ", index + 1, messages.size()) : ""), news.getUrl());
					}
				}
			}
		}
	}

}
