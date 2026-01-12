package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.Schedule;

@Mapper
public interface ScheduleMapper {
	
	// 일정 등록
	public int insertSchedule(Schedule schedule);
	
	// userSeq 의 일정 페이지 목록 (페이징)
	public List<Schedule> selectScheduleByUserPaging(@Param("userSeq") int userSeq, @Param("limit") int limit, @Param("offset") int offset);
	
	// userSeq 의 일정 전체수
	public int countSchedulesByUser(@Param("userSeq") int userSeq);
}
