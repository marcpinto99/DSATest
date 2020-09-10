package com.example.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {
    public String siteID;
    public int udpSenderPortNum;
    public int udpReceiverPortNum;
    public String ipAddress;

    public Host(String siteID2, int udpSenderPortNum2, int udpReceiverPortNum2, String ipAddress2){
        this.siteID = siteID2;
        this.udpSenderPortNum = udpSenderPortNum2;
        this.udpReceiverPortNum = udpReceiverPortNum2;
        this.ipAddress = ipAddress2;
    }

    public Host() {
        this.siteID = "";
        this.udpSenderPortNum = 0;
        this.udpReceiverPortNum = 0;
        this.ipAddress = "";
    }

    
}
 
