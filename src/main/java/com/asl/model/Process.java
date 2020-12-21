package com.asl.model;

import com.asl.ProcessStarter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Process extends Thread {

	private ImportExportHelper helper;

	public Process(ImportExportHelper helper) {
		this.helper = helper;
	}

	@Override
	public void run() {
		super.run();
		try {
			helper.getService().processCSV(helper);
		} catch (ServiceException e) {
			log.error("Error is : {}, {}", e.getMessage(), e);
		}
		ProcessStarter.removeFileFromMap(helper.getModuleType(), helper.getFileName(), helper.getFilesMap());
	}

}
