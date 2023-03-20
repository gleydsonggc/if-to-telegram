package com.example.iftotelegram.news;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewsPageParser {

	private final NewsContentTelegramConverter newsContentTelegramConverter;

	@SneakyThrows
	public News toNews(URL url) {
		log.info("Getting: {}", url.toString());
		Document document = Jsoup.connect(url.toString())
				.timeout((int) Duration.ofMinutes(5).toMillis())
				.get();
		unwrapMailToLinks(document); // Telegram already renders plain text e-mails as links
		setAllAnchorToAbsoluteUrl(document);
		setAllImgToAbsoluteUrl(document);
		return new News.NewsBuilder()
				.title(getTitle(document))
				.description(getDescription(document))
				.img(getImg(document))
				.publishedDate(getPublishedDate(document))
				.modifiedDate(getModifiedDate(document))
				.url(url.toString())
				.content(getContent(document))
				.html(getHtml(document))
				.build();
	}

	private void unwrapMailToLinks(Document document) {
		document.select("a").stream()
				.filter(e -> e.attr("href").contains("mailto:"))
				.forEach(Node::unwrap);
	}

	private void setAllAnchorToAbsoluteUrl(Document document) {
		document.select("a").forEach(e -> e.attr("href", e.absUrl("href")));
	}

	private void setAllImgToAbsoluteUrl(Document document) {
		document.select("img").forEach(e -> e.attr("src", e.absUrl("src")));
	}

	private String getImg(Document document) {
		return Optional.ofNullable(document.selectFirst(NewsPage.CSS_SELECTOR_IMAGE))
				.orElse(new Element("img").attr("src", News.IMG_DEFAULT))
				.absUrl("src");
	}

	private String getDescription(Document document) {
		return Optional.ofNullable(document.selectFirst(NewsPage.CSS_SELECTOR_DESCRIPTION))
				.orElse(new Element("div"))
				.text();
	}

	private String getTitle(Document document) {
		Element titleElement = document.selectFirst(NewsPage.CSS_SELECTOR_TITLE);
		if (titleElement == null) {
			titleElement = document.selectFirst(NewsPage.CSS_SELECTOR_TITLE_ALTERNATIVE);
		}
		return titleElement != null ? titleElement.text() : "";
	}

	private LocalDateTime getDateTime(String dateTime) {
		LocalDate date = LocalDate.parse(dateTime.split(" ")[0], DateTimeFormatter.ofPattern("dd/MM/yyy"));
		LocalTime time = LocalTime.parse(dateTime.split(" ")[1].replace("h", ":"), DateTimeFormatter.ofPattern("HH:mm"));
		return LocalDateTime.of(date, time);
	}

	private LocalDateTime getPublishedDate(Document document) {
		return getDateTime(document.selectFirst(NewsPage.CSS_SELECTOR_DATE_PUBLISHED).text());
	}

	private LocalDateTime getModifiedDate(Document document) {
		return getDateTime(document.selectFirst(NewsPage.CSS_SELECTOR_DATE_MODIFIED).text());
	}

	private String getContent(Document document) {
		String html = Optional.ofNullable(document.selectFirst(NewsPage.CSS_SELECTOR_CONTENT))
				.orElse(new Element("div"))
				.html().replace("&nbsp;", " ");
		return newsContentTelegramConverter.convert(html);
	}

	private String getHtml(Document document) {
		boolean isPretty = document.outputSettings().prettyPrint();
		document.outputSettings().prettyPrint(true);
		String htmlPretty = document.html();
		document.outputSettings().prettyPrint(isPretty);
		return htmlPretty;
	}

}
