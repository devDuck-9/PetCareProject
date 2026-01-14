$(function(){
	const MODAL_INFO = '#schModal';

	const $form = $('#schEditForm');
	const $year = $('#schYear');
	const $month = $('#schMonth');
	const $day = $('#schDay');
	const $hidden = $('#schDateHidden');
	const $title = $('input[name="title"]');

	function pad2(n){ return String(n).padStart(2,'0'); }
	function daysInMonth(y,m){ return new Date(y,m,0).getDate(); }

	// DB에서 가져온 초기날짜 (yyyy-MM-dd)
	const initial = ($form.data('initial-date') || '').toString().trim();
	const initParts = initial.split('-');

	const now = new Date();
	const curY = now.getFullYear();

	const initY = parseInt(initParts[0] || curY, 10);
	const initM = parseInt(initParts[1] || (now.getMonth()+1), 10);
	const initD = parseInt(initParts[2] || now.getDate(), 10);

	function fillYears(){
		const start = curY - 1;
		const end = curY + 5;
		$year.empty();
		for(let y=start; y<=end; y++){
			$year.append(`<option value="${y}">${y}</option>`);
		}
	}

	function fillMonths(){
		$month.empty();
		for(let m=1; m<=12; m++){
			$month.append(`<option value="${pad2(m)}">${pad2(m)}</option>`);
		}
	}

	function fillDays(){
		const y = parseInt($year.val(),10);
		const m = parseInt($month.val(),10);
		const max = daysInMonth(y,m);
		const prev = $day.val();
		$day.empty();
		for(let d=1; d<=max; d++){
			$day.append(`<option value="${pad2(d)}">${pad2(d)}</option>`);
		}
		if (prev) $day.val(prev);
	}

	function syncHidden(){
		const y = $year.val();
		const m = $month.val();
		const d = $day.val();
		$hidden.val(y && m && d ? `${y}-${m}-${d}` : '');
	}

	// 초기화
	fillYears();
	fillMonths();
	$year.val(String(initY));
	$month.val(pad2(initM));
	fillDays();
	$day.val(pad2(initD));
	syncHidden();

	$year.on('change', function(){ fillDays(); syncHidden(); });
	$month.on('change', function(){ fillDays(); syncHidden(); });
	$day.on('change', syncHidden);
	
	// 유효성 검사 (날짜 제약 없음)
	$form.on('submit', function(e){
		const t = ($title.val() || '').trim();
		if (!t){
			e.preventDefault();
			Modal.open(MODAL_INFO, '제목을 입력해주세요. ✍️');
			$title.trigger('focus');
			return;
		}
		if (!($hidden.val() || '').trim()){
			e.preventDefault();
			Modal.open(MODAL_INFO, '일정 날짜를 선택해주세요. 📅');
			$year.trigger('focus');
			return;
		}
	});
	
	// 수정 취소 버튼 → 모달 확인
	$('#btnCancle').on('click', function (e) {
		e.preventDefault();
		
		const href = $(this).attr('href');
		
		Modal.open('#scheduleCancleModal', '정말 취소하시겠습니까?<br><span style="opacity:.8">작성했던 정보가 사라집니다.</span>', {
			primaryText: '돌아가기',
			secondaryText: '취소'
		});
		// actionModal의 primary 버튼
		$('#scheduleCancleModal [data-modal-primary]').off('click.petCancel').on('click.petCancel', function () {
			window.location.href = href;
		});
		
	});
	
});
