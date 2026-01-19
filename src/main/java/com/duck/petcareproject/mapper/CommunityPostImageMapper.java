package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.CommunityPostImage;

@Mapper
public interface CommunityPostImageMapper {
	
	// 이미지 저장
	int insertPostImage(CommunityPostImage image);
	// 목록조회
	List<CommunityPostImage> selectImagesByPostSeq(@Param("postSeq") int postSeq);
	// 전체삭제
	int deleteImagesByPostSeq(@Param("postSeq") int postSeq);
	
	// postSeq로 전체 이미지 경로 조회 (삭제 시 파일 삭제용)
	List<String> selectImagePathsByPostSeq(@Param("postSeq") int postSeq);

	// 삭제 대상 이미지의 경로 조회 (수정 시 개별 삭제용)
	List<String> selectImagePathsBySeqs(@Param("postSeq") int postSeq, @Param("imageSeqs") List<Integer> imageSeqs);

	// 삭제 대상 이미지 row 삭제 (수정 시 개별 삭제용)
	int deleteImagesBySeqs(@Param("postSeq") int postSeq, @Param("imageSeqs") List<Integer> imageSeqs);
	
	// 대표이미지(썸네일) 갱신용 : 첫번째 이미지 path
	String selectFirstImagePathByPostSeq(@Param("postSeq") int postSeq);
	
}
