$(function () {
	// 공통 모달 바인딩
	Modal.bind('#petDeleteModal');

	// 삭제 버튼 → 모달 확인 → submit
	$('#btnDelete').on('click', function () {
		Modal.open('#petDeleteModal', '정말 삭제할까요?<br>삭제 후에는 되돌릴 수 없습니다.', {
			primaryText: '삭제',
			secondaryText: '취소',
			onPrimary: () => $('#deleteForm').submit()
		});
	});
	
});
