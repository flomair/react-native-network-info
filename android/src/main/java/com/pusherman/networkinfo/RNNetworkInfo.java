package com.pusherman.networkinfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.Map;

public class RNNetworkInfo extends ReactContextBaseJavaModule {
    WifiManager wifi;

    public static final String TAG = "RNNetworkInfo";

    public RNNetworkInfo(ReactApplicationContext reactContext) {
        super(reactContext);

        wifi = (WifiManager) reactContext.getSystemService(Context.WIFI_SERVICE);
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

    @ReactMethod
    public void getBroadcastAddress(final Callback callback) {
        //String found_bcast_address = getBroadcastInner();
        System.setProperty("java.net.preferIPv4Stack", "true");
        InetAddress broadcastAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();

            while (broadcastAddress == null && networkInterface.hasMoreElements()) {
                NetworkInterface singleInterface = networkInterface.nextElement();
                String interfaceName = singleInterface.getName();
                if (interfaceName.contains("wlan0")|| interfaceName.contains("eth0")) {
                    for (InterfaceAddress infaceAddress : singleInterface.getInterfaceAddresses()) {
                        broadcastAddress = infaceAddress.getBroadcast();
                        if (broadcastAddress != null) {
                            break;
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        String found_bcast_address = "255.255.255.255" ;
        if(broadcastAddress != null){
             found_bcast_address = broadcastAddress.toString().replace("/", "");
        }else{
             found_bcast_address = getBroadcastInner();
        }
        callback.invoke(found_bcast_address);
    }

    @ReactMethod
    public void getBroadcast(final Callback callback) {
        DhcpInfo myDhcpInfo = wifi.getDhcpInfo();
        String ipAddressString;
        try {
            int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask) | ~myDhcpInfo.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            ipAddressString = InetAddress.getByAddress(quads).getHostAddress();

        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to determine IP address.");
            ipAddressString = "255.255.255.255";
        }
        callback.invoke(ipAddressString);
  /*
    DhcpInfo dhcp = wifi.getDhcpInfo();
    // handle null somehow



    int ip = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;

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
    */
    }

    private String getBroadcastInner() {
        DhcpInfo myDhcpInfo = wifi.getDhcpInfo();
        String ipAddressString;
        try {
            int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask) | ~myDhcpInfo.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            ipAddressString = InetAddress.getByAddress(quads).getHostAddress();

        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to determine IP address.");
            ipAddressString = "255.255.255.255";
        }
        return ipAddressString;
    }
}
