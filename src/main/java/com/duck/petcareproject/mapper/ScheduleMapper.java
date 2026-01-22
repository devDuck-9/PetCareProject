package com.duck.petcareproject.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.Schedule;

@Mapper
public interface ScheduleMapper {
	
	// 내일 일정 조회
	List<Schedule> selectSchedulesBetween(@Param("userSeq") int userSeq, @Param("tomorrow") LocalDate tomorrow);
	
	// 일정 수정
	public int updateScheduleBySeqAndUser(Schedule schedule);

	// 일정 삭제
	public int deleteScheduleBySeqAndUser(@Param("scheduleSeq") int scheduleSeq, @Param("userSeq") int userSeq);

	// 선택한 일정 조회
	public Schedule selectScheduleByIdAndUser(@Param("scheduleSeq") int scheduleSeq, @Param("userSeq") int userSeq);
	
	// 일정 등록
	public int insertSchedule(Schedule schedule);
	
	// userSeq 의 일정 페이지 목록 (페이징)
	public List<Schedule> selectScheduleByUserPaging(@Param("userSeq") int userSeq, @Param("limit") int limit, @Param("offset") int offset);
	
	// userSeq 의 일정 전체수
	public int countSchedulesByUser(@Param("userSeq") int userSeq);
}
