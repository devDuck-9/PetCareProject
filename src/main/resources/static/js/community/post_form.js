$(function () {
	const MODAL_ID = '#postModal';

	const $form = $('#postForm');
	const $category = $('#categorySelect');
	const $title = $('#title');
	const $content = $('#content');

	function openError(msg, $focusEl) {
		Modal.open(MODAL_ID, msg);
		setTimeout(function () {
			if ($focusEl && $focusEl.length) $focusEl.trigger('focus');
		}, 0);
	}

	$form.on('submit', function (e) {
		const cat = ($category.val() || '').trim();
		const title = ($title.val() || '').trim();
		const content = ($content.val() || '').trim();

		if (!cat) {
			e.preventDefault();
			openError('카테고리를 선택해주세요.', $category);
			return;
		}

		if (!title) {
			e.preventDefault();
			openError('제목을 입력해주세요.', $title);
			return;
		}

		if (!content) {
			e.preventDefault();
			openError('본문을 입력해주세요.', $content);
			return;
		}
	});
});