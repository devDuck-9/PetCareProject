package com.duck.petcareproject.service;

import com.duck.petcareproject.domain.DashboardDTO;

public interface DashboardService {
	
	// 사용자 펫 목록
	public DashboardDTO getDashboard(String userId, Integer petPageParam, Integer schPageParam, Integer postPageParam);
	
}
