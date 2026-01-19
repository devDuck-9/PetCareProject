package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostImage {
	private Long imageSeq;
	private int postSeq;
	private String imagePath;
	private Integer sortOrder;
	private LocalDateTime createdAt;
}
