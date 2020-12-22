package com.asl.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asl.enums.FileType;
import com.asl.model.DailyCSVColumns;
import com.asl.model.ImportExportHelper;
import com.asl.model.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Slf4j
@Service("dailyimportexportService")
public class DailyImportExport extends AbstractImportExport {

	@Autowired private DailyDBActionService dbService;

	@Override
	public void processCSV(ImportExportHelper helper) throws ServiceException {
		log.debug("Running daily service, Thread : {}, File name : {}", helper.getThreadName(), helper.getFileName());
		log.debug("Import export helper : {}", helper);

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
			int zLine = helper.isFirstRowHeader() ? 1 : 0;
			CSVFormat csvFormat = CSVFormat.EXCEL.withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true);
			if(helper.isFirstRowHeader()) {
				csvFormat = CSVFormat.EXCEL.withHeader().withTrim().withDelimiter(helper.getDelimeterType()).withIgnoreEmptyLines(true).withIgnoreHeaderCase(true);
			}
			try (Reader reader = Files.newBufferedReader(Paths.get(fileNameWithPath), StandardCharsets.ISO_8859_1);
					CSVParser csvParser = new CSVParser(reader, csvFormat)) {

				// Loop through each record line
				for (CSVRecord csvRecord : csvParser) {
					++zLine;
					long totalColumnFound = csvRecord.size();
					StringBuilder errorReasons = new StringBuilder();

					DailyCSVColumns dcc = new DailyCSVColumns(csvRecord, totalColumnFound);

					// Validate columns
					dcc.validaeColumns(dcc, errorReasons, zLine);

					// If column has error then write to error file and continue for next record
					if(StringUtils.isNotBlank(errorReasons)) {
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}

					// Get SJID using meter number
					StringBuilder sql1 = new StringBuilder("SELECT SJID FROM SB_SJZB WHERE SFYX=1 AND CLDJH='" + dcc.getMeterNo() + "'");
					String sjid = null;
					try {
						List<Map<String, Object>> list = dbService.queryForList(sql1.toString());
						if(!list.isEmpty()) {
							sjid = ((BigDecimal) list.get(0).get("SJID")).toPlainString().trim();
						}
					} catch (Exception e) {
						log.error(ERROR, e.getMessage(), e);
						errorReasons.append(generateErrors(zLine, "A", "SJID read query failed - " + e.getMessage()));
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}
					if(StringUtils.isBlank(sjid)) {
						errorReasons.append(generateErrors(zLine, "A", "Sequence number not found using meter number " + dcc.getMeterNo()));
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}

					// Get CT, PT using meter number
					StringBuilder sql2 = new StringBuilder("SELECT CT,PT FROM DA_BJ WHERE BJJH='" + dcc.getMeterNo() + "'");
					String ct = null;
					String pt = null;
					try {
						List<Map<String, Object>> list = dbService.queryForList(sql2.toString());
						if(!list.isEmpty()) {
							Map<String, Object> map = list.get(0);
							ct = (String) map.get("CT");
							pt = (String) map.get("PT");
						}
					} catch (Exception e) {
						log.error(ERROR, e.getMessage(), e);
						errorReasons.append(generateErrors(zLine, "A", "CT, PT read query failed - " + e.getMessage()));
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}

					// Insert data
					StringBuilder sql3 = new StringBuilder("INSERT INTO sb_dlsj ")
							.append("(sjid, sjsj, zxygz, zxygz1, zxygz2, zxygz3, zxygz4, zxwgz, dbye, ct, pt)")
							.append(" VALUES ")
							.append("(")
							.append("to_number(" + sjid + "),")
							.append("to_date('" + dcc.getDateTime() + "','yyyy-mm-dd'),")
							.append(""+ getNullIfNotExist(dcc.getActiveEnergy1()) +",")
							.append(""+ getNullIfNotExist(dcc.getActiveEnergy2()) +",")
							.append(""+ getNullIfNotExist(dcc.getActiveEnergy3()) +",")
							.append(""+ getNullIfNotExist(dcc.getActiveEnergy4()) +",")
							.append(""+ getNullIfNotExist(dcc.getActiveEnergy5()) +",")
							.append(""+ getNullIfNotExist(dcc.getReactiveEnergy()) +",")
							.append(""+ getNullIfNotExist(dcc.getMeterBalance()) +",")
							.append(""+ getNullIfNotExist(ct) +",")
							.append(""+ getNullIfNotExist(pt) +"")
							.append(")");

					int count = 0;
					try {
						count = dbService.update(sql3.toString());
					} catch (Exception e) {
						log.error(ERROR, e.getMessage(), e);
						errorReasons.append(generateErrors(zLine, "", "Data not inserted - " + e.getMessage()));
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}
					if(count < 1) {
						errorReasons.append(generateErrors(zLine, "", "Data not inserted"));
						csvErrorPrinter.printRecord(dcc.getErrorRecord(dcc, errorReasons));
						continue;
					}

					// If data inserted successfully, then write this record to success file
					csvSuccessPrinter.printRecord(dcc.getSuccessRecord(dcc));

				}

			}

		} catch (IOException e) {
			log.error(ERROR, e.getMessage());
			throw new ServiceException(e.getMessage());
		}
	}

	private String getNullIfNotExist(String val) {
		if(StringUtils.isBlank(val)) return null;
		return val;
	}
}
