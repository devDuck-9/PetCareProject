package com.duck.petcareproject.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schedule {
		private int scheduleSeq;
		private int petSeq;
		private NotifyYn notifyEnabled;	// 1:동의, 0:미동의
		private String title;
		private LocalDateTime scheduleTime;
		private String memo;
		private ScheduleStatus status;
		private LocalDateTime createdAt, updatedAt;
		
		private int userSeq;
		LocalDate tomorrow;
}
