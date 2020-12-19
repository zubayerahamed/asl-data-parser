package com.asl;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.asl.enums.ModuleType;
import com.asl.model.ImportExportHelper;
import com.asl.model.ServiceException;
import com.asl.service.AsyncCSVProcessor;
import com.asl.service.ImportExportService;
import com.asl.service.impl.MonthlyFileReadWriteServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Slf4j
@Component
public class ProcessStarter implements CommandLineRunner {

	private static final String MONTHLY_MODULE = "monthlyimportexport";
	private static final String DAILY_MODULE = "dailyimportexport";
	private static final String ERROR = "Error is : {}, {}";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy");
	private Map<String, String> filesMap = new HashMap<>();

	@Autowired protected ApplicationContext appContext;
	@Autowired private MonthlyFileReadWriteServiceImpl fileReadWriteService;
	@Autowired private AsyncCSVProcessor asyncCSVProcessor;

	@Value("${file.properties.path}")
	private String propertiesPath;


	@Override
	public void run(String... args) throws Exception {
		//fileReadWriteService.startProcess(filesMap);
		//System.out.println(filesMap.size());
		//asyncCSVProcessor.processDataFromCSV();


		// check properties update
//		boolean stat = true;
//		while (stat) {
//			System.out.println(propertiesPath);
//			stat = "true".equalsIgnoreCase(getPropertiesValue("thread.process.running.stat"));
//			System.out.println("Stat is now : " + stat);
//			Thread.sleep(5000);
//		}

		Thread t1 = new Thread(() -> {
			ImportExportHelper helper = new ImportExportHelper();
			helper.setFileName("02_mdata1.xlsx");
			helper.setFileReadLocation("D:/ASL/");
			helper.setFileErrorLocation("D:/ASL/MONTHLY/ERROR/" + SDF.format(new Date()));
			helper.setFileSuccessLocation("D:/ASL/MONTHLY/SUCCESS/" + SDF.format(new Date()));
			helper.setFileArchiveLocation("D:/ASL/MONTHLY/ARCHIVE/" + SDF.format(new Date()));
			helper.setModuleType(ModuleType.MONTHLY);
			helper.setFirstRowHeader(true);
			helper.setDelimeterType(',');
			ImportExportService importExportService = getServiceModule(MONTHLY_MODULE);
			try {
				importExportService.processCSV(helper);
			} catch (ServiceException e) {
				log.error(ERROR, e.getMessage(), e);
			}
		});
		t1.start();

//		Thread t2 = new Thread(() -> {
//			ImportExportHelper helper = new ImportExportHelper();
//			helper.setFileLocation("D:/ASL/");
//			helper.setFileName("02_mdata2.xlsx");
//			ImportExportService importExportService = getServiceModule(DAILY_MODULE);
//			importExportService.processCSV(helper);
//		});
//		t2.start();
//
//		t1.join();
//		t2.join();
	}

	private ImportExportService getServiceModule(String module) {
		if(StringUtils.isBlank(module)) return null;
		try {
			return (ImportExportService) appContext.getBean(module + "Service");
		} catch (Exception e) {
			log.error("Error is : {}, {}", e.getMessage(), e);
			return null;
		}
	}

	private String getPropertiesValue(String key) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesPath));
			return properties.getProperty(key);
		} catch (IOException e) {
			log.error("Error is : {}, {}", e.getMessage(), e);
		}
		return null;
	}

}
