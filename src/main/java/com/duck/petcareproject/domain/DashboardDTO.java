package com.duck.petcareproject.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardDTO {
	
	/* 사용자 */
	private Member loginMember;
	
	/* 반려동물 */
	private List<Pet> pets;
	private int petPage;
	private int petTotalPage;
	
	/* 일정 */
	private List<Schedule> schedules;
	private int schPage;
	private int schTotalPage;
	
	/* 게시판 */
	private List<CommunityPost> todayPosts;
	private int postPage;
	private int postTotalPage;

}
