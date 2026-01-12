# 🐾 Pet Care Project

Spring Boot 기반의 **반려동물 통합 관리 웹 서비스**입니다.
회원은 자신의 반려동물을 등록하고, 일정을 관리하며
대시보드와 커뮤니티 기능을 통해 정보를 한눈에 확인할 수 있습니다.

---

## 📌 프로젝트 소개

**Pet Care Project**는 반려동물을 키우는 사용자를 위해
다음 기능을 제공하는 웹 애플리케이션입니다.

* 반려동물 정보 관리
* 일정 등록 및 관리
* 사용자 맞춤 대시보드
* 커뮤니티 게시판

> 본 프로젝트는 **학원 과정 중 진행한 미니프로젝트로, Spring Boot 학습 및 포트폴리오 목적**으로 제작되었습니다.

---

## 🖥 주요 기능

### 👤 회원 관리

* 회원가입 / 로그인
* Spring Security 기반 인증 처리
* 로그인 사용자 세션 관리

### 🐶 반려동물 관리

* 반려동물 등록
* 상세 정보 조회
* 정보 수정 및 삭제

### 📅 일정 관리

* 반려동물 일정 등록
* 대시보드 연동 일정 표시

### 🏠 대시보드

* 로그인 사용자 기준 데이터 집계
* 반려동물 및 일정 정보 요약 표시
* DTO 기반 데이터 전달 구조

### 💬 커뮤니티

* 게시글 작성
* 게시글 목록 조회
* 댓글 도메인 설계

---

## 🛠 기술 스택

### Backend

* Java 17
* Spring Boot
* Spring Security
* MyBatis
* Gradle

### Frontend

* Thymeleaf
* HTML5 / CSS3
* JavaScript

### Database

* MySQL

### Tools

* Git & GitHub
* Figma (UI 설계)
* MySQL Workbench
* Postman

---

## 📂 프로젝트 구조

```bash
petcareproject
 ┣ 📁 controller
 ┣ 📁 service
 ┣ 📁 mapper
 ┣ 📁 domain
 ┣ 📁 security
 ┣ 📁 resources
 ┃ ┣ 📁 templates
 ┃ ┃ ┣ 📁 views
 ┃ ┃ ┣ 📁 fragments
 ┃ ┃ ┗ 📁 layouts
 ┃ ┗ application.properties
 ┗ 📄 build.gradle
```

---

## 🚀 실행 방법

```bash
# 프로젝트 클론
git clone https://github.com/devDuck-9/petcareproject.git

# 프로젝트 실행
./gradlew bootRun
```

---

## 🎨 UI / UX

* Figma 기반 화면 설계
* Thymeleaf 레이아웃 & fragment 구조 적용
* 로그인 전/후 화면 분리

---

## 🙋‍♂️ 개발자

* GitHub: devDuck-9
* Project: Pet Care Project 🐾

---

## 📄 기타

* 본 프로젝트는 학원 커리큘럼 내 **개인 미니프로젝트**로 진행되었습니다.
