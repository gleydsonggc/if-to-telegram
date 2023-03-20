CREATE TABLE `news` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `content` mediumtext NOT NULL,
    `created_at` datetime NOT NULL,
    `description` varchar(255) NOT NULL,
    `html` mediumtext,
    `img` text,
    `modified_date` datetime NOT NULL,
    `published_date` datetime NOT NULL,
    `sent_to_telegram` datetime DEFAULT NULL,
    `title` varchar(255) DEFAULT NULL,
    `updated_at` datetime NOT NULL,
    `url` text,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;