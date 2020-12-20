package com.asl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.valves.StuckThreadDetectionValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.asl.enums.ModuleType;
import com.asl.model.ModuleFilesContainer;
import com.asl.service.ImportExportService;
import com.asl.service.impl.Process;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Slf4j
@Component
public class ProcessStarter implements CommandLineRunner {

	private static final String ERROR = "Error is : {}, {}";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy");
	private static int mThreadVal = 0;
	private static int dThreadVal = 0;

	@Autowired private Environment env;
	@Autowired private ApplicationContext appContext;

	@Value("${dp.module.active}")
	private List<String> activeModules;

	@Override
	public void run(String... args) throws Exception {

		// Load module wise files into map
		ModuleFilesContainer mfc = new ModuleFilesContainer();

		for(String module : activeModules) {
			if(ModuleType.MONTHLY.getCode().equalsIgnoreCase(module)) {
				loadFilesIntoMap(ModuleType.MONTHLY, mfc);
				new Thread(() -> {
					prepareProcessStart(ModuleType.MONTHLY, mfc.getMonthlyFiles());
				}).start();
			} else if (ModuleType.DAILY.getCode().equalsIgnoreCase(module)) {
				loadFilesIntoMap(ModuleType.DAILY, mfc);
				new Thread(() -> {
					prepareProcessStart(ModuleType.DAILY, mfc.getDailyFiles());
				}).start();
			}
		}

//		Thread t1 = new Thread(() -> {
//			ImportExportHelper helper = new ImportExportHelper();
//			helper.setFileName("02_mdata1.csv");
//			helper.setFileReadLocation("D:/ASL/");
//			helper.setFileErrorLocation("D:/ASL/MONTHLY/ERROR/" + SDF.format(new Date()));
//			helper.setFileSuccessLocation("D:/ASL/MONTHLY/SUCCESS/" + SDF.format(new Date()));
//			helper.setFileArchiveLocation("D:/ASL/MONTHLY/ARCHIVE/" + SDF.format(new Date()));
//			helper.setModuleType(ModuleType.MONTHLY);
//			helper.setFirstRowHeader(true);
//			helper.setDelimeterType(',');
//			ImportExportService importExportService = getServiceModule(MONTHLY_MODULE);
//			try {
//				importExportService.processCSV(helper);
//			} catch (ServiceException e) {
//				log.error(ERROR, e.getMessage(), e);
//			}
//		});
//		t1.start();

		
	}

	private void prepareProcessStart(ModuleType moduleType, Map<String, String> filesMap) {
		int threadName = 0;
		int numberOfThreads = 1;
		int sleepTime = 1000;
		if(ModuleType.MONTHLY.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.monthly.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.monthly.thread.sleep"));
		} else if (ModuleType.DAILY.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.daily.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.daily.thread.sleep"));
		}

		try {
			boolean stat = true;
			while (stat) {
				stat = "true".equalsIgnoreCase(getPropertiesValue("thread.process.running.stat"));
				System.out.println(moduleType.getCode() + " - " + numberOfThreads + " - " + filesMap.size());

				// Create thread to parse files
				if(getThreadVal(moduleType) < numberOfThreads && !filesMap.isEmpty()) {
					threadName++;
					String fileToProcess = getFileName(getThreadVal(moduleType), filesMap);
					if(StringUtils.isNotBlank(fileToProcess)) {
						increaseThreadVal(moduleType);
//						Process process = new Process();
						removeFileFromMap(moduleType, fileToProcess, filesMap);
						System.out.println(fileToProcess + " - " + threadName);
					}

				}

				TimeUnit.SECONDS.sleep(sleepTime);
			}
		} catch (Exception e) {
			log.error(ERROR, e.getMessage(), e);
		}
	}

	private static synchronized int getThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			return mThreadVal;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			return dThreadVal;
		}
		return 0;
	}

	private static synchronized void increaseThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			mThreadVal++;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			dThreadVal++;
		}
	}

	private static synchronized void decreaseThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			mThreadVal--;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			dThreadVal--;
		}
	}

	public static synchronized void removeFileFromMap(ModuleType moduleType, String fileNameToProcess, Map<String,String> filesMap) {
		decreaseThreadVal(moduleType);
		removeFileName(fileNameToProcess, filesMap);
	}

	public static synchronized void removeFileName(String fileNameToProcess, Map<String,String> filesMap) {
		if (filesMap.containsKey(fileNameToProcess)) {
			filesMap.remove(fileNameToProcess);
		}
	}

	private synchronized String getFileName(int threadName, Map<String, String> filesMap) {
		String fileName = "";
		for(Map.Entry<String, String> e : filesMap.entrySet()) {
			if("f".equalsIgnoreCase(e.getValue())) {
				fileName = e.getKey();
				filesMap.put(fileName, "" + threadName);
				break;
			}
		}
		return fileName;
	}

	private synchronized void loadFilesIntoMap(ModuleType moduleType, ModuleFilesContainer mfc) {
		String fileReadPath = null;
		String filePrefix = null;
		Map<String, String> filesMap = new HashMap<>();

		if(ModuleType.MONTHLY.equals(moduleType)) {
			fileReadPath = env.getProperty("dp.module.monthly.file.readpath");
			filePrefix = env.getProperty("dp.module.monthly.file.prefix");
			filesMap = mfc.getMonthlyFiles();
		} else if (ModuleType.DAILY.equals(moduleType)) {
			fileReadPath = env.getProperty("dp.module.daily.file.readpath");
			filePrefix = env.getProperty("dp.module.daily.file.prefix");
			filesMap = mfc.getDailyFiles();
		}

		File file = new File(fileReadPath);
		File[] fileNames = file.listFiles();
		for(File f : fileNames) {
			if(!f.isDirectory() 
					&& !filesMap.containsKey(f.getName()) 
					&& f.getName().contains(filePrefix)
					&& !f.getName().contains("locak")) {
				filesMap.put(f.getName(), "f");
			}
		}
	}

	private ImportExportService getServiceModule(ModuleType module) {
		if(module == null) return null;
		try {
			return (ImportExportService) appContext.getBean(module.getService() + "Service");
		} catch (Exception e) {
			log.error(ERROR, e.getMessage(), e);
			return null;
		}
	}

	private String getPropertiesValue(String key) {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(env.getProperty("dp.properties-path"))) {
			properties.load(fis);
			return properties.getProperty(key);
		} catch (IOException e) {
			log.error(ERROR, e.getMessage(), e);
		}
		return null;
	}

	//	private void readXslFile() throws IOException {
	//	File excel = new File("D:/ASL/02_mdata1.xlsx");
	//	FileInputStream fis = new FileInputStream(excel);
	//	XSSFWorkbook book = new XSSFWorkbook(fis);
	//	XSSFSheet sheet = book.getSheetAt(0);
	//	Iterator<Row> itr = sheet.iterator(); 
	//	
	//
	//
	//	while (itr.hasNext()) {
	//		Row row = itr.next();
	//		Iterator<Cell> cellIterator = row.cellIterator();
	//
	//		List<String> cols = new ArrayList<String>();
	//		while (cellIterator.hasNext()) {
	//			Cell cell = cellIterator.next();
	//			switch (cell.getCellType()) {
	//			case Cell.CELL_TYPE_STRING:
	////				System.out.print(cell.getStringCellValue() + "\t");
	//				cols.add(cell.getStringCellValue());
	//				break;
	//			case Cell.CELL_TYPE_NUMERIC:
	////				System.out.print(cell.getNumericCellValue() + "\t");
	//				cols.add(BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString());
	//				break;
	//			case Cell.CELL_TYPE_BOOLEAN:
	//				System.out.print(cell.getBooleanCellValue() + "\t");
	//				cols.add(Boolean.valueOf(cell.getBooleanCellValue()).toString());
	//				break;
	//			default:
	//				cols.add("");
	//			}
	//		}
	//		
	//		
	//		System.out.println(cols.get(0) + "-" + cols.get(1) + "-" + cols.get(2) + "-" + cols.get(3) + "-" + cols.get(4) + "-" + cols.get(5) + "-" + cols.get(6) + "-" + cols.get(7) + "-" + cols.get(8));
	//		
	//		
	//	}
	//
	//}
	
	//public Object getCellValue(Cell cell) {
	//	if (cell != null) {
	//		switch (cell.getCellType()) {
	//		case Cell.CELL_TYPE_STRING:
	//			return cell.getStringCellValue();
	//		case Cell.CELL_TYPE_BOOLEAN:
	//			return cell.getBooleanCellValue();
	//		case Cell.CELL_TYPE_NUMERIC:
	//			return cell.getNumericCellValue();
	//		}
	//	}
	//	return "";
	//}
}
