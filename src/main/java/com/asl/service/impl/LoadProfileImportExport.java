package com.asl.service.impl;

import org.springframework.stereotype.Service;

import com.asl.model.ImportExportHelper;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Service("loadprofileimportexportService")
public class LoadProfileImportExport extends AbstractImportExport {

	@Override
	public void processCSV(ImportExportHelper helper) {
		System.out.println("From Daily Service");
		System.out.println(helper.toString());
	}

}
