'use strict'

var FBGraph = require("NativeModules").FacebookGraphModule;

var fbGraph = {
	search(type, url, keyword, lat, lng, distance, onCallback) {
		FBGraph.search(type, url, keyword,lat,lng,distance,onCallback)
	},
};
module.exports = fbGraph;