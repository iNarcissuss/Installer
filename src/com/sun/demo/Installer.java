package com.sun.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class Installer {
	public static Logger log = Logger.getLogger(Installer.class);
	
	private static int loginNum = 0;
	
	private static int installNum = 0;
	
	/**
	 * 拿到设备名称和系统版本
	 */
	@Test
	public static void main(String[] args) {
		GetDevices devices = new GetDevices();
		List<String> udidList = devices.getDevicesUdid();
    	for(String udid: udidList) {
    		log.info("设备名称：" + devices.getResult(udid, "ro.product.model") + "\n系统版本：" + devices.getResult(udid, "ro.build.version.release"));
    		Installer installer = new Installer();
    		installer.installAndroidApp(udid);
    	}
	}
	
	/**
	 * uiautomator登录得到
	 */
	public void loginApp(String udid, String apkName) {
		String userPath = System.getProperty("user.dir");
		
		// push uiautomator登录脚本到设备
		shell("adb -s " + udid + " push " + userPath + "/data/apps/app-debug.apk "
				+ " /data/local/tmp/com.sun.uiautomator");
		
		shell("adb -s " + udid + " push " + userPath + "/data/apps/app-debug-androidTest.apk "
				+ " /data/local/tmp/com.sun.uiautomator.test");
		
		// push uiautomator登录脚本到设备
		// 安装uiautomator自动登录脚本
		List<String> uiautomatorList = shell("adb shell pm install -r \"/data/local/tmp/com.sun.uiautomator\"");
		
		// 安装uiautomator自动登录脚本
		List<String> uiautomatorTestList = shell("adb shell pm install -r \"/data/local/tmp/com.sun.uiautomator.test\"");
		
		if (uiautomatorList.contains("Success") && uiautomatorTestList.contains("Success")) {
			// 使用uiautomator登录唯医app
			List<String> loginList = shell("adb -s " + udid + " shell am instrument -w -r -e debug false -e class com.sun.uiautomator.LoginTest "
					+ "com.sun.uiautomator.test/android.support.test.runner.AndroidJUnitRunner");
		
			if(loginList.contains("OK (2 tests)")) {
				log.info("登录成功");
			} else {
				loginNum ++;
				log.error(loginList);
				log.error("【" + apkName + "】登录失败，请检查");
			}
			sleep(10);
		} else {
			log.error("uiautomator脚本未安装成功，请检查后再试");
			log.error(uiautomatorList);
			log.error(uiautomatorTestList);
		}
	}
	
	/**
	 * 安装Android安装包
	 * @return 
	 */
	private void installAndroidApp(String udid){
		String userPath = System.getProperty("user.dir");
		File[] fileList = getFileList(userPath + "/data/output/");
		int num = 0;
		for (File file:fileList) {
			num ++;
			log.info("***开始安装第" + num + "个APP: " + file.getName());
			String install = "adb -s " + udid + " install " + file.getAbsolutePath();
			String uninstall = "adb -s " + udid + " uninstall com.luojilab.player";
			BufferedReader reader = new BufferedReader(new InputStreamReader(command(install).getInputStream()));
			StringBuffer buffer = bufferResponse(reader);
			if (assertResponse(buffer, "Success")) {
				log.info("APP安装成功");
				log.info("等待登录得到");
	        	loginApp(udid, file.getName());
	        	
	        	command("adb -s " + udid + " shell pm clear com.luojilab.player");
			} else if(assertResponse(buffer, "Failure [INSTALL_FAILED_ALREADY_EXISTS]")) {
				log.info("APP已存在，需要先卸载");
				reader = new BufferedReader(new InputStreamReader(command(uninstall).getInputStream()));
				if (assertResponse(bufferResponse(reader), "Success")) {
					log.info("卸载成功，继续安装");
					reader = new BufferedReader(new InputStreamReader(command(install).getInputStream()));
					if (assertResponse(bufferResponse(reader), "Success")) {
						log.info("APP安装成功");
			        	log.info("等待登录得到");
			        	loginApp(udid, file.getName());
			        	
						command("adb -s " + udid + " shell pm clear com.luojilab.player");
			    	} else {
			    		installNum ++;
			    		log.error("【" + file.getName() + "】安装失败");
			    	}
				} else {
					installNum ++;
					log.error("【" + file.getName() + "】卸载失败");
				}
			} else if(assertResponse(buffer, "error")) {
				installNum ++;
				log.error("【" + file.getName() + "】安装失败");
			}
		}
		log.info("安装成功【" + String.valueOf(num - installNum) + "】个\n失败【" + String.valueOf(installNum) + "】个");
		log.info("登录成功【" + String.valueOf(num - loginNum) + "】个\n失败【" + String.valueOf(loginNum) + "】个");
	}
	
	/**
	 * 执行终端命令后的输出拼装在一起
	 * @param reader
	 * @return
	 */
	private StringBuffer bufferResponse(BufferedReader reader) {
		StringBuffer buffer = new StringBuffer();
		String line = ""; 
		try {
			while ((line = reader.readLine()) != null) {
				buffer.append(line.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffer;
	}
	
	/**
	 * 判断执行终端命令后的返回结果
	 * @param reader
	 * @param tag
	 * @return
	 */
	private boolean assertResponse(StringBuffer reader, String tag) {
		return (reader.indexOf(tag) != -1);
	}
	
	private static Process command(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return process;
	}
	
	public File[] getFileList(String path) {
	   	File file = new File(path);
    	File[] files = file.listFiles();
    	
    	return files;
	}
	
	/**
	 * Thread.sleep
	 * @param d
	 */
	public static void sleep(double d) {
		try {
			d *= 1000;
			Thread.sleep((int)d);
		} catch(Exception e) {}
	}
	
    public static List<String> shell(String command) {
    	List<String> list = new LinkedList<String>();
        try{
            Process proc = Runtime.getRuntime().exec(command);
            try {
                if (proc.waitFor() != 0) {
                	log.error("exit value = " + proc.exitValue());
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {    
                	if (!line.isEmpty()) {
                		list.add(line.toString());
                	}
                }   
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
//                    proc.destroy();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
             
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return list;
    }  
}

