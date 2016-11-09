package com.pusherman.networkinfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.CellLocation;
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
    callback.invoke(cell.getCid() & 0xffff);
  }

  @ReactMethod
  public void getCarrierName(final Callback callback){
     telephonyManager = (TelephonyManager) globalReactContext.getSystemService(Context.TELEPHONY_SERVICE);
     String carrierName = telephonyManager.getNetworkOperatorName();
    callback.invoke(carrierName);
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
