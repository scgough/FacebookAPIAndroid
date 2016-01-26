'use strict'

var FBLogin = require("NativeModules").FacebookLoginModule;

var fbLogin = {
	loginWithReadPermissions(permissions, onCallback) {
		FBLogin.loginWithReadPermissions(permissions,onCallback)
	},

	loginWithPublishPermissions(permissions, onCallback) {
		FBLogin.loginWithPublishPermissions(permissions,onCallback)
	},

	logout(onCallback) {
		FBLogin.logout(onCallback)
	},

	getCurrentToken(onCallback) {
		FBLogin.getCurrentToken(onCallback)
	},
};
module.exports = fbLogin;