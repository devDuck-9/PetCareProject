package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.CommunityComment;

@Mapper
public interface CommunityCommentMapper {

		// 특정 게시글 댓글 목록(작성자명 + isOwner 포함)
		List<CommunityComment> selectCommentsByPost(@Param("postSeq") int postSeq, @Param("loginUserSeq") int loginUserSeq);
		
		// 댓글 단건 조회(수정/삭제 권한 체크용)
		CommunityComment selectCommentByIdAndUser(@Param("commentSeq") int commentSeq, @Param("userSeq") int userSeq);
		
		// 댓글 단건 조회(삭제 후 redirect 용 postSeq 확보용)
		CommunityComment selectCommentById(@Param("commentSeq") int commentSeq);
		
		// 댓글 등록
		int insertComment(CommunityComment comment);
		
		// 댓글 수정(내 댓글만)
		int updateCommentByIdAndUser(CommunityComment comment);
		
		// 댓글 삭제(내 댓글만)
		int deleteCommentByIdAndUser(@Param("commentSeq") int commentSeq, @Param("userSeq") int userSeq);
		
		// 댓글 삭제(관리자용)
		int deleteCommentById(@Param("commentSeq") int commentSeq);
		
		// 댓글 조회(관리자용)
		List<CommunityComment> selectCommentsByPostAdmin(@Param("postSeq") int postSeq, @Param("sort") String sort);
		
}
