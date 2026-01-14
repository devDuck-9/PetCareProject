$(function(){
	const MODAL_CONFIRM = '#confirmModal';

	// 상태 뱃지
	const $status = $('.sd-status');
	if ($status.length) {
		const st = ($status.data('status') || '').toString();
		$status.attr('data-status', st);
		if (st === 'DONE') $status.text('완료 ✅');
		if (st === 'CANCELED') $status.text('취소 ❌');
		if (st === 'PLANNED') $status.text('예정 🕒');
	}

	// 삭제 확인 → POST 전송
	$('#btnDeleteSchedule').on('click', function(){
		const scheduleSeq = $(this).data('schedule-seq');
		if (!scheduleSeq) return;

		Modal.open(MODAL_CONFIRM, '일정을 삭제할까요?<br><span style="opacity:.8">삭제 후 복구할 수 없어요.</span>',{
				primaryText: '삭제하기',
				secondaryText: '취소',
				onPrimary: function(){
					const $f = $('<form>', { method: 'post', action: `/deleteSchedule/${scheduleSeq}` });
					$(document.body).append($f);
					$f.trigger('submit');
				}
			}
		);
	});
});
