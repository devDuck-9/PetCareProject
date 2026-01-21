package com.duck.petcareproject.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPostDto {
	
	private int postSeq;
	private String title;
	private String userName;
	private String categoryName;
	private int commentCount;
	private String profileImg;

}
