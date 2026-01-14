$(function () {
	const INFO_MODAL = '#postModal';
	const CONFIRM_MODAL = '#confirmModal';

	// ===== 댓글로 스크롤 =====
	$('#btnScrollComment').on('click', function () {
		const el = document.getElementById('commentSection');
		if (!el) return;
		el.scrollIntoView({ behavior: 'smooth', block: 'start' });
		setTimeout(() => $('#commentContent').trigger('focus'), 250);
	});

	// ===== 댓글 글자수 카운터 + 제한 =====
	const MAX = 300;
	const $comment = $('#commentContent');
	const $counter = $('#commentCounter');
	function updateCounter() {
		let v = ($comment.val() || '');
		if (v.length > MAX) {
			$comment.val(v.substring(0, MAX));
			v = $comment.val();
		}
		$counter.text(v.length + '/' + MAX);
	}
	$comment.on('input', updateCounter);
	updateCounter();

	// ===== 댓글 등록 검증 (빈 내용 방지) =====
	$('#commentForm').on('submit', function (e) {
		const v = ($comment.val() || '').trim();
		if (!v) {
			e.preventDefault();
			Modal.open(INFO_MODAL, '댓글 내용을 입력해 주세요 🙂');
			setTimeout(() => $comment.trigger('focus'), 0);
			return;
		}
	});

	// ===== 게시글 삭제 =====
	$('#btnDeletePost').on('click', function () {
		const postSeq = $(this).data('post-seq');
		if (!postSeq) {
			Modal.open(INFO_MODAL, '삭제할 게시글 정보를 찾지 못했어요.');
			return;
		}

		Modal.open(CONFIRM_MODAL,
			'게시글을 삭제할까요?<br><b>삭제 후 복구할 수 없어요.</b>',
			{
				primaryText: '삭제하기',
				secondaryText: '취소',
				onPrimary: function () {

					const $form = $('<form>', { method: 'post', action: `/deletePost/${postSeq}` });
					const returnUrl = $('#returnUrl').val();
					
					// 이전 목록 URL 전달
					if (returnUrl) {
						$form.append($('<input>', { type: 'hidden', name: 'returnUrl', value: returnUrl }));
					}
					
					$('body').append($form);
					$form.trigger('submit');
				}
			}
		);
	});
});
