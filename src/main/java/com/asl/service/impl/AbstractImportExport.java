package com.asl.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asl.enums.FileType;
import com.asl.model.ImportExportHelper;
import com.asl.model.ServiceException;
import com.asl.service.ImportExportService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Slf4j
public abstract class AbstractImportExport implements ImportExportService {

	protected static final String ERROR = "Error is : {}, {}";

	@Autowired protected JdbcTemplate jdbcTemplate;

	protected String getFileNameWithPath(ImportExportHelper helper) {
		if(StringUtils.isBlank(helper.getFileName()) || StringUtils.isBlank(helper.getFileReadLocation())) return null;
		if(helper.getFileReadLocation().endsWith("/")) {
			return helper.getFileReadLocation().concat(helper.getFileName());
		} else {
			return helper.getFileReadLocation().concat("/").concat(helper.getFileName());
		}
	}

	protected String validateImportExportHelper(ImportExportHelper helper) throws ServiceException {
		int count = 0;
		StringBuilder errorMessage = new StringBuilder();
		if(StringUtils.isBlank(helper.getFileName())) {
			count++;
			errorMessage.append(count + ". File name is empty\n");
		}
		if(StringUtils.isBlank(helper.getFileReadLocation())) {
			count++;
			errorMessage.append(count + ". File read location is empty\n");
		}
		if(StringUtils.isBlank(helper.getFileErrorLocation())) {
			count++;
			errorMessage.append(count + ". File error output location is empty\n");
		}
		if(StringUtils.isBlank(helper.getFileSuccessLocation())) {
			count++;
			errorMessage.append(count + ". File success output location is empty\n");
		}
		if(StringUtils.isBlank(helper.getFileArchiveLocation())) {
			count++;
			errorMessage.append(count + ". File archive output location is empty\n");
		}
		return errorMessage.toString();
	}

	protected String getWritableFileName(ImportExportHelper helper, FileType type) {
		if(StringUtils.isBlank(helper.getFileName())) return null;

		String filename = FilenameUtils.getBaseName(helper.getFileName());
		String extention = "." + FilenameUtils.getExtension(helper.getFileName());
		String suffix = "_"+ type.name() +"_";
		String randId =  "" + UUID.randomUUID();

		return filename + suffix + randId + extention;
	}

	protected String getWritableFile(ImportExportHelper helper, FileType type) {
		String writableFileName = getWritableFileName(helper, type);
		String fileWithPath = null;
		if(FileType.S.equals(type)) {
			fileWithPath = helper.getFileSuccessLocation().concat("/").concat(writableFileName);
		} else if (FileType.E.equals(type)) {
			fileWithPath = helper.getFileErrorLocation().concat("/").concat(writableFileName);
		} else if (FileType.A.equals(type)) {
			fileWithPath = helper.getFileArchiveLocation().concat("/").concat(writableFileName);
		}

		try {
			File fl =  new File(fileWithPath);
			if(!fl.exists()) {
				fl.mkdirs();
			}
		} catch (Exception e) {
			log.error("Error to write dir file: {} {}", fileWithPath, e.getMessage());
		}

		return fileWithPath;
	}

	protected String getRecordValue(CSVRecord csvRecord, long totalNumberOfColumn, int limit) {
		return totalNumberOfColumn > limit ? csvRecord.get(limit) : "";
	}

	/**
	 * Remove Comma separeted lines from csv <br>
	 * Create an empty .tmp file then copy to original file <br>
	 * if any line contains all comma then skip all line below <br>
	 * @param fileName
	 */
	protected long sanitizeUploadedFile(String fileName, char delimeterType) {
		try(BufferedReader in = new BufferedReader(new FileReader(fileName));
				FileWriter fw = new FileWriter(new File(fileName + ".tmp"), true);) {

			String line = "";
			long count = 0;
			while((line = in.readLine()) != null) {
				String nline = line.trim().replaceAll("^["+ delimeterType +"|\\s]+$", "");
				if ("".equals(nline)) break;
				count++;
				fw.write(line);
				fw.write("\n");
			}
			fw.flush();

			File nFile = new File(fileName);
			Files.copy(Paths.get(fileName + ".tmp"), new FileOutputStream(nFile));
			Files.delete(Paths.get(fileName + ".tmp"));
			return count;
		} catch(IOException e) {
			log.error(ERROR, e.getMessage(), e);
			return 0;
		}
	}
}
