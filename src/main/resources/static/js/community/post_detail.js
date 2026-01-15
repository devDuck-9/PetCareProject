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
	
	// ===== 댓글 삭제 =====
	$(document).on('click', '[data-comment-delete]', function () {
		const commentSeq = $(this).attr('data-comment-seq');
		const postSeq = $('input[name="postSeq"]').val(); // 댓글폼 hidden postSeq
		if (!commentSeq || !postSeq) return;

		Modal.open(
			'#confirmModal',
			'정말 이 댓글을 삭제할까요?<br>삭제하시면 복구할 수 없습니다.',
			{
				primaryText: '삭제',
				secondaryText: '취소',
				onPrimary: function () {
					const $form = $('<form>', { method: 'post', action: '/deleteComment' });
					$form.append($('<input>', { type: 'hidden', name: 'commentSeq', value: commentSeq }));
					$form.append($('<input>', { type: 'hidden', name: 'postSeq', value: postSeq }));
					$('body').append($form);
					$form.trigger('submit');
				}
			}
		);
	});
	
	// ===== 댓글 인라인 수정 (액션 자리 토글) =====
	$(document).on('click', '[data-comment-edit]', function () {
		const $btn = $(this);
		const commentSeq = $btn.attr('data-comment-seq');
		const $item = $btn.closest('.pc-item');
		const $body = $item.find('[data-comment-body]');
		const $actionsView = $item.find('.pc-actions-view');

		if (!commentSeq || $body.length === 0) return;

		if ($item.data('editing') === true) return;
		$item.data('editing', true);

		const originalText = $body.text().trim();

		// textarea editor (본문 자리)
		const $editor = $(`
			<div class="pc-editbox" style="margin-top:8px;">
				<textarea class="pc-textarea" rows="3" style="width:100%;"></textarea>
			</div>
		`);
		$editor.find('textarea').val(originalText);

		// 저장/취소 (액션 자리)
		const $actionsEdit = $(`
			<div class="pc-item-actions pc-actions-edit" style="display:flex; gap:10px; justify-content:flex-end;">
				<button type="button" class="btn-pill-orange" data-comment-save>저장</button>
				<button type="button" class="btn-pill" data-comment-cancel>취소</button>
			</div>
		`);

		// UI 토글
		$body.hide();
		$editor.insertAfter($body);

		if ($actionsView.length) {
			$actionsView.hide();
			$actionsEdit.insertAfter($actionsView);
		} else {
			// 혹시 액션이 없으면 editor 아래에라도
			$actionsEdit.insertAfter($editor);
		}

		// 취소 (확인 모달 띄우기)
		$actionsEdit.on('click', '[data-comment-cancel]', function () {

			Modal.open('#confirmModal', '수정을 취소할까요?<br>작성 중인 내용이 사라집니다.', {
				primaryText: '취소하기',
				secondaryText: '계속 작성',
				onPrimary: function () {
					// 여기서만 실제 취소 실행
					$actionsEdit.remove();
					$actionsView.show();
					$editor.remove();
					$body.show();
					$item.data('editing', false);
				}
			});

		});

		// 저장 (AJAX)
		$actionsEdit.on('click', '[data-comment-save]', function () {
			const newText = $editor.find('textarea').val().trim();
			if (!newText) {
				Modal.open('#postModal', '댓글 내용을 입력해주세요 🙂');
				return;
			}

			$.ajax({
				url: '/api/comment/update',
				type: 'POST',
				data: { commentSeq: commentSeq, content: newText },
				success: function (res) {
					if (res && res.ok) {
						$body.text(res.content);

						$actionsEdit.remove();
						$actionsView.show();
						$editor.remove();
						$body.show();
						$item.data('editing', false);

						Modal.open('#postModal', '댓글이 수정되었습니다 ✅');
					} else {
						Modal.open('#postModal', (res && res.msg) ? res.msg : '수정 실패');
					}
				},
				error: function (xhr) {
					if (xhr.status === 401) {
						Modal.open('#postModal', '로그인이 필요합니다.');
					} else if (xhr.status === 403) {
						Modal.open('#postModal', '수정 권한이 없습니다.');
					} else {
						Modal.open('#postModal', '오류가 발생했습니다.');
					}
				}
			});
		});
	});
	
});
