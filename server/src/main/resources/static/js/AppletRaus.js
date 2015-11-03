
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Martin Formanko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Main constructor of AppletRaus object.
 *
 * @param aOptions
 * @constructor
 */
var AppletRaus = function(aOptions) {

    /* Default values */

    AppletRaus.NAME = "AppletRausJS";
    AppletRaus.URI_CALL = "appletraus:client";

    AppletRaus.LOGLEVEL = 4;

    /****************************************************************/

    var sFuncname = "AppletRaus::__construct";

    AppletRaus.ERROR = 1;
    AppletRaus.WARN = 2;
    AppletRaus.INFO = 3;
    AppletRaus.DEBUG = 4;

    AppletRaus.MESSAGE_TYPE_GET_AVAILABLE_METHODS = "GET_AVAILABLE_METHODS";
    AppletRaus.MESSAGE_TYPE_METHOD_CALL = "METHOD_CALL";
    AppletRaus.MESSAGE_TYPE_METHOD_RESULT = "METHOD_RESULT";

    //STATUS
    // #1 (1)  bit: Initialized
    // #2 (2)  bit: Waiting for command result
    // #3 (4)  bit: Waiting for AppletRauslication to be executed
    // #4 (8)  bit: Waiting for command return value
    // #5 (16) bit: Waiting for heartbeat response
    // #6 (32) bit: Initialisation error
    // #7 (64) bit: Last method call error

    AppletRaus.STATUS_INITIALIZED = 1;
    AppletRaus.STATUS_WAITING_FOR_INITIALIZE_RESULT = 2;
    AppletRaus.STATUS_WAITING_FOR_APPLETRAUSLICATION = 4;
    AppletRaus.STATUS_WAITING_FOR_RETURN_VALUE = 8;
    AppletRaus.STATUS_WAITING_HEARTBEAT_RESPONSE = 16;
    AppletRaus.STATUS_INITIALISATION_ERROR = 32;
    AppletRaus.STATUS_LAST_METHOD_CALL_ERROR = 64;

    //AppletRaus._MSG_WAITING = 'AppletRaus wird initialisiert.';
    //AppletRaus._MSG_CONNECTING = 'Verbindung zum AppletRaus-Server wird hergestellt.';
    //AppletRaus._MSG_PLEASE_WAIT = 'Bitte warten';
    //AppletRaus._MSG_ERROR_INITIALIZE = 'Fehler bei Initialisierung der AppletRaus. Bitte neu anmelden oder Applets einstellen in eUser.';
    //AppletRaus._MSG_ERROR_DISCONNECT = 'Die AppletRaus Verbindung wurde geschlossen. Bitte neu anmelden oder Applets einstellen in eUser.';
    //AppletRaus._MSG_ERROR_DISCONNECT2 = 'Die AppletRaus Verbindung wurde geschlossen. Aktualisieren Sie die Seite oder melden Sie sich neu an.';
    //AppletRaus._MSG_ERROR_INIT_TIMEOUT = 'Anscheinend ist AppletRaus nicht installiert. Bitte aktivieren Sie im Modul „eUser“ die Option „Applets benutzen“';
    //AppletRaus._MSG_ERROR_NOT_ENABLED = 'AppletRaus wurde nicht geladet. Bitte neu anmelden oder Applets einstellen in eUser.';

    AppletRaus._oInstance = this;
    this.iStatus = 0;

    if ("aClientOptions" in aOptions)
        if ("sDebugLevel" in aOptions.aClientOptions)
            sDebugLevel = aOptions.aClientOptions.sDebugLevel;

    AppletRaus.debugLevel = typeof aOptions.aClientOptions.iDebugLevel !== 'undefined' ? aOptions.aClientOptions.iDebugLevel : AppletRaus.LOGLEVEL;

    _(AppletRaus.DEBUG, sFuncname, "AppletRaus object being created");

    this.aServerOptions = aOptions.aServerOptions;
    var aCO = aOptions.aClientOptions;

    //Required parameters
    Utils.fnArg(this, "sAtmosphereEndpoint", aCO.sAtmosphereEndpoint);
    Utils.fnArg(this, "sAppletJnlpUrl", aCO.sAppletJnlpUrl);

    //Optional parameters
    Utils.fnArg(this, "sAppletTag", aCO.sFallbackApplet, "");

    this.sWindowId = Utils.fnMakeId();

    _(AppletRaus.DEBUG, sFuncname, "AppletRaus object created with aClientOptions:[" + Utils.fnFormatArray(aCO) + "]");

    this.subscribe();

}

AppletRaus.prototype.subscribe = function() {

    var socket = atmosphere;
    var transport = 'websocket';
    var sEndpoint = this.sAtmosphereEndpoint+"?sessionId="+this.sWindowId;
    var oInstance = this;

    var oRequest = {
        url : sEndpoint,
        contentType : "application/json",
        logLevel : 'debug',
        transport : transport,
        trackMessageLength : true,
        reconnectInterval : 5000
    };

    oRequest.onOpen = function(oResponse) {

        console.log('Atmosphere connected using ' + oResponse.transport);
        transport = oResponse.transport;
        oRequest.uuid = oResponse.request.uuid;
        oInstance._initializeSystemApplication();

    };

    oRequest.onClientTimeout = function(r) {

        console.log("Client closed the connection after a timeout. Reconnecting in "+oRequest.reconnectInterval);
        setTimeout(function() {
            AppletRaus.subSocket = socket.subscribe(oRequest);
        }, oRequest.reconnectInterval);

    };

    oRequest.onReopen = function(oResponse) {

        console.log('Atmosphere re-connected using ' + oResponse.transport);

    };

    oRequest.onTransportFailure = function(sErrorMsg, oRequest) {

        atmosphere.util.info(sErrorMsg);
        oRequest.fallbackTransport = "long-polling";
        console.log('Atmosphere Chat. Default transport is WebSocket, fallback is ' + oRequest.fallbackTransport);

    };

    oRequest.onMessage = function(oResponse) {

        var oMessage = oResponse.responseBody;
        try {
            var oMethods = atmosphere.util.parseJSON(oMessage);
        } catch (e) {
            console.log('This doesn\'t look like a valid JSON: ', oMessage);
            return;
        }

        if (oMethods.messageType == AppletRaus.MESSAGE_TYPE_GET_AVAILABLE_METHODS) {
            oInstance._fnAddMethods(oMethods.appletMethodList);
            $('applet').remove();
            document.contentXferApplet = oInstance;
            return;
        }


    };

    oRequest.onClose = function(oResponse) {

        console.log('Server closed the connection after a timeout');

        //if (subSocket) {
        //    subSocket.push(atmosphere.util.stringifyJSON({
        //        message : 'disconnecting'
        //    }));
        //}

    };

    oRequest.onError = function(oResponse) {

        console.log('Sorry, but there\'s some problem with your socket or the server is down');

    };

    oRequest.onReconnect = function(oRequest, oResponse) {

        console.log('Connection lost, trying to reconnect. Trying to reconnect ' + oRequest.reconnectInterval);

    };

    AppletRaus.subSocket = socket.subscribe(oRequest);

//subSocket.push(atmosphere.util.stringifyJSON({
//    sessionId: 100,
//    initializing: true
//}));

//subSocket.push(atmosphere.util.stringifyJSON({
//    author : author,
//    message : msg
//}));

};

/**
 * Simple logging function, when sLevel is lower than value set in AppletRaus, put the line in console
 * @param {int} sLevel Logging level
 * @param {string} sFunction Name of function from which the logging is called
 * @param {string} sLog Log entry
 * @private
 */
function _(sLevel, sFunction, sLog, bAlert) {

    var aLevels = {
        4: "[ DEBUG ]",
        1: "[ ERROR ]",
        3: "[ INFO  ]",
        2: "[ WARN  ]"
    }

    if (sLevel <= AppletRaus.debugLevel) {
        console.log(aLevels[sLevel]+" - "+sFunction+" - "+sLog);
        if (bAlert) alert(aLevels[sLevel]+" - "+sFunction+" - "+sLog);
    }
}

function _exc(sFuncname, sLog) {
    return "[ EXCEPTION ] "+sFuncname+" "+sLog;
}

var Utils = function() {}

/**
 * Every window is assigned with windowId which is generated with this function
 * @returns {string}
 */
Utils.fnMakeId = function() {
    var sText = "";
    var sPossible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < 15; i++)
        sText += sPossible.charAt(Math.floor(Math.random() * sPossible.length));

    return sText;
}

/**
 * Make simple list of associative array. Used for passing arguments to dynamically created functions on AppletRaus object
 * @param {associative array} aInput
 * @returns {array}
 */
Utils.fnArrayValues = function(aInput) {

    var aTmpArr = [],
        sKey = '';

    if (aInput && typeof aInput === 'object' && aInput.change_key_case) { // Duck-type check for our own array()-created PHPJS_Array
        return aInput.values();
    }

    for (sKey in aInput) {
        aTmpArr[aTmpArr.length] = aInput[sKey];
    }

    return aTmpArr;
}

/**
 * Checks if the input parameters are of correct types, just to be sure that the AppletRaus object is not given incorrect
 * values in options array
 * @param oTarget
 * @param {string} sName    Name of the property, that will be set on object oTarget
 * @param {aarray} oArg     Argument that needs to be checked
 * @param {string} cls      Class name of this argument
 * @param {object} oDefault Default value, if the input parameter is not set
 */
Utils.fnArg = function(oTarget, sName, oArg, oDefault) {

    var sFunc = "Utils::fnArg";
    if (typeof oArg !== 'undefined') {
        var oTypes = {
            "b": ["boolean", function(oArg) { return typeof(oArg) == "boolean"; }],
            "i": ["number", function(oArg) { return typeof(oArg) == "number"; }],
            "s": ["string", function(oArg) { return typeof(oArg) == "string"; }],
            "o": ["object", function(oArg) { return typeof(oArg) == "object"; }],
            "fn": ["function", function(oArg) { return typeof(oArg) == 'function' }]
        };
        var sType = sName.replace(/^(fn|i|b|s|o)[A-Z].+$/, "$1");
        if (sType in oTypes) {
            if (oTypes[sType][1](oArg)) {
                oTarget[sName] = oArg;
            } else {
                _(AppletRaus.ERROR, sFunc, "Value of " + sName + " must be of type " + oTypes[sType][0], true);
                throw _exc(sFunc, "Value of " + sName + " must be of type " + oTypes[sType][0]);
            }
        } else {
            _(AppletRaus.ERROR, sFunc, "Unknown type of argument while initializing: "+sName);
            throw _exc(sFunc, "Unknown type of argument while initializing: "+sName);
        }
    } else {
        if (oDefault === undefined) {
            _(AppletRaus.ERROR, sFunc, "Parameter "+sName+" is required in initialization array for AppletRaus object", true);
            throw _exc(sFunc, "Parameter "+sName+" required");
        } else {
            oTarget[sName] = oDefault;
        }
    }

}

/**
 * Used just for debugging purposes
 * @param {array} aArray
 * @returns {string}
 */
Utils.fnFormatArray = function(aArray) {
    var sResult = "";
    for (sEntry in aArray)
        sResult += sEntry+": "+aArray[sEntry]+", ";
    return sResult;
}

/**
 * Sends initialization command to AppletRaus server.
 */
AppletRaus.prototype.initialize = function() {
    var sFuncname = "AppletRaus::initialize";

    _(AppletRaus.DEBUG, sFuncname, "AppletRaus object being initialized");
    _(AppletRaus.DEBUG, sFuncname, "Generated windowId: " + this.sWindowId);

    var jsonRequest = {
        cookie: this.sCookie,
        parameters: this.aServerOptions,
        type: AppletRaus.MESSAGE_TYPE_INITIALIZE
    }

    $(this.oTestbed).trigger(Testbed.TRIGGER_NAME, Testbed.TEST_INITIALIZE_REQUEST_PRESEND);
    Utils.fnSendJson(this, jsonRequest);

}

/**
 * Dynamically adds available public methods of the applet
 * @param {array} aMethods
 * @private
 */
AppletRaus.prototype._fnAddMethods = function(aMethods) {

    this._iRetries = 0;

    for (i in aMethods) {
        _(AppletRaus.DEBUG, "AppletRaus::_fnAddMethods", "Dynamically adding method " + aMethods[i].name + " to AppletRaus Class");
        AppletRaus.prototype[aMethods[i].name] = AppletRaus.createMethodFunction(aMethods[i].name);
    }

}

/**
 * Closure for method invocation
 * @param sCommand
 * @returns {Function}
 */
AppletRaus.createMethodFunction = function(sCommand) {
    return function() { return this.invoke(sCommand, Utils.fnArrayValues(arguments)); }
}

/**
 * Call dynamic method on AppletRaus object. This function is called indirectly from clients
 * @param sFunctionName - Name of the function that should be identical to the property "name" of each Annotation in AppletRaus code
 * @param aParams - Respective arguments
 * @returns {Object}
 */
AppletRaus.prototype.invoke = function(sFunctionName, aParams) {
    var sLogName = "AppletRaus::invoke(sFunctionName="+sFunctionName+", aParams=["+aParams+"])";

    _(AppletRaus.DEBUG, sLogName, "Invoking method to AppletRaus application")

    var jsonRequest = {
        method: sFunctionName,
        parameters: aParams
    }

    var sResult = $.ajax({
        type: 'POST',
        url: '/appletraus-boot/methodInvoke/'+this.sWindowId,
        data: JSON.stringify(jsonRequest),
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        dataType: 'json',
        async: false
    }).responseText;

    _(AppletRaus.DEBUG, sLogName, "Invoke method ends");
    return sResult;

}

AppletRaus.prototype._initializeSystemApplication = function() {

    var sLink = "/appletraus-boot/Applet.jnlp?cookie="+document.cookie+"&sessionId="+this.sWindowId;
    _(AppletRaus.DEBUG, "asdasdas", sLink);

    if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
        //CHROME
        window.location.assign(sLink);
    } else {
        //FIREFOX+IE+OPERA
        $('body').append('<iframe style="display: none" src="' + sLink + '">');
    }

}

var oAppletRaus = new AppletRaus({
    aClientOptions: {
        iDebugLevel: 4,
        sAppletJnlpUrl: "/appletraus-boot/Applet.jnlp",
        sAtmosphereEndpoint: "/appletraus-boot/appletraus"
    }
});


