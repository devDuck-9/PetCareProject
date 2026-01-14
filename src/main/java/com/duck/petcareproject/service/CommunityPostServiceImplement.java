package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.mapper.CommunityPostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImplement implements CommunityPostService {
	
	private final CommunityPostMapper communityPostMapper;
	
	// 검색/정렬 포함 게시글 목록(페이징)
	public List<CommunityPost> getPostsPaging(String category, String keyword, String sort, int page, int pageSize) {
		if (page <= 0) page = 1;
		if (pageSize <= 0) pageSize = 5;

		int offset = (page - 1) * pageSize;

		// null 안전 처리
		if (keyword == null) keyword = "";
		if (sort == null || sort.isBlank()) sort = "latest"; // latest | old

		return communityPostMapper.selectPostsPagingSearch(category, keyword, sort, pageSize, offset);
	}
	
	// 검색 포함 게시글 수
	public int countPosts(String category, String keyword) {
		if (keyword == null) keyword = "";
		return communityPostMapper.countPostsSearch(category, keyword);
	}
	
	// 내 게시글 조회 (수정 폼/권한 체크)
	public CommunityPost getPost(int postSeq) {
		return communityPostMapper.selectPostById(postSeq);
	}
	
	// 게시글 상세 (조회수 증가)
	public CommunityPost getPostDetail(int postSeq) {
		communityPostMapper.incrementViewCount(postSeq);
		return communityPostMapper.selectPostById(postSeq);
	}
	
	// 내 게시글 조회 (수정 폼/권한 체크)
	public CommunityPost getPostByIdAndUser(int postSeq, int userSeq) {
		return communityPostMapper.selectPostByIdAndUser(postSeq, userSeq);
	}
	
	// 게시글 수정
	public boolean updatePost(CommunityPost post) {
		return communityPostMapper.updatePostByIdAndUser(post) > 0;
	}
	
	// 게시글 삭제
	public boolean deletePost(int postSeq, int userSeq) {
		return communityPostMapper.deletePostByIdAndUser(postSeq, userSeq) > 0;
	}
	
	// 게시글 등록
	public void insertPost(CommunityPost post) {
		communityPostMapper.insertPost(post);
	}
	
	// 전체 게시글 목록(페이징) - category 없으면 전체
	public List<CommunityPost> getPostsPaging(String category, int page, int pageSize) {
		if (page <= 0) page = 1;
		if (pageSize <= 0) pageSize = 5;

		int offset = (page - 1) * pageSize;
		return communityPostMapper.selectPostsPaging(category, pageSize, offset);
	}
	
	// 전체 게시글 수
	public int countPosts(String category) {
		return communityPostMapper.countPosts(category);
	}

}
