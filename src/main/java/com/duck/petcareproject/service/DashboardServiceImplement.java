package com.duck.petcareproject.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.domain.DashboardDTO;
import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.domain.Schedule;
import com.duck.petcareproject.mapper.CommunityPostMapper;
import com.duck.petcareproject.mapper.MemberMapper;
import com.duck.petcareproject.mapper.PetMapper;
import com.duck.petcareproject.mapper.ScheduleMapper;
import com.duck.petcareproject.util.PagingUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImplement implements DashboardService {
	
	private final MemberMapper memberMapper;
	private final PetMapper petMapper;
	private final ScheduleMapper scheduleMapper;
	private final CommunityPostMapper postMapper;
	
	public DashboardDTO getDashboard(String userId, Integer petPageParam, Integer schPageParam, Integer postPageParam) {
		
		DashboardDTO dto = new DashboardDTO();
		
		// 로그인 사용자 정보
		Member member = memberMapper.selectMember(userId);
		dto.setLoginMember(member);
		
		// 로그인 사용자 없으면(예외 케이스) 안전 처리
		if (member == null) {
			dto.setPets(Collections.emptyList());
			dto.setPetPage(1);
			dto.setPetTotalPage(1);
			
			dto.setSchedules(Collections.emptyList());
			dto.setSchPage(1);
			dto.setSchTotalPage(1);
			
			return dto;
		}
		
		int userSeq = member.getUserSeq();
		
		final int pageSize = 3; // 한 페이지에 보여줄 수
		// =========================
		// 펫 페이징
		// =========================
		int petPage = PagingUtils.pageOrDefault(petPageParam);
		int petTotalCount = petMapper.countPetsByUser(userSeq);
		int petTotalPage = PagingUtils.calcTotalPage(petTotalCount, pageSize);
		
		// 요청 페이지가 마지막 페이지보다 크면 마지막 페이지로 보정
		if (petTotalPage <= 0) petTotalPage = 1;
		if (petPage > petTotalPage) petPage = petTotalPage;
		int offset = (petPage - 1) * pageSize;
		
		// 펫 목록
		List<Pet> pets = petMapper.selectPetsByUserPaging(userSeq, pageSize, offset);
		dto.setPets(pets);
		dto.setPetPage(petPage);
		dto.setPetTotalPage(petTotalPage);
		
		// =========================
		// 일정 페이징
		// =========================
		int schPage = PagingUtils.pageOrDefault(schPageParam);
		int schTotalCount = scheduleMapper.countSchedulesByUser(userSeq);
		int schTotalPage = PagingUtils.calcTotalPage(schTotalCount, pageSize);
		
		if (schTotalPage <= 0) schTotalPage = 1;
		if (schPage > schTotalPage) schPage = schTotalPage;
		int schOffset = (schPage - 1) * pageSize;
		
		// 일정 목록
		List<Schedule> schedules = scheduleMapper.selectScheduleByUserPaging(userSeq, pageSize, schOffset);
		dto.setSchedules(schedules);
		dto.setSchPage(schPage);
		dto.setSchTotalPage(schTotalPage);
		
		// =========================
		// 오늘의 게시글 페이징
		// =========================
		int postPage = PagingUtils.pageOrDefault(postPageParam);
		int postTotalCount = postMapper.countTodayPostsByUser(userSeq);
		int postTotalPage = PagingUtils.calcTotalPage(postTotalCount, pageSize);

		if (postTotalPage <= 0) postTotalPage = 1;
		if (postPage > postTotalPage) postPage = postTotalPage;
		int postOffset = (postPage - 1) * pageSize;

		// 오늘의 게시글 목록
		List<CommunityPost> todayPosts = postMapper.selectTodayPostsByUserPaging(userSeq, pageSize, postOffset);
		dto.setTodayPosts(todayPosts);
		dto.setPostPage(postPage);
		dto.setPostTotalPage(postTotalPage);
		
		return dto;
	}
	
}
