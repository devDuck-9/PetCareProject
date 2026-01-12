package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pet {
	private int petSeq;
	private int userSeq;
	private String petName;
	private String petType;
	private Integer petAge;
	private String petImage;
	private LocalDateTime createdAt, updatedAt;
}
