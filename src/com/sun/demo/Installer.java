package com.sun.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

public class Installer implements Runnable{
	public static Logger log = Logger.getLogger(Installer.class);
	
	// 登录失败计数
	private int loginNum = 0;
	
	// 安装失败计数
	private int installNum = 0;
	
	private String udid;
	
	private String deviceName;
	
	public Installer(String udid) {
		this.udid = udid;
	}

	@Override
	public void run() {
		boolean flag = pushUiautomator();
		GetDevices devices = new GetDevices();
		deviceName = devices.getResult(udid, "ro.product.model");
		log.info("设备名称：" + deviceName + "\n系统版本：" + devices.getResult(udid, "ro.build.version.release"));
		installAndroidApp(flag);
	}
	
	private boolean pushUiautomator() {
		String userPath = System.getProperty("user.dir");
		
		// push uiautomator登录脚本到设备
		Utils.shell("adb -s " + udid + " push " + userPath + "/data/uiautomatorApps/app-debug.apk "
				+ " /data/local/tmp/com.sun.uiautomator");
		
		Utils.shell("adb -s " + udid + " push " + userPath + "/data/uiautomatorApps/app-debug-androidTest.apk "
				+ " /data/local/tmp/com.sun.uiautomator.test");
		
		// push uiautomator登录脚本到设备
		// 安装uiautomator自动登录脚本
		List<String> uiautomatorList = Utils.shell("adb -s " + udid + " shell pm install -r \"/data/local/tmp/com.sun.uiautomator\"");
		
		// 安装uiautomator自动登录脚本
		List<String> uiautomatorTestList = Utils.shell("adb -s " + udid + " shell pm install -r \"/data/local/tmp/com.sun.uiautomator.test\"");
	
		if (uiautomatorList.contains("Success") && uiautomatorTestList.contains("Success")) {
			return true;
		} else {
			log.error("【" + deviceName + "】uiautomator脚本未安装成功，请检查后再试");
			log.error(uiautomatorList);
			log.error(uiautomatorTestList);
			
			loginNum ++;
			
			return false;
		}
	
	}
	
//	/**
//	 * 执行
//	 */
//	@Test
//	public static void main(String[] args) {
//		GetDevices devices = new GetDevices();
//		List<String> udidList = devices.getDevicesUdid();
//    	for(String udid: udidList) {
//    		log.info("设备名称：" + devices.getResult(udid, "ro.product.model") + "\n系统版本：" + devices.getResult(udid, "ro.build.version.release"));
//    		Installer installer = new Installer();
//    		installer.installAndroidApp(udid);
//    	}
//	}
	
	/**
	 * uiautomator登录得到
	 */
	public void loginApp(String apkName) {
		// 使用uiautomator登录唯医app
		List<String> loginList = Utils.shell("adb -s " + udid + " shell am instrument -w -r -e debug false -e class com.sun.uiautomator.LoginTest "
				+ "com.sun.uiautomator.test/android.support.test.runner.AndroidJUnitRunner");
	
		if(loginList.contains("OK (2 tests)")) {
			log.info("【" + deviceName + "】登录成功");
		} else {
			loginNum ++;
			log.error(loginList);
			log.error("【" + deviceName + "】【" + apkName + "】登录失败，请检查");
		}
		Utils.sleep(10);
	}
	
	/**
	 * 安装Android安装包
	 * @return 
	 */
	private void installAndroidApp(boolean flag){
		String userPath = System.getProperty("user.dir");
		File[] fileList = Utils.getFileList(userPath + "/data/testApps/");
		int num = 0;
		for (File file:fileList) {
			num ++;
			log.info("【" + deviceName + "】***开始安装第" + num + "个APP: " + file.getName());
			String install = "adb -s " + udid + " install " + file.getAbsolutePath();
			String uninstall = "adb -s " + udid + " uninstall com.luojilab.player";
			BufferedReader reader = new BufferedReader(new InputStreamReader(Utils.command(install).getInputStream()));
			StringBuffer buffer = Utils.bufferResponse(reader);
			if (Utils.assertResponse(buffer, "Success")) {
				log.info("【" + deviceName + "】APP安装成功");
				log.info("【" + deviceName + "】等待登录得到");
				if (flag) {
					loginApp(file.getName());
				}
	        	Utils.command("adb -s " + udid + " shell pm clear com.luojilab.player");
			} else if(Utils.assertResponse(buffer, "Failure [INSTALL_FAILED_ALREADY_EXISTS]")) {
				log.info("【" + deviceName + "】APP已存在，需要先卸载");
				reader = new BufferedReader(new InputStreamReader(Utils.command(uninstall).getInputStream()));
				if (Utils.assertResponse(Utils.bufferResponse(reader), "Success")) {
					log.info("【" + deviceName + "】卸载成功，继续安装");
					reader = new BufferedReader(new InputStreamReader(Utils.command(install).getInputStream()));
					if (Utils.assertResponse(Utils.bufferResponse(reader), "Success")) {
						log.info("【" + deviceName + "】APP安装成功");
			        	log.info("【" + deviceName + "】等待登录得到");
			        	if (flag) {
			        		loginApp(file.getName());
			        	}
						Utils.command("adb -s " + udid + " shell pm clear com.luojilab.player");
			    	} else {
			    		installNum ++;
			    		log.error("【" + deviceName + "】【" + file.getName() + "】安装失败");
			    	}
				} else {
					installNum ++;
					log.error("【" + deviceName + "】【" + file.getName() + "】卸载失败");
				}
			} else if(Utils.assertResponse(buffer, "error")) {
				installNum ++;
				log.error("【" + deviceName + "】【" + file.getName() + "】安装失败");
			}
		}
		log.info("【" + deviceName + "】\n安装成功【" + String.valueOf(num - installNum) + "】个\n失败【" + String.valueOf(installNum) + "】个\n"
				+ "登录成功【" + String.valueOf(num - loginNum) + "】个\n失败【" + String.valueOf(loginNum) + "】个");
	}

}

