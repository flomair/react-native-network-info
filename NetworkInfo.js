'use strict';
import {NativeModules} from 'react-native';


const getSSID = NativeModules.RNNetworkInfo.getSSID;
const getIPAddress = NativeModules.RNNetworkInfo.getIPAddress;
const getBroadcastAddress = NativeModules.RNNetworkInfo.getBroadcastAddress;





export default  {
    SSID(){
        return new Promise((resolve) => {
            getSSID(info => {
                resolve(info)
            });
        })
    },

    IP(){
        return new Promise((resolve) => {
            getIPAddress(info => {
                resolve(info)
            });
        })
    },

    broadcastIP(){
        return new Promise((resolve) => {
            getBroadcastAddress(info => {
                resolve(info)
            });
        })
    },

    async all () {
        const [ssid,ip,broadcastIP] = await Promise.all([this.SSID(),this.IP(),this.broadcastIP()])
        return{
            ssid,ip,broadcastIP
        }
    }
}