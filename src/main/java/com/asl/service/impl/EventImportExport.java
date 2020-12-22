package com.asl.service.impl;

import org.springframework.stereotype.Service;

import com.asl.model.ImportExportHelper;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Service("eventimportexportService")
public class EventImportExport extends AbstractImportExport {

	@Override
	public void processCSV(ImportExportHelper helper) {
		System.out.println("From Event Service");
		System.out.println(helper.toString());
	}

}
