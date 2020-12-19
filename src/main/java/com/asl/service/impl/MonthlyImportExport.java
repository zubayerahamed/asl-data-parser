package com.asl.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.asl.enums.FileType;
import com.asl.model.ImportExportHelper;
import com.asl.model.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Slf4j
@Service("monthlyimportexportService")
public class MonthlyImportExport extends AbstractImportExport {

	@Override
	public void processCSV(ImportExportHelper helper) throws ServiceException {
		System.out.println("From Monthly Service");
		System.out.println(helper.toString());

		String error = validateImportExportHelper(helper);
		if(StringUtils.isNotBlank(error)) throw new ServiceException(error);

		String fileNameWithPath = getFileNameWithPath(helper);
		if(StringUtils.isBlank(fileNameWithPath)) throw new ServiceException("File name with path not found");

		// Check file exist or not
		try {
			File file = new File(fileNameWithPath);
			if(!file.exists()) {
				log.error(ERROR, "File not found!", fileNameWithPath);
				throw new ServiceException("File not found");
			}
		} catch (Exception e) {
			log.error(ERROR, e.getMessage(), e);
			throw new ServiceException("File name with path not found");
		}

		String successFile = getWritableFile(helper, FileType.S);
		String errorFile = getWritableFile(helper, FileType.S);

		CSVFormat csvWritableFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL)
										.withIgnoreEmptyLines()
										.withDelimiter(',')
										.withIgnoreSurroundingSpaces();

		// Open file write stream
		try (CSVPrinter csvSuccessPrinter = new CSVPrinter(new FileWriter(successFile, true), csvWritableFormat);
			CSVPrinter csvErrorPrinter = new CSVPrinter(new FileWriter(errorFile, true), csvWritableFormat)) {


			// Open file read stream
			boolean firstLoop = true;
			int zLine = helper.isFirstRowHeader() ? 1 : 0;
			CSVFormat csvFormat = CSVFormat.DEFAULT.withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true);
			if(helper.isFirstRowHeader()) {
				csvFormat = CSVFormat.DEFAULT.withHeader().withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true).withIgnoreHeaderCase(true);
			}
			try (Reader reader = Files.newBufferedReader(Paths.get(fileNameWithPath));
					CSVParser csvParser = new CSVParser(reader, csvFormat)) {

				// Loop through each record line
				for (CSVRecord csvRecord : csvParser) {
					++zLine;
					long totalColumnFound = csvRecord.size();

					String meterNo = getRecordValue(csvRecord, totalColumnFound, 0);
					String dateTime = getRecordValue(csvRecord, totalColumnFound, 1);
					String activeEnergy1 = getRecordValue(csvRecord, totalColumnFound, 2);
					String activeEnergy2 = getRecordValue(csvRecord, totalColumnFound, 3);
					String activeEnergy3 = getRecordValue(csvRecord, totalColumnFound, 4);
					String activeEnergy4 = getRecordValue(csvRecord, totalColumnFound, 5);
					String activeEnergy5 = getRecordValue(csvRecord, totalColumnFound, 6);
					String reactiveEnergy = getRecordValue(csvRecord, totalColumnFound, 7);
					String meterBalance = getRecordValue(csvRecord, totalColumnFound, 8);

					
					
					
				}
				

			}

		} catch (IOException e) {
			log.error(ERROR, e.getMessage());
			throw new ServiceException(e.getMessage());
		}

		
	}

	
	

}
