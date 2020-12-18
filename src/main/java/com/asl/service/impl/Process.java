package com.asl.service.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Process extends Thread {

	private String fileToProcess;
	private String filePath;
	private int threadNumber;

	public Process(String fileToProcess, String filePath, int threadNumber) {
		this.fileToProcess = fileToProcess;
		this.threadNumber = threadNumber;
		this.filePath = filePath;
	}

	@Override
	public void run() {
		super.run();
		System.out.println(fileToProcess + " - " + filePath + " - " + threadNumber);
	}

}
