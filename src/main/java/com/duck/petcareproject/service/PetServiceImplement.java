package com.duck.petcareproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.Pet;
import com.duck.petcareproject.mapper.PetMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetServiceImplement implements PetService {
	
	private final PetMapper petMapper;
	
	// 펫 수정
	public void updatePet(Pet pet) {
		petMapper.updatePetBySeqAndUser(pet);
	}

	// 펫 삭제 (성공여부 반환)
	public boolean deletePet(int petSeq, int userSeq) {
		return petMapper.deletePetBySeqAndUser(petSeq, userSeq) == 1;
	}
	
	// 선택한 펫 정보
	public Pet getPetBySeq(int petSeq, int userSeq) {
		return petMapper.selectPetBySeqAndUser(petSeq, userSeq);
	}
	
	// 펫 등록
	public void insertPet(Pet pet) {
		petMapper.insertPet(pet);
	}
	
	// 펫 전체 목록
	public List<Pet> selectPetsByUser(int userSeq){
		return petMapper.selectPetsByUser(userSeq);
	};
	
}
