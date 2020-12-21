package com.asl.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zubayer Ahamed
 * @since Dec 20, 2020
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCSVColumns {

	private String meterNo;
	private String dateTime;
	private String activeEnergy1;
	private String activeEnergy2;
	private String activeEnergy3;
	private String activeEnergy4;
	private String activeEnergy5;
	private String reactiveEnergy;
	private String meterBalance;

	public MonthlyCSVColumns(CSVRecord csvRecord, long totalColumnFound) {
		this.meterNo = getRecordValue(csvRecord, totalColumnFound, 0);
		this.dateTime = getRecordValue(csvRecord, totalColumnFound, 1);
		this.activeEnergy1 = getRecordValue(csvRecord, totalColumnFound, 2);
		this.activeEnergy2 = getRecordValue(csvRecord, totalColumnFound, 3);
		this.activeEnergy3 = getRecordValue(csvRecord, totalColumnFound, 4);
		this.activeEnergy4 = getRecordValue(csvRecord, totalColumnFound, 5);
		this.activeEnergy5 = getRecordValue(csvRecord, totalColumnFound, 6);
		this.reactiveEnergy = getRecordValue(csvRecord, totalColumnFound, 7);
		this.meterBalance = getRecordValue(csvRecord, totalColumnFound, 8);
	}

	private String getRecordValue(CSVRecord csvRecord, long totalNumberOfColumn, int limit) {
		return totalNumberOfColumn > limit ? csvRecord.get(limit) : "";
	}

	public void validaeColumns(MonthlyCSVColumns mcc, StringBuilder errorReasons, int rowNumber) {
		if(StringUtils.isBlank(mcc.getMeterNo())) {
			errorReasons.append(generateErrors(rowNumber, "A", "Meter number required"));
		}
		if(StringUtils.isBlank(mcc.getDateTime())) {
			errorReasons.append(generateErrors(rowNumber, "B", "Date Time required"));
		}
	}

	private String generateErrors(int rowNumber, String column, String reason) {
		return "Line " + rowNumber + " - Column " + column + " - Reason : " + reason + ", ";
	}

	public List<String> getErrorRecord(MonthlyCSVColumns mcc, StringBuilder errorReasons){
		List<String> errorRecord = new ArrayList<>();
		getRecord(errorRecord, mcc);
		errorRecord.add(errorReasons.toString());
		return errorRecord;
	}

	public List<String> getSuccessRecord(MonthlyCSVColumns mcc){
		List<String> successRecord = new ArrayList<>();
		getRecord(successRecord, mcc);
		return successRecord;
	}

	private void getRecord(List<String> record, MonthlyCSVColumns mcc) {
		record.add(mcc.getMeterNo());
		record.add(mcc.getDateTime());
		record.add(mcc.getActiveEnergy1());
		record.add(mcc.getActiveEnergy2());
		record.add(mcc.getActiveEnergy3());
		record.add(mcc.getActiveEnergy4());
		record.add(mcc.getActiveEnergy5());
		record.add(mcc.getReactiveEnergy());
		record.add(mcc.getMeterBalance());
	}
}
