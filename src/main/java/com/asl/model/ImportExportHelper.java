package com.asl.model;

import java.util.Map;

import com.asl.enums.ModuleType;
import com.asl.service.ImportExportService;

import lombok.Data;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Data
public class ImportExportHelper {

	private String fileName;
	private String fileReadLocation;
	private String fileErrorLocation;
	private String fileSuccessLocation;
	private String fileArchiveLocation;
	private ModuleType moduleType;
	private boolean firstRowHeader;
	private char delimeterType;
	private Map<String, String> filesMap;
	private ImportExportService service;
	private Long threadName;
}
