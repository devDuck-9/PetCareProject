$(function () {
	// 아이디 중복확인 상태 관리
	const $idInput	= $('input[name="userId"]');
	const $checkBtn = $('#btnCheckId');
	
	// 중복확인 완료 여부
	let isIdChecked = false;
	
	// 변경되는 아이디값
	let lastUserId = '';
	
	// 버튼 상태 초기화
	function resetIdCheck() {
		isIdChecked = false;
	
		if ($checkBtn.length) {
			$checkBtn.prop('disabled', false);
			$checkBtn.removeClass('is-done');
			$checkBtn.text('중복확인');
		}
	}
	
	function validateUserIdFormat() {
		const v = ($idInput.val() || '').replace(/\s/g, '');
		const ok = /^[A-Za-z0-9_]{4,20}$/.test(v);

		// 버튼 제어
		$checkBtn.prop('disabled', !ok || isIdChecked);

		// 빨간 문구
		if (!ok && v.length > 0) {
			$('#userIdFormatError').show();
		} else {
			$('#userIdFormatError').hide();
		}

		return ok;
	}
	
	// 아이디 입력이 바뀌면 다시 확인 필요
	if ($idInput.length) {
		lastUserId = $idInput.val();
		
		$idInput.on('input', function () {
			const cleaned = $(this).val().replace(/\s/g, '');
			if ($(this).val() !== cleaned) $(this).val(cleaned);
			
			// 값이 바뀌면 중복확인 무효화
			if (cleaned !== lastUserId) {
				resetIdCheck();
				lastUserId = cleaned;
			}
			validateUserIdFormat();
		});
		validateUserIdFormat();
	}
	
	// 중복확인 버튼 클릭
	if ($checkBtn.length) {
		$checkBtn.on('click', async function (e) {
			e.preventDefault();
	
			if (isIdChecked) return;
	
			const cleaned = (($idInput.val() || '')).replace(/\s/g, '');

			if (!cleaned) { Modal.open('#idCheckModal','아이디를 입력해주세요.'); $idInput.trigger('focus'); return; }
			if (!/^[A-Za-z0-9_]{4,20}$/.test(cleaned)) {
				Modal.open('#idCheckModal','아이디는 4~20자, 영문/숫자/_ 만 사용 가능합니다.');
				$idInput.trigger('focus');
				return;
			}

			try {
				const res = await fetch(`/api/member/check-id?userId=${encodeURIComponent(cleaned)}`, {
					headers: { 'Accept': 'application/json' }
				});

				if (!res.ok) { Modal.open('#idCheckModal','서버 오류가 발생했어요.'); return; }

				const data = await res.json();
				Modal.open('#idCheckModal', data.message);

				if (data.exists === false) {
					isIdChecked = true;
					$checkBtn.addClass('is-done').text('확인완료 !');
					validateUserIdFormat(); // 상태 반영
				}
			} catch {
				Modal.open('#idCheckModal','네트워크 오류가 발생했어요.');
			}
		});
	}
	
	// 비밀번호 확인 메세지
	const $form = $('.join-form');
	const $pw1	= $form.find('input[name="password"]');
	const $pw2	= $form.find('input[name="passwordConfirm"]');
	const $err	= $('#pwError');
	
	// 비밀번호 형식 에러(p)
	const $pwFormatErr = $('#userPwFormatError');
	
	function showPwFormatError() {
		if ($pwFormatErr.length) $pwFormatErr.show();
	}

	function hidePwFormatError() {
		if ($pwFormatErr.length) $pwFormatErr.hide();
	}

	// 비밀번호 형식(길이+허용문자+공백금지)만 체크
	function validatePwFormatOnly() {
		if (!$pw1.length) return true;
		
		const v = ($pw1.val() || '');
		
		// submit에서는 빈값도 에러
		if (!v.length) {
			if (show) showPwFormatError();
			else hidePwFormatError();
			return false;
		}
		
		// 입력 전엔 숨김
		if (!v.length) {
			hidePwFormatError();
			return true;
		}
		
		const okLen = v.length >= 8 && v.length <= 20;
		const okNoSpace = !/\s/.test(v);
		const allow = /^[A-Za-z\d~`!@#$%^&*()_+\-={}[\]|\\:;\"'<>,.?/]+$/;
		const okAllow = allow.test(v);
		
		const ok = okLen && okNoSpace && okAllow;
		
		if (!ok) showPwFormatError();
		else hidePwFormatError();

		return ok;
	}

	// 입력 시 즉시 반응
	if ($pw1.length) {
		$pw1.on('input', () => {
			const v = ($pw1.val() || '');
			if (!v.length) { hidePwFormatError(); return; }	// 입력 안 하면 숨김
			validatePwFormatOnly({ show: true });
		});
	}
	
	function showPwError(msg) {
		if (!$err.length) return;
		if (msg) $err.text(msg);
		$err.show();
	}

	function hidePwError() {
		if ($err.length) $err.hide();
	}

	function validatePw({ forceShow = false } = {}) {
		if (!$pw1.length || !$pw2.length || !$err.length) return true;

		const v1 = $pw1.val() || '';
		const v2 = $pw2.val() || '';

		// 둘 중 하나라도 입력이 시작되었거나, submit에서 강제로 보여야 할 때만 노출
		const shouldCheck = forceShow || v1.length > 0 || v2.length > 0;

		// 일치 여부만 판단
		const mismatch = shouldCheck && v1 && v2 && v1 !== v2;

		if (mismatch) showPwError('비밀번호가 일치하지 않습니다.');
		else hidePwError();

		return !mismatch;
	}
	
	if ($pw1.length) $pw1.on('input', () => validatePw());
	if ($pw2.length) $pw2.on('input', () => validatePw());
	
	// 이메일 도메인
	const $domainInput	= $('input[name="emailDomainInput"]');
	const $domainSelect = $('select[name="emailDomainSelect"]');
	const $domainFinal	= $('#emailDomainFinal');
	
	function setManualMode() {
		if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;
	
		$domainInput.val('');
		$domainInput.prop('readonly', false);
		$domainInput.attr('placeholder', '도메인 직접입력');
	
		$domainFinal.val('');
	}
	
	function setPresetMode(domain) {
		if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;
	
		$domainInput.val(domain);
		$domainInput.prop('readonly', true);
		$domainInput.attr('placeholder', '');
	
		$domainFinal.val(domain);
	}
	
	function syncDomainFromSelect() {
		if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;
	
		const v = $domainSelect.val();
		if (v === 'manual') setManualMode();
		else setPresetMode(v);
	}
	
	if ($domainSelect.length) {
		$domainSelect.on('change', function () {
			syncDomainFromSelect();
	
			// manual 선택했을 때만 포커스
			if ($(this).val() === 'manual' && $domainInput.length) {
				$domainInput.trigger('focus');
			}
		});
	}
	
	if ($domainInput.length) {
		$domainInput.on('input', function () {
			if (!$domainFinal.length || !$domainSelect.length) return;
			if ($domainSelect.val() === 'manual') {
				$domainFinal.val($domainInput.val().trim());
			}
		});
	}
	
	syncDomainFromSelect();
	
	function getFullEmail() {
		const id = $('input[name="emailId"]').val()?.trim();
		const domain = $('#emailDomainFinal').val()?.trim();
		if (!id || !domain) return '';
		return `${id}@${domain}`;
	}
	
	// --------------------
	// 이메일 인증
	// --------------------
	const $emailSendBtn = $('#btnSendEmailCode');
	const $emailVerifyBtn = $('#btnVerifyEmailCode');
	const $emailCodeInput = $('#emailCode');
	const $emailMsg = $('#emailMsg');
	const $emailVerified = $('#emailVerified');
	
	let lastEmail = '';
	let sendEmaildownTimer = null;
	
	if ($emailCodeInput.length) {
		$emailCodeInput.on('input', function () {
			this.value = this.value.replace(/\D/g, '').slice(0, 6);
		});
	}
	
	/* 이메일 재발송 시간 제한 */
	function setSendEmaildown(seconds) {
		if (!$emailSendBtn.length) return;
		if (sendEmaildownTimer) {
			clearInterval(sendEmaildownTimer);
			sendEmaildownTimer = null;
		}
	
		let remain = seconds;
		$emailSendBtn.prop('disabled', true);
		$emailSendBtn.text(`재발송 (${remain}s)`);
	
		sendEmaildownTimer = setInterval(() => {
			remain -= 1;
			if (remain <= 0) {
				clearInterval(sendEmaildownTimer);
				sendEmaildownTimer = null;
				$emailSendBtn.prop('disabled', false);
				$emailSendBtn.text('인증번호 발송');
				return;
			}
			$emailSendBtn.text(`재발송 (${remain}s)`);
		}, 1000);
	}
	
	function showEmailMsg(text) {
		if ($emailMsg.length) $emailMsg.text(text).show();
	}
	
	function resetEmailVerify() {
		if ($emailVerified.length) $emailVerified.val('false');
		if ($emailCodeInput.length) $emailCodeInput.val('').prop('disabled', false);
		if ($emailMsg.length) $emailMsg.hide();
		if ($emailVerifyBtn.length) $emailVerifyBtn.prop('disabled', false).text('확인');
	}
	
	// 이메일 변경 시 인증 무효
	$('input[name="emailId"], input[name="emailDomainInput"], select[name="emailDomainSelect"]').on('change input', function () {
		const now = getFullEmail();
		if (now !== lastEmail) {
			resetEmailVerify();
			lastEmail = getFullEmail();
		}
	});
	
	// 발송
	$emailSendBtn.on('click', async function () {
		const $btn = $(this);
	
		// 이미 카운트다운 중이면 무시
		if ($btn.prop('disabled')) return;
	
		const email = getFullEmail();
		if (!email) {
			showEmailMsg('이메일을 먼저 정확히 입력해주세요.');
			return;
		}
	
		// 전송중
		$btn.prop('disabled', true).text('전송중...');
	
		try {
			const res = await fetch('/api/email/send', {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({ email })
			});
	
			const data = await res.json().catch(() => ({}));
			
			// 콘솔로 이메일 인증코드 찍어주기
			if (data.devCode) {
				console.log("[DEV EMAIL CODE]", data.devCode);
			}
			
			if (!res.ok) {
				showEmailMsg(data.message || '이메일 발송 실패');
				// 실패면 버튼 원복
				$btn.prop('disabled', false).text('인증번호 발송');
				return;
			}
	
			resetEmailVerify();
			showEmailMsg(data.message || '인증번호를 이메일로 발송했어요.');
	
			// 성공하면 재발송 카운트다운 시작 (5초)
			setSendEmaildown(5);
			
		} catch (e) {
			showEmailMsg('네트워크 오류');
			// 예외면 버튼 원복
			$btn.prop('disabled', false).text('인증번호 발송');
		}
	});
	
	// 확인
	$emailVerifyBtn.on('click', async function () {
		const email = getFullEmail();
		const code = $emailCodeInput.val().replace(/\D/g, '').trim();
	
		if (!email) return showEmailMsg('이메일을 입력해주세요.');
		if (!/^\d{6}$/.test(code)) return showEmailMsg('인증번호 6자리를 입력해주세요.');
	
		try {
			const res = await fetch('/api/email/verify', {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: new URLSearchParams({ email, code })
			});
	
			const data = await res.json().catch(() => ({}));
			if (!res.ok) {
				showEmailMsg(data.message || '인증 실패');
				return;
			}
	
			$emailVerified.val('true');
			$emailVerifyBtn.text('완료').prop('disabled', true);
			$emailCodeInput.prop('disabled', true);
			showEmailMsg(data.message);
		} catch (e) {
			showEmailMsg('네트워크 오류');
		}
	});
	
	// --------------------
	// 휴대폰(select + input)
	// --------------------
	const $m1 = $('#mobile1');
	const $m2 = $('#mobile2');
	const $m3 = $('#mobile3');
	const $mFinal = $('#mobileFinal');
	const $mErr = $('#mobileError');
	
	function syncMobile() {
		if (!$m1.length || !$m2.length || !$m3.length || !$mFinal.length) return;
	
		const v1 = $m1.val();
		const v2 = ($m2.val() || '').replace(/\D/g, '').slice(0, 4);
		const v3 = ($m3.val() || '').replace(/\D/g, '').slice(0, 4);
	
		$m2.val(v2);
		$m3.val(v3);
	
		// 가운데 3~4자리, 끝 4자리
		if (v2.length >= 3 && v3.length === 4) $mFinal.val(`${v1}-${v2}-${v3}`);
		else $mFinal.val('');
		
		// 번호가 바뀌면 인증 무효 처리
		const nowMobile = $mFinal.val();
		if (nowMobile !== mobileLast) {
			resetSmsVerification();
			mobileLast = nowMobile;
		}
		
	}
	
	// 숫자만 입력
	$('#smsCode').on('input', function () {
		this.value = this.value.replace(/\D/g, '').slice(0, 6);
	});
	
	// 휴대폰 인증
	const $sendBtn = $('#btnSendCode');
	const $verifyBtn = $('#btnVerifyCode');
	const $codeInput = $('#smsCode');
	const $smsMsg = $('#smsMsg');
	const $mobileVerified = $('#mobileVerified');
	
	let mobileLast = '';
	let sendCooldownTimer = null;
	
	function hideSmsMsg() {
		if ($smsMsg.length) $smsMsg.hide();
	}
	
	function showSmsMsg(text) {
		if (!$smsMsg.length) return;
		$smsMsg.text(text).show();
	}
	
	function resetSmsVerification() {
		if ($mobileVerified.length) $mobileVerified.val('false');
		// 입력칸 다시 열기
		if ($codeInput.length) $codeInput.val('').prop('disabled', false);
		hideSmsMsg();
		// 확인 버튼도 다시 열고 텍스트 원복
		if ($verifyBtn.length) $verifyBtn.prop('disabled', false).text('확인');
	}
	
	function setSendCooldown(seconds) {
		if (!$sendBtn.length) return;
		if (sendCooldownTimer) {
			clearInterval(sendCooldownTimer);
			sendCooldownTimer = null;
		}
	
		let remain = seconds;
		$sendBtn.prop('disabled', true);
		$sendBtn.text(`재발송 (${remain}s)`);
	
		sendCooldownTimer = setInterval(() => {
			remain -= 1;
			if (remain <= 0) {
				clearInterval(sendCooldownTimer);
				sendCooldownTimer = null;
				$sendBtn.prop('disabled', false);
				$sendBtn.text('인증번호 발송');
				return;
			}
			$sendBtn.text(`재발송 (${remain}s)`);
		}, 1000);
	}
	
	// --------------------
	// 휴대폰 인증
	// --------------------
	if ($sendBtn.length) {
		$sendBtn.on('click', async function () {
			syncMobile();
			const mobile = ($mFinal.length ? $mFinal.val() : '').trim();
			if (!mobile) {
				showSmsMsg('휴대폰 번호를 먼저 정확히 입력해주세요.');
				if ($m2.length) $m2.trigger('focus');
				return;
			}
	
			try {
				const res = await fetch('/api/sms/send', {
					method: 'POST',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: new URLSearchParams({ mobile })
				});
	
				const data = await res.json().catch(() => ({}));
				
				// 콘솔로 SMS 인증코드 찍어주기
				if (data.devCode) {
					console.log('[DEV SMS CODE] =', data.devCode);
				}
				
				if (!res.ok) {
					showSmsMsg(data.message || '인증번호 발송에 실패했어요.');
					return;
				}
	
				resetSmsVerification();
				showSmsMsg(data.message || '인증번호를 발송했어요.');
				setSendCooldown(5);
			} catch (e) {
				showSmsMsg('네트워크 오류가 발생했어요.');
			}
		});
	}
	
	if ($verifyBtn.length) {
		$verifyBtn.on('click', async function () {
			syncMobile();
			const mobile = ($mFinal.length ? $mFinal.val() : '').trim();
			const code = ($codeInput.length ? $codeInput.val() : '').replace(/\D/g, '').trim();
	
			if (!mobile) return showSmsMsg('휴대폰 번호를 먼저 입력해주세요.');
			if (!/^\d{6}$/.test(code)) return showSmsMsg('인증번호 6자리를 입력해주세요.');
	
			try {
				const res = await fetch('/api/sms/verify', {
					method: 'POST',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: new URLSearchParams({ mobile, code })
				});
	
				const data = await res.json().catch(() => ({}));
				if (!res.ok) {
					if ($mobileVerified.length) $mobileVerified.val('false');
					showSmsMsg(data.message || '인증에 실패했어요.');
					return;
				}
				
				// 인증 성공 잠금 처리
				if ($mobileVerified.length) $mobileVerified.val('true');
				if ($verifyBtn.length) $verifyBtn.text('완료').prop('disabled', true);
				if ($codeInput.length) $codeInput.prop('disabled', true);
				
				if ($mobileVerified.length) $mobileVerified.val('true');
				showSmsMsg(data.message || '인증 완료! ✅');
				if ($verifyBtn.length) $verifyBtn.prop('disabled', true);
			} catch (e) {
				showSmsMsg('네트워크 오류가 발생했어요.');
			}
		});
	}
	
	if ($m2.length) $m2.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
	if ($m3.length) $m3.on('input', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
	if ($m1.length) $m1.on('change', function(){ syncMobile(); if ($mErr.length) $mErr.hide(); });
	
	syncMobile();
	
	
	// --------------------
	// 주소 찾기 (다음 우편번호)
	// --------------------
	$('#btnSearchAddress').on('click', function () {
		new daum.Postcode({
			oncomplete: function (data) {
				const zipcode = data.zonecode;
				const addr = data.roadAddress || data.jibunAddress;
	
				$('#zipcode').val(zipcode);
				$('#address1').val(addr);
				$('#address2').val('').focus();
			}
		}).open();
	});
	
	
	// --------------------
	// 이름(닉네임) 중복확인
	// --------------------
	const $nameInput = $('input[name="userName"]');
	const $nameBtn	 = $('#btnCheckName');
	
	let isNameChecked = false;
	let lastUserName	= '';
	
	function resetNameCheck() {
		isNameChecked = false;
		if ($nameBtn.length) {
			$nameBtn.prop('disabled', false);
			$nameBtn.removeClass('is-done');
			$nameBtn.text('중복확인');
		}
	}
	
	function validateUserNameFormat() {
		const v = ($nameInput.val() || '').replace(/\s/g, '');
		const ok = /^[가-힣A-Za-z0-9_]{2,12}$/.test(v);

		// 버튼 제어
		if ($nameBtn.length) $nameBtn.prop('disabled', !ok || isNameChecked);

		// 빨간 문구
		if ($('#userNameFormatError').length) {
			$('#userNameFormatError').css('display', (!ok && v.length > 0) ? 'block' : 'none');
		}

		return ok;
	}
	
	if ($nameInput.length) {
		lastUserName = ($nameInput.val() || '').replace(/\s/g, '');
		
		$nameInput.on('input', function () {
			const cleaned = ($(this).val() || '').replace(/\s/g, '');
			if ($(this).val() !== cleaned) $(this).val(cleaned);

			if (cleaned !== lastUserName) {
				resetNameCheck();
				lastUserName = cleaned;
			}
			validateUserNameFormat();
		});
		validateUserNameFormat();
	}
	
	if ($nameBtn.length) {
		$nameBtn.on('click', async function (e) {
			e.preventDefault();
			if (isNameChecked) return;
	
			// 공백 전부 제거, 2~12, 한글/영문/숫자/_
			const cleaned = (($nameInput.val() || '')).replace(/\s/g, '');
			if (!cleaned) { Modal.open('#nameCheckModal','이름(닉네임)을 입력해주세요.'); $nameInput.trigger('focus'); return; }
			
			if (!/^[가-힣A-Za-z0-9_]{2,12}$/.test(cleaned)) {
				Modal.open('#nameCheckModal','이름(닉네임)은 2~12자, 한글/영문/숫자/_ 만 가능합니다.');
				$nameInput.trigger('focus');
				return;
			}
			
			// 서버 호출
			try {
				const res = await fetch(`/api/member/check-name?userName=${encodeURIComponent(cleaned)}`, { 
					headers: { 'Accept': 'application/json' } }
				);
	
				if (!res.ok) { Modal.open('#nameCheckModal','서버 오류가 발생했어요.'); return; }
	
				const data = await res.json();
				Modal.open('#nameCheckModal', data.message);
	
				if (data.exists === false) {
					isNameChecked = true;
					$nameBtn.addClass('is-done').text('확인완료 !');
					validateUserNameFormat(); // 버튼 잠금 반영
				}
			} catch (err) {
				Modal.open('#nameCheckModal', '네트워크 오류가 발생했어요.');
			}
		});
	}
	
	// === 비밀번호 정책(서버와 동일) ===
	// 서버를 갔다오면 입력했던 값 전부 리셋되는 문제해결
	function validatePwPolicy({ show = true } = {}) {
		if (!$pw1.length) return true;

		const v = ($pw1.val() || '');
		
		// submit에서는 show=true로 형식도 강제
		const okFormat = validatePwFormatOnly({ show });
		if (!okFormat) return false;
		
		const hasAlpha = /[A-Za-z]/.test(v);
		const hasDigit = /\d/.test(v);
		const hasSpecial = /[~`!@#$%^&*()_+\-={}[\]|\\:;\"'<>,.?/]/.test(v);

		const kindCount = (hasAlpha?1:0) + (hasDigit?1:0) + (hasSpecial?1:0);
		
		if (kindCount < 2) {
			if (show) showPwFormatError();
			return false;
		}
		
		hidePwFormatError();
		return true;
	}
	
	// --------------------
	// submit 이벤트
	// --------------------
	if ($form.length) {
		$form.on('submit', function (e) {
			
			// 비밀번호 정책 먼저 (서버로 가기 전에 막기)
			if (!validatePwPolicy()) {
				e.preventDefault();
				$pw1.trigger('focus');
				return;
			}
			
			// 비밀번호 확인(일치) 체크
			if (!validatePw({ forceShow: true })) {
				e.preventDefault();
				$pw2.trigger('focus');
				return;
			}
			
			// 휴대폰 hidden 동기화 + 검증
			syncMobile();
			if ($mFinal.length && !$mFinal.val()) {
				e.preventDefault();
				if ($mErr.length) $mErr.show();
				if ($m2.length) $m2.trigger('focus');
				return;
			}
	
			// 이메일 도메인 검증 (manual일 때만)
			if ($domainSelect.length && $domainSelect.val() === 'manual') {
				const d = $domainInput.length ? $domainInput.val().trim() : '';
				if (!d) {
					e.preventDefault();
					if ($domainInput.length) $domainInput.trigger('focus');
					Modal.open('#emailCheckModal', '이메일 도메인을 입력해주세요.');
					return;
				}
			}
	
			// 아이디 중복확인
			if (!isIdChecked) {
				e.preventDefault();
				Modal.open('#idCheckModal', '아이디 중복확인을 먼저 해주세요.');
				return;
			}
			
			// 이름 중복확인
			if (!isNameChecked) {
				e.preventDefault();
				Modal.open('#nameCheckModal', '이름(닉네임) 중복확인을 먼저 해주세요.');
				return;
			}
			
			// 휴대폰 인증
			if ($mobileVerified.length && $mobileVerified.val() !== 'true') {
				e.preventDefault();
				showSmsMsg('휴대폰 인증을 완료해주세요.');
				if ($codeInput.length) $codeInput.trigger('focus');
				return;
			}
			
			// 이메일 인증
			if ($('#emailVerified').val() !== 'true') {
				e.preventDefault();
				showEmailMsg('이메일 인증을 완료해주세요.');
				return;
			}
			
		});
		
	}
	
	initMobileFromFinal();
	initEmailDomainFromFinal();
	
	// 복원한 값 기준으로 last를 먼저 맞춰둠
	mobileLast = ($('#mobileFinal').val() || '').trim();
	lastEmail  = getFullEmail();
	
	// hidden 동기화(안전장치)
	syncMobile();
	syncDomainFromSelect();
	
	// 서버에서 검증 걸릴 시 리셋 방지
	// 휴대폰 복원
	function initMobileFromFinal() {
		const v = ($('#mobileFinal').val() || '').trim();
		if (!v) return;

		const m = v.split('-');
		if (m.length !== 3) return;

		$('#mobile1').val(m[0]);
		$('#mobile2').val(m[1]);
		$('#mobile3').val(m[2]);
	}

	// 이메일 도메인 복원
	function initEmailDomainFromFinal() {
		const domain = ($('#emailDomainFinal').val() || '').trim();
		if (!domain) return;

		const $sel = $('select[name="emailDomainSelect"]');
		const $inp = $('input[name="emailDomainInput"]');

		// select에 있는 도메인이면 그걸로, 없으면 manual로 두고 input에 넣기
		if ($sel.find(`option[value="${domain}"]`).length) {
			$sel.val(domain).trigger('change');	 // setPresetMode 타게
		} else {
			$sel.val('manual').trigger('change'); // setManualMode 타게
			$inp.val(domain);
			$('#emailDomainFinal').val(domain);
		}
	}
	
});