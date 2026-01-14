$(function () {
	const MODAL_ID = '#schModal';
	const $modal = $(MODAL_ID);

	// ===== 모달 닫힌 뒤 포커스 복원 =====
	window.lastFocusEl = null;

	if ($modal.length) {
		const el = $modal.get(0);
		const observer = new MutationObserver(function (mutations) {
			for (const m of mutations) {
				if (m.attributeName === 'class' && !el.classList.contains('is-open')) {
					setTimeout(function () {
						const $f = window.lastFocusEl;
						if ($f && $f.length) $f.get(0).focus();
						window.lastFocusEl = null;
					}, 0);
				}
			}
		});
		observer.observe(el, { attributes: true });
	}

	function openError(msg, $focusEl) {
		window.lastFocusEl = ($focusEl && $focusEl.length) ? $focusEl : null;
		Modal.open(MODAL_ID, msg);
	}

	function pad2(n) { return String(n).padStart(2, '0'); }
	function daysInMonth(y, m) { return new Date(y, m, 0).getDate(); } // m: 1~12

	// ===== 기준 날짜 =====
	const today = new Date();
	today.setHours(0, 0, 0, 0);

	// 기본값은 내일
	const tomorrow = new Date(today);
	tomorrow.setDate(tomorrow.getDate() + 1);

	const DEF_Y = tomorrow.getFullYear();
	const DEF_M = tomorrow.getMonth() + 1;
	const DEF_D = tomorrow.getDate();

	// ===== DOM =====
	const $year = $('#schYear');
	const $month = $('#schMonth');
	const $day = $('#schDay');
	const $hidden = $('#schDateHidden');

	function isSelectableDate(y, m, d) {
		const sel = new Date(y, m - 1, d);
		sel.setHours(0, 0, 0, 0);
		return sel.getTime() > today.getTime();
	}

	function syncHidden() {
		const y = $year.val();
		const m = $month.val();
		const d = $day.val();
		$hidden.val(y && m && d ? `${y}-${m}-${d}` : '');
	}

	// 선택 가능한 첫 날짜(=내일)를 강제로 맞춰줌 (안전장치)
	function forceDefaultTomorrow() {
		$year.val(String(DEF_Y));
		$month.val(pad2(DEF_M));
		fillDays();									// days 재생성
		$day.val(pad2(DEF_D));
		syncHidden();
	}

	// ===== Year: (올해 ~ 올해+5) =====
	function fillYears() {
		const start = today.getFullYear();
		const end = start + 5;

		$year.empty();
		for (let y = start; y <= end; y++) {
			// 연도 자체는 미래 날짜가 있을 수 있으니 전부 활성
			$year.append(`<option value="${y}">${y}</option>`);
		}
		$year.val(String(DEF_Y));
	}

	// ===== Month: 선택한 year에 따라 과거 월 disable =====
	function fillMonths() {
		const y = parseInt($year.val(), 10);
		const thisY = today.getFullYear();
		const thisM = today.getMonth() + 1;

		$month.empty();

		for (let m = 1; m <= 12; m++) {
			let disabled = false;

			// 올해면 과거 월은 disable (미래만)
			if (y === thisY && m < thisM) disabled = true;

			// 올해 + 이번달이면 이번달도 가능하지만 날짜에서 오늘/과거를 막을 것
			$month.append(
				`<option value="${pad2(m)}" ${disabled ? 'disabled' : ''}>${pad2(m)}</option>`
			);
		}

		// 기본값 : 내일의 월
		$month.val(pad2(DEF_M));

		// 혹시 기본값 월이 disable이면 다음 활성 월로 이동
		if ($month.find('option:selected').is(':disabled')) {
			const $firstEnabled = $month.find('option:not(:disabled)').first();
			if ($firstEnabled.length) $month.val($firstEnabled.val());
		}
	}

	// ===== Day: 선택한 year/month에 따라 과거 일 disable =====
	function fillDays() {
		const y = parseInt($year.val(), 10);
		const m = parseInt($month.val(), 10);
		const max = daysInMonth(y, m);

		const prev = $day.val(); // 기존 선택 유지 시도
		$day.empty();

		for (let d = 1; d <= max; d++) {
			$day.append(`<option value="${pad2(d)}">${pad2(d)}</option>`);
		}

		// 1) 이전 선택이 유효하고 disabled가 아니면 유지
		if (prev && !$day.find(`option[value="${prev}"]`).is(':disabled')) {
			$day.val(prev);
			return;
		}

		// 2) 기본값(내일)이 해당 월에 있으면 선택
		const defVal = pad2(DEF_D);
		if (y === DEF_Y && m === DEF_M && !$day.find(`option[value="${defVal}"]`).is(':disabled')) {
			$day.val(defVal);
			return;
		}

		// 3) 그 외에는 첫 enabled day 선택
		const $firstEnabled = $day.find('option:not(:disabled)').first();
		if ($firstEnabled.length) {
			$day.val($firstEnabled.val());
			return;
		}

		// 4) 만약 enabled day가 하나도 없다면 내일로 강제
		forceDefaultTomorrow();
	}

	// ===== 초기 세팅 =====
	fillYears();
	fillMonths();
	fillDays();
	syncHidden();

	// ===== 변경 이벤트 =====
	$year.on('change', function () {
		fillMonths();
		fillDays();
		syncHidden();
	});

	$month.on('change', function () {
		fillDays();
		syncHidden();
	});

	$day.on('change', function () {
		const y = parseInt($year.val(), 10);
		const m = parseInt($month.val(), 10);
		const d = parseInt($day.val(), 10);

		if (!isSelectableDate(y, m, d)) {
			openError('과거 날짜는 선택할 수 없습니다.', $day);
			forceDefaultTomorrow();
			return;
		}

		syncHidden();
	});

	// ===== submit 검증 =====
	const $form = $('#schForm');
	const $title = $('input[name="title"]');

	$form.on('submit', function (e) {
		const titleVal = ($title.val() || '').trim();
		const dateVal = ($hidden.val() || '').trim();

		if (!titleVal) {
			e.preventDefault();
			openError('제목을 입력해주세요.', $title);
			return;
		}

		if (!dateVal) {
			e.preventDefault();
			openError('일정 날짜를 선택해주세요.', $year);
			return;
		}

		// disabled로 막아놨지만 혹시나 조작 방지용 마지막 검증
		const y = parseInt($year.val(), 10);
		const m = parseInt($month.val(), 10);
		const d = parseInt($day.val(), 10);
		if (!isSelectableDate(y, m, d)) {
			e.preventDefault();
			Modal.open(MODAL_ID, '일정은 미래 날짜만 선택할 수 있어요.');
			forceDefaultTomorrow();
			return;
		}
	});
});
