package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.domain.CommunityPostImage;

public interface CommunityPostService {
	
	// 내가 쓴 글 총 개수
	int countMyPosts(int userSeq);
	// 내가 쓴 글 목록
	List<CommunityPost> findMyPosts(int userSeq, int size, int offset);
	
	// 단순 조회 (삭제/수정/권한체크용)
	CommunityPost getPost(int postSeq);
	
	// 검색/정렬용 목록
	List<CommunityPost> getPostsPaging(String category, String keyword, String sort, int page, int pageSize);
	
	// 검색/정렬용 목록 수
	int countPosts(String category, String keyword);
	
	// 게시글 상세 (조회수 증가)
	CommunityPost getPostDetail(int postSeq);

	// 내 게시글 조회 (수정 폼/권한 체크)
	CommunityPost getPostByIdAndUser(int postSeq, int userSeq);

//	// 게시글 수정
//	boolean updatePost(CommunityPost post);
//
//	// 게시글 삭제
//	boolean deletePost(int postSeq, int userSeq);
//	
	
	// 게시글 + 이미지 다건 수정
	boolean updatePostWithImages(CommunityPost post, MultipartFile[] newImages, List<Integer> removeImageSeqs) throws Exception;
	
	// 게시글 + 이미지 다건 삭제
	boolean deletePostWithImages(int postSeq, int userSeq, boolean isAdmin);
	
	// 게시글 삭제 (관지자용)
//	boolean deletePostAdmin(int postSeq);
	boolean deletePostWithImagesAdmin(int postSeq);
	
//	// 게시글 등록
//	public void insertPost(CommunityPost post);
	
	// 게시글 + 이미지 다건 등록
	int insertPostWithImages(CommunityPost post, MultipartFile[] images) throws Exception;
	
	// 게시글 이미지 조회
	List<CommunityPostImage> getPostImages(int postSeq);
	
	// 전체 게시글 목록(페이징) - category 없으면 전체
	List<CommunityPost> getPostsPaging(String category, int page, int pageSize);
	
	// 전체 게시글 수
	int countPosts(String category);
	
	// 관리자 대시보드
	int countPostsAdmin(String category, String type, String keyword);
	List<CommunityPost> getPostsAdminPaging(String category, String type, String keyword, String sort, int page, int size);

}
