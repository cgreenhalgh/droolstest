// Bluetoothex client Javascript
var req;

function makeResponseCallback(id) {
	return function () {
//	alert("callback");
	//alert(table);
	//alert("callback for "+id+": "+req.readyState);
 	   if (req.readyState == 4) {
 	   		//alert("callback for "+id+", ready, "+req.status);
    		var response = document.getElementById(id);
			if (req.status == 200) {
				response.value = req.responseText;
         	}
         	else {
         		response.value = "Error code "+req.status;
         	}
    	}
	}
}

function newXMLHttpRequest() {
	if (typeof XMLHttpRequest != "undefined") {
		return new XMLHttpRequest();
  	} else if (window.ActiveXObject) {
    	return new ActiveXObject("Microsoft.XMLHTTP");
   	}
	return null;
}

function submit(httpOperation, path, requestId, responseId) {
	if (typeof XMLHttpRequest != "undefined") {
		req = new XMLHttpRequest();
  	} else if (window.ActiveXObject) {
    	req = new ActiveXObject("Microsoft.XMLHTTP");
   	}
//	alert("get "+req);
  
   	req.open(httpOperation, path, true);
	req.onreadystatechange = makeResponseCallback(responseId);
   	var response = document.getElementById(responseId);
   	response.value = "Send "+httpOperation+" request to "+path+"...";   
  	if (requestId!=null) {
  		var myRequest = document.getElementById(requestId).value;
		req.send(myRequest);
	}
	else
		req.send();	
}

function addFacts() {
//	submit("POST", "../rawfacts", "myRequest", "myResult");
}
function getFacts() {
	var sessionId = document.getElementById("sessionId").value;
	if (sessionId==null || sessionId=="") {
		alert("Please set sessionId");
		return;
	}
	submit("GET", "../../1/sessions/"+sessionId+"/rawfacts", null, "myFacts");
}

// load initial facts
function initFacts() {
	var freq = newXMLHttpRequest();
	// sync
	freq.open("GET", "initial_facts.xml", false);
	// avoid default IE forever-cacheing
	freq.setRequestHeader("Pragma", "Cache-Control: no-cache");
	freq.send();
	var facts = freq.responseText;
	
	var areq = newXMLHttpRequest();
	var sessionId = document.getElementById("sessionId").value;
	areq.open("POST", "../../1/sessions/"+sessionId+"/rawfacts", false);
	areq.send(facts);
}

function getRegions() {
	var ureq = newXMLHttpRequest();
	var sessionId = document.getElementById("sessionId").value;
	// sync
	ureq.open("GET", "../../1/sessions/"+sessionId+"/rawfacts", false);
	ureq.send();
	// MS-specific
	if (window.ActiveXObject) {
		ureq.responseXML.setProperty("SelectionLanguage", "XPath");
	}
	// MS-specific - see http://www.wrox.com/WileyCDA/Section/id-291861.html
	var users = ureq.responseXML.documentElement.selectNodes("//fact[@class='uk.ac.horizon.ug.samples.bluetoothex.Region']");
	/* Search the document for all h2 elements.    
	 * The result will likely be an unordered node iterator. */  
	var alertText = "Regions: ";  
	var i;
	for (i=0; i<users.length; i++) {
		var user = users.item(i);
		alertText += user.getElementsByTagName("id").item(0).firstChild.nodeValue+" ";
	}   
	alert(alertText); // Alerts the text of all h2 elements  
}