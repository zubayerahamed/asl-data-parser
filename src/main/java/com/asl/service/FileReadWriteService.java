package com.asl.service;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Component
public interface FileReadWriteService {

	/**
	 * Process start point
	 */
	public void startProcess(Map<String, String> filesMap);
	
	/**
	 * Read all files from directory
	 * @param directory
	 * @return
	 */
	public void readFiles(Map<String, String> filesMap);
}
