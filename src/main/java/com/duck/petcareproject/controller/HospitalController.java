package com.duck.petcareproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HospitalController {
	
	// 병원정보 페이지
	@GetMapping("/hospital")
	public String hospitalPage(Model model) {
		// 헤더 메뉴 활성화
		model.addAttribute("activeMenu", "hospital");
		return "hospital";
	}

}
