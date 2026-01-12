package com.duck.petcareproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.Schedule;
import com.duck.petcareproject.mapper.ScheduleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImplement implements ScheduleService {
	
	private final ScheduleMapper scheduleMapper;
	
	// 일정 등록
	public void insertSchedule(Schedule schedule) {
		scheduleMapper.insertSchedule(schedule);
	};
	
	
}
