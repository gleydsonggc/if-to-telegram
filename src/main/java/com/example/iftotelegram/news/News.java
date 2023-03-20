package com.example.iftotelegram.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class News {

	public static final String IMG_DEFAULT = "https://www.ifpe.edu.br/campus/barreiros/noticias/atualizada-lista-de-candidatos-ao-consup/ifpe.png/@@images/image.png";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String title;

	@NotNull
	private String description = "";

	@NotNull
	@Column(columnDefinition = "mediumtext")
	private String content = "";

	@URL
	@Column(columnDefinition = "text")
	private String img = IMG_DEFAULT;

	@NotNull
	private LocalDateTime publishedDate;

	@NotNull
	private LocalDateTime modifiedDate;

	@EqualsAndHashCode.Include
	@URL
	@Column(unique = true, columnDefinition = "text")
	private String url;

	private LocalDateTime sentToTelegram;

	@NotBlank
	@Column(columnDefinition = "mediumtext")
	private String html;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

}
