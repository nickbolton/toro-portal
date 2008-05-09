function byID(elem) { return document.getElementById(elem); }

function saveFragmentLayout(layout) {
	UniconCookieHandler.setCookie('frag_layout',layout,3600);
	return true;
}

function loadFragmentLayout() {
	if(byID('tabPrefsContainerToolbarLayoutContainer')) {
		var fraglay = UniconCookieHandler.getCookie('frag_layout');
		//alert(fraglay);
		if(fraglay) {
			//update layout images
			clearSelectedLayout();
			if(byID(fraglay)) {
				var imgpath = byID(fraglay).src;
				byID(fraglay).src = imgpath.replace(/.gif/,"_selected.gif");
			}
			//update column widths
			var fragColArray = fraglay.split('_');
			var colcounter = 1;
			for(var i=2; i<fragColArray.length; i++) {
				if(fragColArray[i] == 'left') {
					i = i+2;	
				}
				var fragwidth = fragColArray[i];
				//alert(colcounter);
				if(fragwidth == 'even') {
					byID('col'+colcounter).className = 'column-preferences';
				}
				else if(fragwidth == 'wide' || fragwidth == 'narrow') {
					byID('col'+colcounter).className = 'column-preferences-' + fragwidth;
				}
				colcounter++;
			}
		}
	}
}

function deleteFragmentLayout() {
	UniconCookieHandler.deleteCookie('frag_layout');
}

function clearSelectedLayout() {
	var imgArray = document.getElementsByTagName('input');
	for(var i=0; i<imgArray.length; i++) {
		if(imgArray[i].src.indexOf('_selected') > 0) {
			byID(imgArray[i].id).src = imgArray[i].src.replace(/_selected/,"");	
		}
	}	
}

function confirmDeleteColumn() {
	if(confirm('Are you sure you want to remove this column and associated channels?')) {
		deleteFragmentLayout(); 
		return true;
	}
	return false;
}