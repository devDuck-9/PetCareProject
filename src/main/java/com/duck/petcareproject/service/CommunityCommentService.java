package com.duck.petcareproject.service;

import java.util.List;

import com.duck.petcareproject.domain.CommunityComment;

public interface CommunityCommentService {

	List<CommunityComment> getCommentsByPost(int postSeq, int loginUserSeq);
	
	void insertComment(CommunityComment comment);
	
	CommunityComment getCommentByIdAndUser(int commentSeq, int userSeq);
	
	CommunityComment getComment(int commentSeq);
	
	boolean updateComment(CommunityComment comment);
	
	boolean deleteComment(int commentSeq, int userSeq);
}
