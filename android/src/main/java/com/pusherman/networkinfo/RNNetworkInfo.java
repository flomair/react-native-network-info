package com.pusherman.networkinfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.CellLocation;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Map;

public class RNNetworkInfo extends ReactContextBaseJavaModule {
  WifiManager wifi;
  GsmCellLocation cell;
  TelephonyManager telephonyManager;
  Integer dbm;
  ReactApplicationContext globalReactContext;
  public static final String TAG = "RNNetworkInfo";

  public RNNetworkInfo(ReactApplicationContext reactContext) {
    super(reactContext);
    globalReactContext = reactContext;
    wifi = (WifiManager)reactContext.getSystemService(Context.WIFI_SERVICE);
  }

  @Override
  public String getName() {
    return TAG;
  }

  @ReactMethod
  public void getSSID(final Callback callback) {
    WifiInfo info = wifi.getConnectionInfo();
    
    // This value should be wrapped in double quotes, so we need to unwrap it.
    String ssid = info.getSSID();
    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
      ssid = ssid.substring(1, ssid.length() - 1);
    }
    
    callback.invoke(ssid);
  }

  @ReactMethod
  public void getCID(final Callback callback){
     telephonyManager = (TelephonyManager) globalReactContext.getSystemService(Context.TELEPHONY_SERVICE);
     cell = (GsmCellLocation) telephonyManager.getCellLocation();
    callback.invoke((cell.getCid() & 0xffff), (cell.getLac() & 0xffff));
  }

  @ReactMethod
    public void getNetworkType(final Callback callback){
       telephonyManager = (TelephonyManager) globalReactContext.getSystemService(Context.TELEPHONY_SERVICE);
           int networkType = telephonyManager.getNetworkType();
           String networkTypeString;
           switch (networkType) {
               case TelephonyManager.NETWORK_TYPE_GPRS:
                    networkTypeString = "gprs";
                    break;
               case TelephonyManager.NETWORK_TYPE_EDGE:
               case TelephonyManager.NETWORK_TYPE_CDMA:
               case TelephonyManager.NETWORK_TYPE_1xRTT:
               case TelephonyManager.NETWORK_TYPE_IDEN:
                   networkTypeString = "edge";
                   break;
               case TelephonyManager.NETWORK_TYPE_UMTS:
               case TelephonyManager.NETWORK_TYPE_EVDO_0:
               case TelephonyManager.NETWORK_TYPE_EVDO_A:
               case TelephonyManager.NETWORK_TYPE_HSDPA:
                     networkTypeString = "hsdpa";
                     break;
               case TelephonyManager.NETWORK_TYPE_HSPA:
                    networkTypeString = "hspa";
                    break;
               case TelephonyManager.NETWORK_TYPE_HSUPA:
               case TelephonyManager.NETWORK_TYPE_EVDO_B:
               case TelephonyManager.NETWORK_TYPE_EHRPD:
               case TelephonyManager.NETWORK_TYPE_HSPAP:
                   networkTypeString = "hspa+";
                   break;
               case TelephonyManager.NETWORK_TYPE_LTE:
                   networkTypeString = "lte";
                   break;
               default:
                   networkTypeString = "not found";
                   }
      callback.invoke(networkTypeString);
    }


  @ReactMethod
  public void getCarrierName(final Callback callback){
     telephonyManager = (TelephonyManager) globalReactContext.getSystemService(Context.TELEPHONY_SERVICE);
     String carrierName = telephonyManager.getNetworkOperatorName();
    callback.invoke(carrierName);
  }

 @ReactMethod
  public void getSignalStrength(final Callback callback){
   try {
        telephonyManager = (TelephonyManager) globalReactContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getAllCellInfo()!=null && telephonyManager.getAllCellInfo().size() > 0){
        if(telephonyManager.getAllCellInfo().get(0) instanceof CellInfoWcdma){
            CellInfoWcdma cellinfogsm = (CellInfoWcdma)telephonyManager.getAllCellInfo().get(0);
            dbm = cellinfogsm.getCellSignalStrength().getDbm();
        }
          if(telephonyManager.getAllCellInfo().get(0) instanceof CellInfoGsm){
                    CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
                    dbm = cellinfogsm.getCellSignalStrength().getDbm();
          }
           if(telephonyManager.getAllCellInfo().get(0) instanceof CellInfoCdma){
                              CellInfoCdma cellinfogsm = (CellInfoCdma)telephonyManager.getAllCellInfo().get(0);
                              dbm = cellinfogsm.getCellSignalStrength().getDbm();
                    }
              if(telephonyManager.getAllCellInfo().get(0) instanceof CellInfoLte){
                                         CellInfoLte cellinfogsm = (CellInfoLte)telephonyManager.getAllCellInfo().get(0);
                                         dbm = cellinfogsm.getCellSignalStrength().getDbm();
                               }
        callback.invoke(dbm);
        }
        else{
            callback.invoke(-99);
        }
    } catch (NullPointerException e) {
         callback.invoke(-99);
     }
  }

  @ReactMethod
  public void getIPAddress(final Callback callback) {
    WifiInfo info = wifi.getConnectionInfo();

    // The following is courtesy of Digital Rounin at
    //   http://stackoverflow.com/a/18638588 .

    // The endian-ness of `ip` is potentially varying, but we need it to be big-
    // endian.
    int ip = info.getIpAddress();

    // Convert little-endian to big-endian if needed.
    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
        ip = Integer.reverseBytes(ip);
    }

    // Now that the value is guaranteed to be big-endian, we can convert it to
    // an array whose first element is the high byte.
    byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

    String ipAddressString;
    try {
        // `getByAddress()` wants network byte-order, aka big-endian. 
        // Good thing we planned ahead!
        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
    } catch (UnknownHostException ex) {
        Log.e(TAG, "Unable to determine IP address.");
        ipAddressString = null;
    }
    
    callback.invoke(ipAddressString);
  }

}
