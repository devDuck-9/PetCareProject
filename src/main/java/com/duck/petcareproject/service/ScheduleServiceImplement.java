package com.duck.petcareproject.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.Schedule;
import com.duck.petcareproject.mapper.ScheduleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImplement implements ScheduleService {
	
	private final ScheduleMapper scheduleMapper;
	
	// 내일 일정 조회
	public List<Schedule> findTomorrowPlannedNotifyY(int userSeq, LocalDate tomorrow) {
		return scheduleMapper.selectSchedulesBetween(userSeq, tomorrow);
	}
	
	// 일정 수정
	public int updateScheduleBySeqAndUser(Schedule schedule) {
		return scheduleMapper.updateScheduleBySeqAndUser(schedule);
	}

	// 일정 삭제
	public int deleteScheduleBySeqAndUser(int scheduleSeq, int userSeq) {
		return scheduleMapper.deleteScheduleBySeqAndUser(scheduleSeq, userSeq);
	}

	// 선택한 일정 조회
	public Schedule selectScheduleByIdAndUser(int scheduleSeq, int userSeq) {
		return scheduleMapper.selectScheduleByIdAndUser(scheduleSeq, userSeq);
	}
	
	// 일정 등록
	public void insertSchedule(Schedule schedule) {
		scheduleMapper.insertSchedule(schedule);
	};
	
	
}
