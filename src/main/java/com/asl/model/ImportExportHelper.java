package com.asl.model;

import com.asl.enums.ModuleType;

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
}
