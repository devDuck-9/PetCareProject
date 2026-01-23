package com.duck.petcareproject.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.duck.petcareproject.domain.Member;
import com.duck.petcareproject.domain.Role;
import com.duck.petcareproject.domain.Status;
import com.duck.petcareproject.service.MemberService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Member member = memberService.getMember(userId);
        
        if (member == null) {
        	// 아이디 없음
        	throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
        }
        
        Status st = member.getStatus();
        if (st == null || st == Status.WITHDRAWN || st == Status.PURGED) {
        	throw new WithdrawnAccountException("해당 계정은 이용할 수 없습니다.");
        }
        
        // 권한 적용 (ADMIN/USER) / Spring Security 는 "ROLE_" prefix 형태를 권장
        Role roleEnum = member.getRole();
        String role = (roleEnum == null ? "USER" : roleEnum.name());
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return new org.springframework.security.core.userdetails.User(
                member.getUserId(),
                member.getPassword(), // DB에 BCrypt로 저장된 값
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
