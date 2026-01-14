$(function () {
	window.lastFocusEl = null;

	const MODAL_ID = '#petModal';
	const $modal = $(MODAL_ID);

	// 모달 닫힘 감지(클래스 변화) → 닫힌 다음 포커스 복원
	if ($modal.length) {
		const el = $modal.get(0);

		const observer = new MutationObserver(function (mutations) {
			for (const m of mutations) {
				if (m.attributeName === 'class') {
					// is-open이 빠진 순간 = 닫힘 완료
					if (!el.classList.contains('is-open')) {
						// 다음 tick에서 포커스(닫힘 애니/DOM 반영 이후)
						setTimeout(function () {
							const $f = window.lastFocusEl;
							if ($f && $f.length) $f.get(0).focus();
							window.lastFocusEl = null;
						}, 0);
					}
				}
			}
		});

		observer.observe(el, { attributes: true });
	}
	
	const today = new Date();
	const TODAY_Y = today.getFullYear();
	const TODAY_M = today.getMonth() + 1; // 0-based
	const TODAY_D = today.getDate();
	
	const $form = $('#petForm');

	const $name = $('input[name="petName"]');
	const $petType = $('input[name="petType"]');

	const $year = $('#birthYear');
	const $month = $('#birthMonth');
	const $day = $('#birthDay');
	const $birthHidden = $('#birthDate');

	// ===== 사진 미리보기 + 파일 제한 =====
	const $file = $('#petPhoto');
	const $preview = $('#petPhotoPreview');
	const $photoBox = $('#petPhotoBox');

	const MAX_MB = 5;
	const ALLOWED = ['image/jpeg', 'image/png', 'image/webp'];

	function pad2(n) { return String(n).padStart(2, '0'); }

	function openError(msg, $focusEl) {
		window.lastFocusEl = ($focusEl && $focusEl.length) ? $focusEl : null;
		Modal.open(MODAL_ID, msg);
	}
	
	// 미래날짜 검증
	function isFutureSelected() {
		const y = parseInt($year.val(), 10);
		const m = parseInt($month.val(), 10);
		const d = parseInt($day.val(), 10);

		const selected = new Date(y, m - 1, d);
		const today = new Date();
		today.setHours(0, 0, 0, 0);

		return selected.getTime() > today.getTime();
	}
	function rollbackToToday() {
		const t = new Date();
		const ty = t.getFullYear();
		const tm = pad2(t.getMonth() + 1);
		const td = pad2(t.getDate());

		$year.val(String(ty));
		$month.val(tm);

		fillDays(); // 해당 월의 일 다시 채우기
		$day.val(td);

		syncBirthHidden();

		Modal.open(MODAL_ID, '미래 날짜는 선택할 수 없습니다.');
	}

	function resetPreview() {
		$photoBox.removeClass('has-image');
		$preview.removeAttr('src');	// 깨진 이미지 방지
		$preview.hide();
		$photoBox.find('.pet-photo-icon').show();
	}

	// 파일 change 이벤트
	$file.on('change', function () {
		const f = this.files && this.files[0];

		// 선택 취소
		if (!f) {
			this.value = '';
			resetPreview();
			return;
		}

		// 타입 검증
		if (!ALLOWED.includes(f.type)) {
			Modal.open(MODAL_ID, '이미지는 JPG / PNG / WEBP 형식만 업로드할 수 있어요.');
			this.value = '';
			resetPreview();
			return;
		}

		// 용량 검증
		const maxBytes = MAX_MB * 1024 * 1024;
		if (f.size > maxBytes) {
			Modal.open(MODAL_ID, `이미지 용량은 ${MAX_MB}MB 이하만 업로드할 수 있어요.`);
			this.value = '';
			resetPreview();
			return;
		}

		// 미리보기
		const url = URL.createObjectURL(f);
		$photoBox.addClass('has-image');
		$preview.attr('src', url).show();
	});

	// ===== 태어난날 =====
	function daysInMonth(y, m) {
		return new Date(y, m, 0).getDate(); // m: 1~12
	}

	function fillYears() {
		const thisYear = new Date().getFullYear();
		const start = 1980;
		const end = thisYear;

		$year.empty();
		for (let y = end; y >= start; y--) {
			$year.append(`<option value="${y}">${y}</option>`);
		}

		// 기본값
		$year.val(String(TODAY_Y));
	}

	function fillMonths() {
		$month.empty();
		for (let m = 1; m <= 12; m++) {
			$month.append(`<option value="${pad2(m)}">${pad2(m)}</option>`);
		}
		$month.val(pad2(TODAY_M));
	}

	function fillDays() {
		const y = parseInt($year.val(), 10);
		const m = parseInt($month.val(), 10);
		const max = daysInMonth(y, m);

		const current = $day.val();
		$day.empty();

		for (let d = 1; d <= max; d++) {
			$day.append(`<option value="${pad2(d)}">${pad2(d)}</option>`);
		}

		if (!current) {
			$day.val(pad2(Math.min(TODAY_D, max)));
		} else {
			$day.val(parseInt(current, 10) > max ? pad2(max) : current);
		}
	}

	function syncBirthHidden() {
		const y = $year.val();
		const m = $month.val();
		const d = $day.val();
		$birthHidden.val(y && m && d ? `${y}-${m}-${d}` : '');
	}

	fillYears();
	fillMonths();
	fillDays();
	syncBirthHidden();

	$year.on('change', function () {
		fillDays();
		syncBirthHidden();
		if (isFutureSelected()) rollbackToToday();
	});

	$month.on('change', function () {
		fillDays();
		syncBirthHidden();
		if (isFutureSelected()) rollbackToToday();
	});
	
	$day.on('change', function () {
		syncBirthHidden();
		if (isFutureSelected()) rollbackToToday();
	});

	// ===== submit 검증 (모달) =====
	$form.on('submit', function (e) {
		const nameVal = ($name.val() || '').trim();
		const petTypeVal = ($petType.val() || '').trim();

		if (!nameVal) {
			e.preventDefault();
			openError('이름을 입력해주세요.', $name);
			return;
		}

		if (!petTypeVal) {
			e.preventDefault();
			openError('품종을 입력해주세요.', $petType);
			return;
		}

		if (!($birthHidden.val() || '').trim()) {
			e.preventDefault();
			openError('태어난 날을 선택해주세요.');
			return;
		}
	});
	
	// 수정폼일 경우 기존 birthDate 세팅
	const birthDateInput = document.getElementById('birthDate');
	if (birthDateInput && birthDateInput.dataset.value) {
		const [y, m, d] = birthDateInput.dataset.value.split('-');
		document.getElementById('birthYear').value = y;
		document.getElementById('birthMonth').value = m;
		document.getElementById('birthDay').value = d;
		birthDateInput.value = birthDateInput.dataset.value;
	}
	
	// 수정 취소 버튼 → 모달 확인
	$('#btnCancle').on('click', function (e) {
		e.preventDefault();
		
		const href = $(this).attr('href');
		
		Modal.open('#petCancleModal', '정말 취소하시겠습니까?<br><span style="opacity:.8">작성했던 정보가 사라집니다.</span>', {
			primaryText: '돌아가기',
			secondaryText: '취소'
		});
		// actionModal의 primary 버튼
		$('#petCancleModal [data-modal-primary]').off('click.petCancel').on('click.petCancel', function () {
			window.location.href = href;
		});
		
	});
	

});