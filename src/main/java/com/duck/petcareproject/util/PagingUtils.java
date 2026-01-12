package com.duck.petcareproject.util;

public class PagingUtils {
	private PagingUtils() {} 
	
	public static int pageOrDefault(Integer page) {
		return (page == null || page < 1) ? 1 : page;
	}
	
	public static int calcTotalPage(int totalCount, int size) {
		return Math.max((totalCount + size - 1) / size, 1);
	}
	
}
