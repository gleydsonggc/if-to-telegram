package com.example.iftotelegram.news;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Component
@Slf4j
public class NewsTelegramFormatter {

	public List<String> format(News news) {
		String msg = String.format(
				"%s%s%s%n%n<b># %s</b>%n%n%s%s%n%n---%n<b>Fonte:</b> %s%n",
				"<a href='" + news.getImg() + "'>&#8205;</a>", // Force Telegram to show this image at the end of the message
				news.getPublishedDate().toLocalDate() + ", " + news.getPublishedDate().toLocalTime(),
				news.getModifiedDate().isEqual(news.getPublishedDate())
						? ""
						: String.format(" (%s, %s)", news.getModifiedDate().toLocalDate(), news.getModifiedDate().toLocalTime()),
				news.getTitle(),
				news.getDescription().isBlank() ? "" : String.format("<i>%s</i>%n%n", news.getDescription()),
				news.getContent(),
				news.getUrl()
		);
		return splitAtNewLine(msg, 3500); // Telegram message max length is 4096 characters
	}

	public static List<String> splitAtNewLine(String input, int maxLength) {
		List<String> substrings = new ArrayList<>();
		if (input == null || input.isBlank() || maxLength <= 0) {
			return substrings;
		}
		int inputLength = input.length();
		if (inputLength <= maxLength) {
			return List.of(input);
		}
		int start = 0;
		int end = maxLength;
		while (start < inputLength) {
			String substring = input.substring(start, end);
			if (substring.contains("\n")) {
				end = 1 + input.lastIndexOf("\n", end);
			} else if (substring.contains(" ") && end < inputLength) {
				end = 1 + input.lastIndexOf(" ", end);
			}
			substring = input.substring(start, end);

			// Update end index to break at last opening tag, if their closing tag isn't contained in this same substring.
			// This is done to avoid "Unclosed start tag" error while sending some parts of a Telegram message
			List<MatchResult> tagOpenResults = Pattern.compile("<[a-zA-Z]+(>|.*?[^?]>)").matcher(substring).results().toList();
			List<MatchResult> tagCloseResults = Pattern.compile("</[^<]+>").matcher(substring).results().toList();
			while (tagOpenResults.size() != tagCloseResults.size()) {
				int indexOfLastOpeningTag = tagOpenResults.get(tagOpenResults.size() - 1).start();
				end = indexOfLastOpeningTag;
				log.info("Changed end value to: {}", end);
				substring = input.substring(start, end);
				tagOpenResults = Pattern.compile("<[a-zA-Z]+(>|.*?[^?]>)").matcher(substring).results().toList();
				tagCloseResults = Pattern.compile("</[^<]+>").matcher(substring).results().toList();
			}

			substring = input.substring(start, end);
			substrings.add(substring.strip());
			start = end;
			end = Math.min(start + maxLength, inputLength);
		}
		return substrings;
	}

}
