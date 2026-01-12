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
	
	// 중복 확인 완료 후 readonly 처리를 고려했으나
	// UX가 저하된다고 판단하여 해당 로직 제거
	/*if ($idInput.length) {
		$idInput.prop('readonly', false);
	}*/
	
}

// 아이디 입력이 바뀌면 다시 확인 필요
if ($idInput.length) {
	lastUserId = $idInput.val();
	
	$idInput.on('input', function () {
		const now = $(this).val();
		if (now !== lastUserId) {
			resetIdCheck();
			lastUserId = now;
		}
	});
}

// 중복확인 버튼 클릭
if ($checkBtn.length) {
	$checkBtn.on('click', async function (e) {
		e.preventDefault();
		
		// 이미 확인완료 상태일 경우
		if (isIdChecked) return;
		
		const userId = ($idInput.length ? $idInput.val() : '').trim();
		
		if (!userId) {
			Modal.open('#idCheckModal', '아이디를 입력해주세요.');
			if ($idInput.length) $idInput.trigger('focus');
			return;
		}

		try {
			const res = await fetch(
				`/api/member/check-id?userId=${encodeURIComponent(userId)}`,
				{ headers: { 'Accept': 'application/json' } }
			);

			if (!res.ok) {
				Modal.open('#idCheckModal', '서버 오류가 발생했어요.');
				return;
			}

			const data = await res.json();

			// 모달 메시지
			Modal.open('#idCheckModal', data.message);

			// exists=true 일 때 이미 사용중 , false 일 경우 사용 가능한 아이디
			if (data.exists === false) {
				isIdChecked = true;
				
				// 
				if ($checkBtn.length) {
					$checkBtn.addClass('is-done');
					$checkBtn.text('확인완료 !');
				}
			}
			
		} catch (err) {
			Modal.open('#idCheckModal', '네트워크 오류가 발생했어요.');
		}
	});
}

// 비밀번호 확인 메세지
const $form = $('.join-form');
const $pw1	= $form.find('input[name="password"]');
const $pw2	= $form.find('input[name="passwordConfirm"]');
const $err	= $('#pwError');

function validatePw() {
	// submit 에서 쓰기 위해 true 반환
	if (!$pw1.length || !$pw2.length || !$err.length) return true;
	
	const show = $pw1.val() && $pw2.val() && $pw1.val() !== $pw2.val();
	$err.css('display', show ? 'block' : 'none');
	return !show;
}

if ($pw1.length) $pw1.on('input', validatePw);
if ($pw2.length) $pw2.on('input', validatePw);

// 이메일 도메인
const $domainInput	= $('input[name="emailDomainInput"]');
const $domainSelect = $('select[name="emailDomainSelect"]');
const $domainFinal	= $('#emailDomainFinal');

function setManualMode() {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	$domainInput.val('');		// 비우기
	$domainInput.prop('readonly', false);	// 입력 가능
	$domainInput.attr('placeholder', '도메인 직접입력');
	$domainInput.trigger('focus');

	$domainFinal.val('');	// hidden 비우기
}

function setPresetMode(domain) {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	$domainInput.val(domain);	// 선택값을 input에 보여주기
	$domainInput.prop('readonly', true);	// 수정 불가
	$domainInput.attr('placeholder', '');

	$domainFinal.val(domain);	// hidden에 최종값 저장
}

function syncDomainFromSelect() {
	if (!$domainSelect.length || !$domainInput.length || !$domainFinal.length) return;

	const v = $domainSelect.val();

	if (v === 'manual') {
		setManualMode();
	} else {
		setPresetMode(v);
	}
}

// select 변경 시 모드 전환
if ($domainSelect.length) {
	$domainSelect.on('change', syncDomainFromSelect);
}

// 도메인 직접입력에서 타이핑하면 hidden에 반영
if ($domainInput.length) {
	$domainInput.on('input', function () {
		if (!$domainFinal.length || !$domainSelect.length) return;
		if ($domainSelect.val() === 'manual') {
			$domainFinal.val($domainInput.val().trim());
		}
	});
}

// 초기 상태 반영
syncDomainFromSelect();

// submit 이벤트
if ($form.length) {
	$form.on('submit', function (e) {
		
		// 비밀번호 불일치 방지
		const pwOk = validatePw();
		if (!pwOk) {
			e.preventDefault();
			return;
		}
		
		// 이메일 도메인 검증 (manual일 때만)
		if ($domainSelect.length && $domainSelect.val() === 'manual') {
			const d = $domainInput.length ? $domainInput.val().trim() : '';
			if (!d) {
				e.preventDefault();
				if ($domainInput.length) $domainInput.trigger('focus');
				alert('이메일 도메인을 입력해주세요.');
				return;
			}
		}
		
		// 아이디 중복확인
		if (!isIdChecked) {
			e.preventDefault();
			Modal.open('#idCheckModal', '아이디 중복확인을 먼저 해주세요.');
			return;
		}

	});
}