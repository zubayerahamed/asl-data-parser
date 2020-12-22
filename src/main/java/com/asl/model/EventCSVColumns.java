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
public class EventCSVColumns {

	private String meterNo;
	private String cimCode;
	private String eventDate;

	public EventCSVColumns(CSVRecord csvRecord, long totalColumnFound) {
		this.meterNo = getRecordValue(csvRecord, totalColumnFound, 0);
		this.cimCode = getRecordValue(csvRecord, totalColumnFound, 1);
		this.eventDate = getRecordValue(csvRecord, totalColumnFound, 2);
	}

	private String getRecordValue(CSVRecord csvRecord, long totalNumberOfColumn, int limit) {
		return totalNumberOfColumn > limit ? csvRecord.get(limit) : "";
	}

	public void validaeColumns(EventCSVColumns mcc, StringBuilder errorReasons, int rowNumber) {
		if(StringUtils.isBlank(mcc.getMeterNo())) {
			errorReasons.append(generateErrors(rowNumber, "A", "Meter number required"));
		}
		if(StringUtils.isBlank(mcc.getCimCode())) {
			errorReasons.append(generateErrors(rowNumber, "B", "Event code required"));
		}
	}

	private String generateErrors(int rowNumber, String column, String reason) {
		return "Line " + rowNumber + " - Column " + column + " - Reason : " + reason + ", ";
	}

	public List<String> getErrorRecord(EventCSVColumns mcc, StringBuilder errorReasons){
		List<String> errorRecord = new ArrayList<>();
		getRecord(errorRecord, mcc);
		errorRecord.add(errorReasons.toString());
		return errorRecord;
	}

	public List<String> getSuccessRecord(EventCSVColumns mcc){
		List<String> successRecord = new ArrayList<>();
		getRecord(successRecord, mcc);
		return successRecord;
	}

	private void getRecord(List<String> record, EventCSVColumns mcc) {
		record.add(mcc.getMeterNo());
		record.add(mcc.getCimCode());
		record.add(mcc.getEventDate());
	}
}
