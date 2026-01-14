package com.duck.petcareproject.service;

import com.duck.petcareproject.domain.Schedule;

public interface ScheduleService {
	
	/// 일정 수정
	public int updateScheduleBySeqAndUser(Schedule schedule);

	// 일정 삭제
	public int deleteScheduleBySeqAndUser(int scheduleSeq, int userSeq);

	// 선택한 일정 조회
	public Schedule selectScheduleByIdAndUser(int scheduleSeq, int userSeq);
	
	// 일정 등록
	public void insertSchedule(Schedule schedule);

}
