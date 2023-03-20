package com.example.iftotelegram.news;

public class NewsPage {

	public static final String CSS_SELECTOR_TITLE = "h1[property='rnews:name']";

	public static final String CSS_SELECTOR_TITLE_ALTERNATIVE = "h1.documentFirstHeading";

	public static final String CSS_SELECTOR_DESCRIPTION = "div[property='rnews:description']";

	public static final String CSS_SELECTOR_DESCRIPTION_ALTERNATIVE = "div.documentDescription";

	public static final String CSS_SELECTOR_DATE_PUBLISHED = "span[property='rnews:datePublished']";

	public static final String CSS_SELECTOR_DATE_MODIFIED = "span[property='rnews:dateModified']";

	public static final String CSS_SELECTOR_CONTENT = "div[property='rnews:articleBody']";

	public static final String CSS_SELECTOR_CONTENT_ALTERNATIVE = ".newsImageContainer + div";

	public static final String CSS_SELECTOR_IMAGE = "#parent-fieldname-image > img[property='rnews:thumbnailUrl']";

}
