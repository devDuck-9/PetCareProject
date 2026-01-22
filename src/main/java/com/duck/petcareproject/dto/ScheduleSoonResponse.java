package com.duck.petcareproject.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScheduleSoonResponse {
	
	private LocalDate date;	 // 내일 날짜
	private int count;		// 내일 일정 개수
	private List<Item> items;	// 타이틀 목록(최대 N개)
	private boolean hiddenToday;
	
	@Getter
	@AllArgsConstructor
	public static class Item {
		private int scheduleSeq;
		private String title;
	}

}