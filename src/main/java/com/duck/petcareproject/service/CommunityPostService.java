package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.CommunityPost;

public interface CommunityPostService {
	
	// 게시글 등록
	public void insertPost(CommunityPost post);
	
	// 전체 게시글 목록(페이징) - category 없으면 전체
	List<CommunityPost> getPostsPaging(String category, int page, int pageSize);
	
	// 전체 게시글 수
	int countPosts(String category);

}
