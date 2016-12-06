package com.sun.demo;

import java.util.List;

public class Main{
	
	public static void main(String[] args) {
		GetDevices devices = new GetDevices();
		List<String> udidList = devices.getDevicesUdid();
    	for(String udid: udidList) {
    		new Thread(new Installer(udid)).start();
    	}
	}
}
