package com.asl.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.asl.service.FileReadWriteService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Slf4j
@Service
public class MonthlyFileReadWriteServiceImpl implements FileReadWriteService {

//	@Value("${file.monthly.thread.number:1}")
//	private int numberOfThread;
//	@Value("${file.monthly.thread.sleep:10000}")
//	private int threadSleepTime;
//	@Value("${file.monthly.prefix:02_}")
//	private String filePrefix;
//	@Value("${file.monthly.location.read}")
//	private String fileReadLocation;
//	@Value("${file.monthly.location.backup}")
//	private String fileBackupLocation;
//	@Value("${file.monthly.location.error}")
//	private String fileErrorLocation;
//	@Value("${file.monthly.location.done}")
//	private String fileDoneLocation;
//
//	private boolean killProcess = false;
//
//	@Override
//	public void startProcess(Map<String, String> filesMap) {
//		readFiles(filesMap);
//
//		String fileToProcess = null;
//		int threadNumber = 0;
//		try {
//			while (true) {
//				if(killProcess) break;
//
//				if(!filesMap.isEmpty() && threadNumber < numberOfThread) {
//					threadNumber++;
//					fileToProcess = getFileNameAndUpdateFilesMap(filesMap, threadNumber);
//
//					if(StringUtils.isNotBlank(fileToProcess)) {
//						Process process = new Process(fileToProcess, fileReadLocation, threadNumber);
//						process.start();
//					}
//
//					break;
//				}
//
//				readFiles(filesMap);
//				Thread.sleep(threadSleepTime);
//			}
//		} catch (Exception e) {
//			log.error("Error is : {}, {}", e.getMessage(), e);
//		}
//	}
//
//	private String getFileNameAndUpdateFilesMap(Map<String, String> filesMap, int threadNumber) {
//		String fileName = null;
//		String fileType = null;
//		for(Map.Entry<String, String> m : filesMap.entrySet()) {
//			fileName = m.getKey();
//			fileType = m.getValue();
//			if("f".equalsIgnoreCase(fileType)) {
//				filesMap.put(fileName, "" + threadNumber);
//			}
//		}
//		return fileName;
//	}
//
//
//	@Override
//	public synchronized void readFiles(Map<String, String> filesMap) {
//		try {
//			File file = new File(fileReadLocation);
//			File[] files = file.listFiles();
//			for(File f : files) {
//				if(!f.isDirectory() 
//						&& !filesMap.containsKey(f.getName()) 
//						&& f.getName().contains(filePrefix)
//						&& !f.getName().contains("locak")) {
//					filesMap.put(f.getName(), "f");
//					//System.out.println(f.getName());
//				}
//			}
//		} catch (Exception e) {
//			log.error("Error is : {}, {}", e.getMessage(), e);
//		}
//	}

}
