package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Guide {
		private int guideSeq;
		private int userSeq;
		private String title;
		private String content;
		private int viewCount;
		private LocalDateTime createdAt, updatedAt;
}
