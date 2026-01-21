package com.duck.petcareproject.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.duck.petcareproject.domain.CommunityPost;
import com.duck.petcareproject.domain.CommunityPostImage;
import com.duck.petcareproject.mapper.CommunityPostImageMapper;
import com.duck.petcareproject.mapper.CommunityPostMapper;
import com.duck.petcareproject.service.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImplement implements CommunityPostService {
	
	private final CommunityPostMapper communityPostMapper;
	private final CommunityPostImageMapper communityPostImageMapper;
	private final FileStorageService fileStorageService;
	
	// 내가 쓴 글 총 개수
	public int countMyPosts(int userSeq) {
		return communityPostMapper.countMyPosts(userSeq);
	}

	// 내가 쓴 글 목록(페이징)
	public List<CommunityPost> findMyPosts(int userSeq, int size, int offset) {
		if (size <= 0) size = 5;
		if (offset < 0) offset = 0;
		return communityPostMapper.selectMyPostsPaging(userSeq, size, offset);
	}
	
	// 게시글 이미지 조회
	public List<CommunityPostImage> getPostImages(int postSeq) {
		if (postSeq <= 0) return Collections.emptyList();
		return communityPostImageMapper.selectImagesByPostSeq(postSeq);
	}
	
	// 등록 (글, 이미지 다건, post 테이블 대표 썸네일)
	// 한번이라도 실패 시 앞에서 성공한 DB 작업도 전부 롤백
	@Transactional
	public int insertPostWithImages(CommunityPost post, MultipartFile[] images) throws Exception {
		// 1) 글 먼저 저장 (postSeq 생성됨)
		int r = communityPostMapper.insertPost(post);
		if (r != 1 || post.getPostSeq() == 0) {
			throw new RuntimeException("게시글 저장에 실패했습니다.");
		}

		// 2) 이미지들 저장 + 테이블 insert
		String firstImagePath = null;

		if (images != null) {
			int order = 0;
			for (MultipartFile f : images) {
				if (f == null || f.isEmpty()) continue;

				String path = fileStorageService.savePostImage(f);
				if (firstImagePath == null) firstImagePath = path;

				CommunityPostImage img = new CommunityPostImage();
				img.setPostSeq(post.getPostSeq());
				img.setImagePath(path);
				img.setSortOrder(order++);

				communityPostImageMapper.insertPostImage(img);
			}
		}

		if (firstImagePath != null) {
			post.setPostImage(firstImagePath);
			communityPostMapper.updatePostThumbnail(post.getPostSeq(), firstImagePath);
		}

		return post.getPostSeq();
	}
	
	// 수정 (글, 기존이미지 일부삭제, 새이미지 추가, 대표썸네일 재계산)
	@Transactional
	public boolean updatePostWithImages(CommunityPost post, MultipartFile[] newImages, List<Integer> removeImageSeqs) throws Exception {

		// 1) 글(텍스트) 먼저 수정 (권한체크 포함된 update)
		int ur = communityPostMapper.updatePostByIdAndUser(post);
		if (ur <= 0) return false;

		// 2) 기존 이미지 일부 삭제 (DB row 삭제 + 파일 삭제)
		if (removeImageSeqs != null && !removeImageSeqs.isEmpty()) {

			// 파일 삭제용 경로 조회
			List<String> removePaths = communityPostImageMapper.selectImagePathsBySeqs(post.getPostSeq(), removeImageSeqs);

			// DB row 삭제
			communityPostImageMapper.deleteImagesBySeqs(post.getPostSeq(), removeImageSeqs);

			// 실제 파일 삭제
			fileStorageService.deletePostImages(removePaths);
		}

		// 3) 새 이미지 추가 (sort_order는 현재 이미지 개수 기준으로 뒤에 붙임)
		int nextOrder = communityPostImageMapper.selectImagesByPostSeq(post.getPostSeq()).size();

		if (newImages != null) {
			for (MultipartFile f : newImages) {
				if (f == null || f.isEmpty()) continue;

				String path = fileStorageService.savePostImage(f);

				CommunityPostImage img = new CommunityPostImage();
				img.setPostSeq(post.getPostSeq());
				img.setImagePath(path);
				img.setSortOrder(nextOrder++);

				communityPostImageMapper.insertPostImage(img);
			}
		}

		// 4) 대표썸네일 재계산 (첫번째 이미지 path로 post_image 갱신)
		String firstPath = communityPostImageMapper.selectFirstImagePathByPostSeq(post.getPostSeq());
		communityPostMapper.updatePostThumbnail(post.getPostSeq(), firstPath); // firstPath가 null이면 썸네일도 null

		return true;
	}

	// 삭제
	@Transactional
	public boolean deletePostWithImages(int postSeq, int userSeq, boolean isAdmin) {
		// 글 삭제 전에 이미지 경로 조회 → 글 삭제(이미지 row는 FK CASCADE로 자동 삭제) → 파일 삭제
		
		// 1) 파일 삭제를 하려면, 삭제 전에 경로를 먼저 뽑아둬야 함
		List<String> paths = communityPostImageMapper.selectImagePathsByPostSeq(postSeq);

		// 2) 글 삭제
		int dr = isAdmin ? communityPostMapper.deletePostById(postSeq) : communityPostMapper.deletePostByIdAndUser(postSeq, userSeq);

		if (dr <= 0) return false;

		// 3) 실제 파일 삭제
		fileStorageService.deletePostImages(paths);

		return true;
	}
	
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
	
	// 게시글 삭제 (관리자)
//	public boolean deletePostAdmin(int postSeq) {
//		return communityPostMapper.deletePostById(postSeq) > 0;
//	}
	@Transactional
	public boolean deletePostWithImagesAdmin(int postSeq) {
			List<String> paths = communityPostImageMapper.selectImagePathsByPostSeq(postSeq);
			int dr = communityPostMapper.deletePostById(postSeq);
			if (dr <= 0) return false;
			fileStorageService.deletePostImages(paths);
			return true;
	}
	
	
//	// 게시글 등록
//	public void insertPost(CommunityPost post) {
//		communityPostMapper.insertPost(post);
//	}
	
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
	
	// 관리자 대시보드
	public int countPostsAdmin(String category, String type, String keyword) {
		return communityPostMapper.countPostsAdmin(category, type, keyword);
	}
	public List<CommunityPost> getPostsAdminPaging(
			String category, String type, String keyword, String sort, int page, int size) {

		int offset = (page - 1) * size;
		return communityPostMapper.selectPostsAdminPaging(category, type, keyword, sort, size, offset);
	}

}
