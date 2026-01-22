$(function () {
	const errorMsg = ($('#loginErrorMsg').val() || '').trim();

	function openIfError() {
		if (!errorMsg) return;
		Modal.bind('#loginFailModal');
		Modal.open('#loginFailModal', errorMsg);
	}

	openIfError();

	window.addEventListener('pageshow', function () {
		openIfError();
	});
});