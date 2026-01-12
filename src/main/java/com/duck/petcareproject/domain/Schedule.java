package com.duck.petcareproject.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schedule {
		private int scheduleSeq;
		private int petSeq;
		private int notifyEnabled;	// 1:수신, 0:미수신
		private String title;
		private LocalDateTime scheduleTime;
		private String memo;
		private String status;
		private LocalDateTime createdAt, updatedAt;
}
