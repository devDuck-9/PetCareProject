package com.duck.petcareproject.service;

import java.util.List;

import com.duck.petcareproject.domain.Pet;

public interface PetService {

	// 펫 수정
	void updatePet(Pet pet);
	
	// 펫 삭제
	boolean deletePet(int petSeq, int userSeq);
	
	// 선택한 펫 정보
	public Pet getPetBySeq(int petSeq, int userSeq);
	
	// 펫 등록
	public void insertPet(Pet pet);
	
	// 펫 목록
	public List<Pet> selectPetsByUser(int userSeq);

}
