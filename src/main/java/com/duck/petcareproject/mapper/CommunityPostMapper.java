package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.CommunityPost;

@Mapper
public interface CommunityPostMapper {

	// 게시글 등록
	int insertPost(CommunityPost post);
	
	// 전체 게시글 목록(페이징) - category 없으면 전체
	List<CommunityPost> selectPostsPaging(@Param("category") String category, @Param("limit") int limit, @Param("offset") int offset);
	
	// 전체 게시글 수
	int countPosts(@Param("category") String category);
	
	// userSeq 기준 오늘의 게시글 페이징 목록 - 대시보드용
	List<CommunityPost> selectTodayPostsByUserPaging(@Param("userSeq") int userSeq, @Param("limit") int limit, @Param("offset") int offset
	);

	// userSeq 기준 오늘의 게시글 전체 수 - 대시보드용
	int countTodayPostsByUser(@Param("userSeq") int userSeq);
}