package com.sun.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 * 解析移动设备info
 * @author sun
 *
 */
public class GetDevices {	
	private static String result = "";
	public static Logger log = Logger.getLogger(GetDevices.class);
	
	/**
	 * 拿到设备名称和系统版本
	 */
	public void getDeviceInfo() {
		List<String> udidList = getDevicesUdid();
    	for(String udid: udidList) {
	        log.info("设备名称：" + getResult(udid, "ro.product.model"));
	        log.info("系统版本：" + getResult(udid, "ro.build.version.release") + "\n");
    	}
	}
	
	/**
	 * 从getprop筛选设备名称和系统版本
	 * @param udid
	 * @param field
	 * @return
	 */
    public String getResult(String udid, String field) {
        List<String> list = getProp(udid);
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).contains(field)) {
            	String temp = list.get(i).split(":")[1];
            	result = temp.replace(" [", "").replace("]", "");   
            }       
        }
        return result;
    }
 
    /**
     * 执行adb shell getprop拿到设备详细信息
     * @param udid
     * @return
     */
    private List<String> getProp(String udid) {
        List<String> list = new LinkedList<String>();
        String line = "";  
        String command = "adb -s " + udid + " shell getprop";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));   
            while ((line = reader.readLine()) != null) {
                list.add(line.toString());
            }
            process.waitFor();
         
        } catch (IOException | InterruptedException e) {
            e.getMessage();
        }
        
        return list;
    }
	
    /**
     * 拿到已连接设备的udid
     * @return
     */
    public List<String> getDevicesUdid() {
        List<String> list = new LinkedList<String>();
        String line = "";  
        String command = "adb devices";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
            reader.readLine();
            while ((line = reader.readLine()) != null) {
            	if (line.contains("device")){
            		list.add(line.toString().replace("	device", ""));
            	} 
            }
            
            log.info(list.size() + "台设备已连接");
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.getMessage();
        }
        
        return list;
    }
    
	@Test
	public void f() {
//		getDeviceInfo();
	}
}
