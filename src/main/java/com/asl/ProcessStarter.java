package com.asl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.asl.enums.ModuleType;
import com.asl.model.ImportExportHelper;
import com.asl.model.ModuleFilesContainer;
import com.asl.model.Process;
import com.asl.service.ImportExportService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Slf4j
@Component
public class ProcessStarter implements CommandLineRunner {

	private static final int DEFAULT_NUMBER_OF_THREAD = 1;
	private static final int DEFAULT_THREAD_SLEEP_TIME_IN_SEC = 10;
	private static final String ERROR = "Error is : {}, {}";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy");
	private static int mThreadVal = 0;
	private static int dThreadVal = 0;
	private static int eThreadVal = 0;
	private static int lThreadVal = 0;

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
				new Thread(() -> prepareProcessStart(ModuleType.MONTHLY, mfc)).start();
			} else if (ModuleType.DAILY.getCode().equalsIgnoreCase(module)) {
				loadFilesIntoMap(ModuleType.DAILY, mfc);
				new Thread(() -> prepareProcessStart(ModuleType.DAILY, mfc)).start();
			} else if (ModuleType.EVENT.getCode().equalsIgnoreCase(module)) {
				loadFilesIntoMap(ModuleType.EVENT, mfc);
				new Thread(() -> prepareProcessStart(ModuleType.EVENT, mfc)).start();
			} else if (ModuleType.LOAD_PROFILE.getCode().equalsIgnoreCase(module)) {
				loadFilesIntoMap(ModuleType.LOAD_PROFILE, mfc);
				new Thread(() -> prepareProcessStart(ModuleType.LOAD_PROFILE, mfc)).start();
			}
		}

	}

	/**
	 * Prepare thread processor based on module type
	 * @param moduleType
	 * @param mfc
	 */
	private void prepareProcessStart(ModuleType moduleType, ModuleFilesContainer mfc) {
		Long threadName = Long.valueOf(0);
		int numberOfThreads = DEFAULT_NUMBER_OF_THREAD;
		int sleepTime = DEFAULT_THREAD_SLEEP_TIME_IN_SEC;
		if(ModuleType.MONTHLY.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.monthly.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.monthly.thread.sleep"));
		} else if (ModuleType.DAILY.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.daily.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.daily.thread.sleep"));
		} else if (ModuleType.EVENT.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.event.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.event.thread.sleep"));
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			numberOfThreads = Integer.parseInt(env.getProperty("dp.module.loadprofile.thread.number"));
			sleepTime = Integer.parseInt(env.getProperty("dp.module.loadprofile.thread.sleep"));
		}

		// CSV process thread generate start from here
		try {
			boolean stat = true;
			while (stat) {
				stat = "true".equalsIgnoreCase(getPropertiesValue("thread.process.running.stat"));
				log.debug("Module : {}, Total Files to process : {}", moduleType.getCode(), getFilesMapOfModule(moduleType, mfc).size());

				// Create thread to parse files
				if(getThreadVal(moduleType) < numberOfThreads && !getFilesMapOfModule(moduleType, mfc).isEmpty()) {

					String fileToProcess = getFileName(getThreadVal(moduleType), getFilesMapOfModule(moduleType, mfc));
					if(StringUtils.isNotBlank(fileToProcess)) {
						threadName++;
						increaseThreadVal(moduleType);
						Process process = new Process(getImportExportHelper(moduleType, fileToProcess, getFilesMapOfModule(moduleType, mfc), threadName));
						process.start();
					}

				}

				// Load files into map again if new available
				loadFilesIntoMap(moduleType, mfc);
				TimeUnit.SECONDS.sleep(sleepTime);
			}
		} catch (Exception e) {
			log.error(ERROR, e.getMessage(), e);
		}
	}

	/**
	 * Get Module wise files map
	 * @param moduleType
	 * @param mfc
	 * @return
	 */
	private synchronized Map<String, String> getFilesMapOfModule(ModuleType moduleType, ModuleFilesContainer mfc){
		if(ModuleType.MONTHLY.equals(moduleType)){
			return mfc.getMonthlyFiles();
		} else if (ModuleType.DAILY.equals(moduleType)) {
			return mfc.getDailyFiles();
		} else if (ModuleType.EVENT.equals(moduleType)) {
			return mfc.getEventFiles();
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			return mfc.getLoadProfileFiles();
		}
		return Collections.emptyMap();
	}

	/**
	 * Prepare import export helper object
	 * @param moduleType
	 * @param fileToProcess
	 * @param filesMap
	 * @param threadName
	 * @return {@link ImportExportHelper}
	 */
	private synchronized ImportExportHelper getImportExportHelper(ModuleType moduleType, String fileToProcess, Map<String,String> filesMap, Long threadName) {
		ImportExportHelper helper = new ImportExportHelper();
		helper.setFileName(fileToProcess);
		helper.setModuleType(moduleType);
		helper.setFileReadLocation(env.getProperty("dp.module."+ moduleType.getCode().toLowerCase() +".file.readpath"));
		helper.setFileErrorLocation(env.getProperty("dp.module."+ moduleType.getCode().toLowerCase() +".file.errorpath") + "/" + SDF.format(new Date()));
		helper.setFileSuccessLocation(env.getProperty("dp.module."+ moduleType.getCode().toLowerCase() +".file.successpath") + "/" + SDF.format(new Date()));
		helper.setFileArchiveLocation(env.getProperty("dp.module."+ moduleType.getCode().toLowerCase() +".file.archivepath") + "/" + SDF.format(new Date()));
		helper.setFirstRowHeader(true);
		helper.setDelimeterType(',');
		helper.setFilesMap(filesMap);
		helper.setService(getServiceModule(moduleType));
		helper.setThreadName(threadName);
		return helper;
	}

	/**
	 * Get current threadval based on module type
	 * @param moduleType
	 * @return
	 */
	private static synchronized int getThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			return mThreadVal;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			return dThreadVal;
		} else if (ModuleType.EVENT.equals(moduleType)) {
			return eThreadVal;
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			return lThreadVal;
		}
		return 0;
	}

	/**
	 * Increase threadval basedn on module type
	 * @param moduleType
	 */
	private static synchronized void increaseThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			mThreadVal++;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			dThreadVal++;
		} else if (ModuleType.EVENT.equals(moduleType)) {
			eThreadVal++;
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			lThreadVal++;
		}
	}

	/**
	 * Decrease threadval based on module type
	 * @param moduleType
	 */
	private static synchronized void decreaseThreadVal(ModuleType moduleType) {
		if(ModuleType.MONTHLY.equals(moduleType)){
			mThreadVal--;
		} else if (ModuleType.DAILY.equals(moduleType)) {
			dThreadVal--;
		} else if (ModuleType.EVENT.equals(moduleType)) {
			eThreadVal--;
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			lThreadVal--;
		}
	}

	/**
	 * Remove file from map and decrease threadval to based on module type
	 * @param moduleType
	 * @param fileNameToProcess
	 * @param filesMap
	 */
	public static synchronized void removeFileFromMap(ModuleType moduleType, String fileNameToProcess, Map<String,String> filesMap) {
		decreaseThreadVal(moduleType);
		removeFileName(fileNameToProcess, filesMap);
	}

	/**
	 * Remove filename from map
	 * @param fileNameToProcess
	 * @param filesMap
	 */
	public static synchronized void removeFileName(String fileNameToProcess, Map<String,String> filesMap) {
		if (filesMap.containsKey(fileNameToProcess)) {
			filesMap.remove(fileNameToProcess);
		}
	}

	/**
	 * Get file name from map
	 * @param threadName
	 * @param filesMap
	 * @return
	 */
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

	/**
	 * Loads files name into map from server based on module type
	 * @param moduleType
	 * @param mfc
	 */
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
		} else if (ModuleType.EVENT.equals(moduleType)) {
			fileReadPath = env.getProperty("dp.module.event.file.readpath");
			filePrefix = env.getProperty("dp.module.event.file.prefix");
			filesMap = mfc.getEventFiles();
		} else if (ModuleType.LOAD_PROFILE.equals(moduleType)) {
			fileReadPath = env.getProperty("dp.module.loadprofile.file.readpath");
			filePrefix = env.getProperty("dp.module.loadprofile.file.prefix");
			filesMap = mfc.getLoadProfileFiles();
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

	/**
	 * Return module wise service
	 * @param module
	 * @return {@link ImportExportService}
	 */
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
}
