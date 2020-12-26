package com.asl.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

		// Move source file to destination folder. If successfull, then update files map by removing file name
		if (true == moveFile(helper.getFileReadLocation(), helper.getFileArchiveLocation(), helper.getFileName())) {
			log.debug("File moved successfully");
			ProcessStarter.removeFileFromMap(helper.getModuleType(), helper.getFileName(), helper.getFilesMap());
		}

	}

	private boolean moveFile(String source, String dest, String fileName) {
		// Check destination folder exist or not. If not then create
		File file = new File(dest);
		if(!file.exists()) {
			file.mkdirs();
		}

		// Delete previous archive file if have same name 
		try {
			Files.deleteIfExists(Paths.get(dest + "/" + fileName));
			log.debug("Deleted same name previous archived file : {}", dest + "/" + fileName);
		} catch (IOException e) {
			log.error("Can't delete same name previous archived : {}", dest + "/" + fileName);
		}

		// Move file from source to destination
		Path temp = null;
		try {
			temp = Files.move(Paths.get(source + "/" + fileName), Paths.get(dest + "/" + fileName));
		} catch (IOException e) {
			log.error("Error is : {}, {}", e.getMessage(), e);
			return false;
		}
		return temp != null;
	}

}
