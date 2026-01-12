package com.duck.petcareproject.service;

import org.springframework.stereotype.Service;

@Service
public class CommonUtils {
	
	public static int pageOrDefault(Integer page) {
		return (page == null || page < 1) ? 1 : page;
	}
	
	public static int calcTotalPage(int totalCount, int size) {
		return Math.max((totalCount + size - 1) / size, 1);
	}
	
}
