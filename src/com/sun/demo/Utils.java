package com.sun.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class Utils {
	public static Logger log = Logger.getLogger(Utils.class);
	
	/**
	 * 执行终端命令后的输出拼装在一起
	 * @param reader
	 * @return
	 */
	public static StringBuffer bufferResponse(BufferedReader reader) {
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
	public static  boolean assertResponse(StringBuffer reader, String tag) {
		return (reader.indexOf(tag) != -1);
	}
	
	public static Process command(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return process;
	}
	
	public static File[] getFileList(String path) {
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
