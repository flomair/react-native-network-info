'use strict';

var RNNetworkInfo = require('react-native').NativeModules.RNNetworkInfo;

var NetworkInfo = {
  getSSID(ssid) {
    RNNetworkInfo.getSSID(ssid);
  },

  getIPAddress(ip) {
    RNNetworkInfo.getIPAddress(ip);
  },

  getCID(cid) {
    RNNetworkInfo.getCID(cid);
  }
};

module.exports = NetworkInfo;
