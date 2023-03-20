package com.example.iftotelegram.news;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Component;

@Component
public class NewsContentTelegramConverter {

	public String convert(String html) {
		Document doc = Jsoup.parse(html);
		NewsVisitor newsVisitor = new NewsVisitor();
		doc.traverse(newsVisitor);
		return newsVisitor.getNewsContentConverted();
	}

	private static class NewsVisitor implements NodeVisitor {

		private final StringBuilder sb = new StringBuilder();

		private int listDepth = 0;

		public String getNewsContentConverted() {
			return sb.toString()
					.replaceAll(" +\\n", "\n") // Remove trailing space
					.replaceAll("\\n +", "\n") // Remove leading space
					.replaceAll("(\\n( +)?){3,}+", "\n\n") // Replace 3+ line breaks with 2
					.replaceAll("(?m)^( *)(- +)", "$1- ") // Remove extra leading spaces of list items
					.replaceAll("(?m)^(<[^>]+>) +", "$1") // Remove leading space inside of first element of the line
					.replaceAll("(?m) +(</[^<]+>)$", "$1") // Remove trailing space inside of last element of the line
					.strip();
		}

		@Override
		public void head(@NotNull Node node, int depth) {
			if (node instanceof Element element) {
				String tagName = element.tagName().toLowerCase();
				switch (tagName) {
					case "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b":
						if (element.hasText()) {
							if (tagName.matches("h[1-6]") && !sb.isEmpty()) {
								sb.append("\n\n");
							}
							sb.append("<b>");
						}
						break;
					case "br", "div":
						sb.append("\n");
						break;
					case "p":
						if (sb.isEmpty()) {
							break;
						}
						sb.append("\n\n");
						break;
					case "a":
						String href = element.attr("href");
						if (!href.contains("mailto:")) { // Telegram already renders plain text e-mails as links
							sb.append("<a href='").append(href).append("'>");
						}
						break;
					case "ul", "ol":
						if (listDepth == 0) {
							sb.append("\n\n");
						}
						listDepth++;
						break;
					case "li":
						if (listDepth > 0) {
							sb.append("\n").append("  ".repeat(listDepth - 1)).append("- ");
						}
						break;
					case "code":
						sb.append("<code>");
						break;
					case "pre":
						sb.append("<pre>");
						break;
					case "em", "i":
						sb.append("<i>");
						break;
					case "u", "ins":
						sb.append("<u>");
						break;
					case "strike", "s", "del":
						sb.append("<s>");
						break;
					case "img":
						String alt = element.attr("alt");
						String src = element.attr("src");
						sb.append("<a href='").append(src).append("'>")
								.append("[imagem: ").append(alt).append("]</a>");
						break;
				}
			} else if (node instanceof TextNode textNode) {
				sb.append(textNode.text());
			}
		}

		@Override
		public void tail(@NotNull Node node, int depth) {
			if (node instanceof Element element) {
				String tagName = element.tagName().toLowerCase();
				switch (tagName) {
					case "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b":
						if (element.hasText()) {
							sb.append("</b>");
						}
						break;
					case "div":
						sb.append("\n");
						break;
					case "a":
						String href = element.attr("href");
						if (!href.contains("mailto:")) { // Telegram already renders plain text e-mails as links
							sb.append("</a>");
						}
						break;
					case "ul", "ol":
						listDepth--;
						break;
					case "code":
						sb.append("</code>");
						break;
					case "pre":
						sb.append("</pre>");
						break;
					case "em", "i":
						sb.append("</i>");
						break;
					case "u", "ins":
						sb.append("</u>");
						break;
					case "strike", "s", "del":
						sb.append("</s>");
						break;
				}
			}
		}

	}

}
