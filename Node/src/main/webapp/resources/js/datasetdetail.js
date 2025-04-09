$(document).ready(function() {
	$("#toggleFakeButton").hover(
		function () {
			$(this).addClass("ui-state-hover");
		},
		function () {
			$(this).removeClass("ui-state-hover");
		}
	);
	
	$("#toggleFakeButton").click(function() {
		var jqThis = $(this);
		// all accordion panel tabs are expanded and will be collapsed
		if( jqThis.hasClass("expanded") ) {
			$(".ui-accordion-header").each( function( i ) {
				PF('accPanelWidget').unselect(i);
				$(this).attr("title", jqThis.attr("data-expand"));
			});
			jqThis.removeClass("expanded");
			jqThis.addClass("collapsed");
			jqThis.text(jqThis.attr("data-expandAll"));
		}
		// all accordion panel tabs are collapsed and will be expanded
		else {
			$(".ui-accordion-header").each( function( i ) {
				PF('accPanelWidget').select(i);
				$(this).attr("title", jqThis.attr("data-collapse"));
			});
			jqThis.removeClass("collapsed");
			jqThis.addClass("expanded");
			jqThis.text(jqThis.attr("data-collapseAll"));
		}
	});
});

$(document).ready(function() {
	$("#backButton").hover(
		function () {
			$(this).addClass("ui-state-hover");
		},
		function () {
			$(this).removeClass("ui-state-hover");
		}
	);
});

$(document).ready(function() {
	$("#closeButton").hover(
		function () {
			$(this).addClass("ui-state-hover");
		},
		function () {
			$(this).removeClass("ui-state-hover");
		}
	);
});