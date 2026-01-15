package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityComment {
	private int commentSeq;
	private int postSeq;
	private int userSeq;
	private String content;
	private LocalDateTime createdAt, updatedAt;
	
	// postList.html
	private String userName;
	private Boolean isOwner;
}
