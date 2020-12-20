package com.asl.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
import com.asl.model.MonthlyCSVColumns;
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

		// Get Success & Error file write directory
		String successFile = getWritableFile(helper, FileType.S);
		String errorFile = getWritableFile(helper, FileType.E);

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
			CSVFormat csvFormat = CSVFormat.EXCEL.withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true);
			if(helper.isFirstRowHeader()) {
				csvFormat = CSVFormat.EXCEL.withHeader().withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true).withIgnoreHeaderCase(true);
			}
			try (Reader reader = Files.newBufferedReader(Paths.get(fileNameWithPath), StandardCharsets.ISO_8859_1);
					CSVParser csvParser = new CSVParser(reader, csvFormat)) {
				//new BufferedReader(new InputStreamReader(new FileInputStream(fileNameWithPath),"utf-8"));

				// Loop through each record line
				for (CSVRecord csvRecord : csvParser) {
					++zLine;
					long totalColumnFound = csvRecord.size();
					StringBuilder errorReasons = new StringBuilder();

					MonthlyCSVColumns mcc = new MonthlyCSVColumns(csvRecord, totalColumnFound);

					// Validate columns
					mcc.validaeColumns(mcc, errorReasons, zLine);

					// If column has error then write to error file and continue for next record
					if(StringUtils.isNotBlank(errorReasons)) {
						csvErrorPrinter.printRecord(mcc.getErrorRecord(mcc, errorReasons));
						continue;
					}

					System.out.println(mcc.toString());
					// Get SJID using meter number
					StringBuilder sql1 = new StringBuilder("SELECT SJID FROM SB_SJZB WHERE SFYX=1 AND CLDJH='" + mcc.getMeterNo() + "'");
					String sjid = null;
					try {
						List<Map<String, Object>> list = jdbcTemplate.queryForList(sql1.toString());
						if(!list.isEmpty()) {
							sjid = ((BigDecimal) list.get(0).get("SJID")).toPlainString().trim();
						}
					} catch (Exception e) {
						log.error(ERROR, e.getMessage(), e);
						errorReasons.append(generateErrors(zLine, "A", "SJID read query failed - " + e.getMessage()));
						csvErrorPrinter.printRecord(mcc.getErrorRecord(mcc, errorReasons));
						continue;
					}
					if(StringUtils.isBlank(sjid)) {
						errorReasons.append(generateErrors(zLine, "A", "Sequence number not found using meter number " + mcc.getMeterNo()));
						csvErrorPrinter.printRecord(mcc.getErrorRecord(mcc, errorReasons));
						continue;
					}
					System.out.println("===> sjid : " + sjid);

					// Get CT, PT using meter number
					StringBuilder sql2 = new StringBuilder("SELECT CT,PT FROM DA_BJ WHERE BJJH='" + mcc.getMeterNo() + "'");
					String ct = null;
					String pt = null;
					try {
						List<Map<String, Object>> list = jdbcTemplate.queryForList(sql2.toString());
						if(!list.isEmpty()) {
							Map<String, Object> map = list.get(0);
							ct = (String) map.get("CT");
							pt = (String) map.get("PT");
						}
					} catch (Exception e) {
						log.error(ERROR, e.getMessage(), e);
						errorReasons.append(generateErrors(zLine, "A", "CT, PT read query failed - " + e.getMessage()));
						csvErrorPrinter.printRecord(mcc.getErrorRecord(mcc, errorReasons));
						continue;
					}

					// Insert data
					StringBuilder sql3 = new StringBuilder("INSERT INTO sb_dlsj_ydj ")
							.append("(sjid,sjsj, zxygz, zxygz1,zxygz2, zxygz3, zxygz4,zxwgz, dbye,ct,pt)")
							.append(" VALUES ")
							.append("(")
							.append("to_number(" + sjid + "),")
							.append("to_date('" + mcc.getDateTime() + "','yyyy-mm-dd'),")
							.append(""+ mcc.getActiveEnergy1() +",")
							.append(""+ mcc.getActiveEnergy2() +",")
							.append(""+ mcc.getActiveEnergy3() +",")
							.append(""+ mcc.getActiveEnergy4() +",")
							.append(""+ mcc.getActiveEnergy5() +",")
							.append(""+ mcc.getReactiveEnergy() +",")
							.append(""+ ct +",")
							.append(""+ pt +"")
							.append(")");
							
					System.out.println(sql3.toString());
					
					

				}
				

			}

		} catch (IOException e) {
			log.error(ERROR, e.getMessage());
			throw new ServiceException(e.getMessage());
		}


	}



	
	
	

}
