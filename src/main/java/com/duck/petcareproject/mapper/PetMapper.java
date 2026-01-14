package com.duck.petcareproject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.duck.petcareproject.domain.Pet;

@Mapper
public interface PetMapper {
	
	// 내 반려동물 수정
	int updatePetBySeqAndUser(Pet pet);

	// 내 반려동물 삭제
	int deletePetBySeqAndUser(@Param("petSeq") int petSeq, @Param("userSeq") int userSeq);
	
	// 선택한 반려동물 조회
	public Pet selectPetBySeqAndUser(@Param("petSeq") int petSeq, @Param("userSeq") int userSeq);
	
	// 반려동물 등록
	public int insertPet(Pet pet);
	
	// userSeq 의 반려동물 페이징 목록 _ 화면용
	public List<Pet> selectPetsByUserPaging(@Param("userSeq") int userSeq, @Param("limit") int limit, @Param("offset") int offset);
	
	// 반려동물 전체 목록 _ 존재여부
	public List<Pet> selectPetsByUser(@Param("userSeq") int userSeq);
	
	// userSeq 의 반려동물 전체수
	public int countPetsByUser(@Param("userSeq") int userSeq);
}
