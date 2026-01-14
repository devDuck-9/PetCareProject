$(function () {
	// 정렬 변경 시 즉시 검색
	const $form = $('#postSearchForm');
	const $sort = $('#sortSelect');
	const $q = $('#q');

	$sort.on('change', function () {
		// page는 검색 시 1로 초기화
		// (form에 page가 없으니 그냥 submit)
		$form.trigger('submit');
	});

	// Enter로 검색할 때 공백만 들어가면 제거
	$form.on('submit', function () {
		const v = ($q.val() || '').trim();
		$q.val(v);
	});
});
