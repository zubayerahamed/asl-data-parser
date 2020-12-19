package com.asl.service;

import org.springframework.stereotype.Component;

import com.asl.model.ImportExportHelper;
import com.asl.model.ServiceException;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Component
public interface ImportExportService {

	public void processCSV(ImportExportHelper helper) throws ServiceException;
}
