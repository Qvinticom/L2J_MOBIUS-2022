$(window).on('load', function() {
	"use strict";
});
var $document = $(document);
$document.ready(function() {
	$document.on('click', '.stat-1', function(e) {
		e.preventDefault();
		var statLink = $(this).attr('data-target');
		window.location = statLink;
	});
	$document.on('click', '.stat-2', function(e) {
		e.preventDefault();
		var statLinkS = $(this).attr('data-target');
		window.location = statLinkS;
	});
	$document.on('click', '.langs > a', function(e) {
		$.cookie('lang', $(this).data('lang'), {
			expires: 365,
			path: '/'
		});
		location.reload();
	});
});