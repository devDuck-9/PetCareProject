package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPost {
	private int postSeq;
	private int userSeq;
	private String title;
	private String content;
	private int viewCount;
	private LocalDateTime createdAt, updatedAt;
	private String category; // FREE, INFO, BOAST, QNA
	
	private String userName;
	private Integer commentCount;

}
