package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.CommunityPost;

@Mapper
public interface CommunityPostMapper {
	
	// 관리자 삭제(작성자 조건 없이)
	int deletePostById(@Param("postSeq") int postSeq);
	
	// 게시글 상세 조회 (작성자 이름 포함)
	CommunityPost selectPostById(@Param("postSeq") int postSeq);
	
	// 조회수 증가
	int incrementViewCount(@Param("postSeq") int postSeq);

	// 내 게시글 조회 (수정/삭제 권한 체크용)
	CommunityPost selectPostByIdAndUser(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	// 게시글 수정 (내 글만)
	int updatePostByIdAndUser(CommunityPost post);
	
	// 게시글 삭제 (내 글만)
	int deletePostByIdAndUser(@Param("postSeq") int postSeq, @Param("userSeq") int userSeq);
	
	// 게시글 등록
	int insertPost(CommunityPost post);
	
	// 게시글 대표이미지 등록
	int updatePostThumbnail(@Param("postSeq") int postSeq,  @Param("postImage") String postImage);
	
	// 전체 게시글 목록(페이징) - category 없으면 전체
	List<CommunityPost> selectPostsPaging(@Param("category") String category, @Param("limit") int limit, @Param("offset") int offset);
	
	// 전체 게시글 수
	int countPosts(@Param("category") String category);
	
	// 검색/정렬 포함 게시글 목록(페이징)
	List<CommunityPost> selectPostsPagingSearch(@Param("category") String category,
												@Param("keyword") String keyword,
												@Param("sort") String sort,
												@Param("limit") int limit,
												@Param("offset") int offset);

	// 검색 포함 게시글 수
	int countPostsSearch(@Param("category") String category, @Param("keyword") String keyword);
	
	// userSeq 기준 오늘의 게시글 페이징 목록 - 대시보드용
	List<CommunityPost> selectTodayPostsByUserPaging(@Param("userSeq") int userSeq, @Param("limit") int limit, @Param("offset") int offset
	);

	// userSeq 기준 오늘의 게시글 전체 수 - 대시보드용
	int countTodayPostsByUser(@Param("userSeq") int userSeq);
	
	// 관리자 대시보드
	List<CommunityPost> selectPostsAdminPaging(@Param("category") String category,
												@Param("type") String type,
												@Param("keyword") String keyword,
												@Param("sort") String sort,
												@Param("limit") int limit,
												@Param("offset") int offset);

	int countPostsAdmin(@Param("category") String category, @Param("type") String type, @Param("keyword") String keyword);
	
}