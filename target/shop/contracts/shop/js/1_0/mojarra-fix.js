/* eslint max-len: [1, 120] */
// http://java.net/jira/browse/JAVASERVERFACES_SPEC_PUBLIC-1024
// http://balusc.blogspot.de/2011/09/communication-in-jsf-20.html#AutomaticallyFixMissingJSFViewStateAfterAjaxRendering
// http://showcase.omnifaces.org/scripts/FixViewState

/* global jsf: false */
jsf.ajax.addOnEvent(function(data) {
    "use strict";
    if (data.status === "success") {
        var viewState = getViewState(data.responseXML);

        for (var i = 0; i < document.forms.length; i++) {
            var form = document.forms[i];

            /* eslint max-depth: [1, 2] */
            if (form.method === "post" && !hasViewState(form)) {
                createViewState(form, viewState);
            }
        }
    }
});

function getViewState(responseXML) {
    "use strict";
    var updates = responseXML.getElementsByTagName("update");

    for (var i = 0; i < updates.length; i++) {
        // jscs:disable
        if (updates[i].getAttribute("id").match(/^([\w]+:)?javax\.faces\.ViewState(:[0-9]+)?$/)) {
            return updates[i].firstChild.nodeValue;
        }
        // jscs:enable
    }

    return null;
}

function hasViewState(form) {
    "use strict";
    for (var i = 0; i < form.elements.length; i++) {
        if (form.elements[i].name === "javax.faces.ViewState") {
            return true;
        }
    }

    return false;
}

function createViewState(form, viewState) {
    "use strict";
    var hidden;

    /* jshint ignore:start */
    try {
        // IE 6-8
        /* eslint quotes: [0, "double"] */
        /* jscs:disable validateQuoteMarks */
        hidden = document.createElement("<input name='javax.faces.ViewState'>"); // IE6-8.
    } catch (e) {
        hidden = document.createElement("input");
        hidden.setAttribute("name", "javax.faces.ViewState");
    }
    /* jshint ignore:end */

    hidden.setAttribute("type", "hidden");
    hidden.setAttribute("value", viewState);
    hidden.setAttribute("autocomplete", "off");
    form.appendChild(hidden);
}
