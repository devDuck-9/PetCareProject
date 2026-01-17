package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.CommunityComment;
import com.duck.petcareproject.mapper.CommunityCommentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityCommentServiceImplement implements CommunityCommentService {
	
	private final CommunityCommentMapper communityCommentMapper;

	// 특정 게시글 댓글 목록(작성자명 + isOwner 포함)
	public List<CommunityComment> getCommentsByPost(int postSeq, int loginUserSeq) {
		return communityCommentMapper.selectCommentsByPost(postSeq, loginUserSeq);
	}
	
	// 댓글 등록
	public void insertComment(CommunityComment comment) {
		communityCommentMapper.insertComment(comment);
	}
	
	// 댓글 단건 조회(수정/삭제 권한 체크용)
	public CommunityComment getCommentByIdAndUser(int commentSeq, int userSeq) {
		return communityCommentMapper.selectCommentByIdAndUser(commentSeq, userSeq);
	}
	
	// 댓글 단건 조회(삭제 후 redirect 용 postSeq 확보용)
	public CommunityComment getComment(int commentSeq) {
		return communityCommentMapper.selectCommentById(commentSeq);
	}
	
	// 댓글 수정(내 댓글만)
	public boolean updateComment(CommunityComment comment) {
		return communityCommentMapper.updateCommentByIdAndUser(comment) > 0;
	}
	
	// 댓글 삭제(내 댓글만)
	public boolean deleteComment(int commentSeq, int userSeq) {
		return communityCommentMapper.deleteCommentByIdAndUser(commentSeq, userSeq) > 0;
	}
	
	// 댓글 삭제(관리자용)
	public boolean deleteCommentAdmin(int commentSeq) {
		return communityCommentMapper.deleteCommentById(commentSeq) > 0;
	}
	
	// 댓글 조회(관리자용)
	public List<CommunityComment> getCommentsByPostAdmin(int postSeq, String sort) {
		 return communityCommentMapper.selectCommentsByPostAdmin(postSeq, sort);
	}

}
