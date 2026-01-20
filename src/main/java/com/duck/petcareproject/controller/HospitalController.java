package com.duck.petcareproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HospitalController {
	
	
	@GetMapping("/hospital")
	public String hospitalList() {
		return "/hospital";
	}
	
}
