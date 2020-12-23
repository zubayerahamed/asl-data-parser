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
public class LoadProfileCSVColumns {

	private String meterNo;
	private String dateTime;

	private String aPhaseVoltage;
	private String bPhaseVoltage;
	private String cPhaseVoltage;
	private String aPhaseCurrent;
	private String bPhaseCurrent;
	private String cPhaseCurrent;
	private String aPhaseActivePower1;
	private String aPhaseActivePower2;
	private String aPhaseActivePower3;
	private String frequency;
	private String aPhasePowerFactor;
	private String bPhasePowerFactor;
	private String cPhasePowerFactor;

	private String activeEnergy1;
	private String activeEnergy2;
	private String activeEnergy3;
	private String activeEnergy4;
	private String activeEnergy5;
	private String reactiveEnergy;
	private String meterBalance;

	public LoadProfileCSVColumns(CSVRecord csvRecord, long totalColumnFound) {
		this.meterNo = getRecordValue(csvRecord, totalColumnFound, 0);
		this.dateTime = getRecordValue(csvRecord, totalColumnFound, 1);

		this.aPhaseVoltage = getRecordValue(csvRecord, totalColumnFound, 2);
		this.bPhaseVoltage = getRecordValue(csvRecord, totalColumnFound, 3);
		this.cPhaseVoltage = getRecordValue(csvRecord, totalColumnFound, 4);
		this.aPhaseCurrent = getRecordValue(csvRecord, totalColumnFound, 5);
		this.bPhaseCurrent = getRecordValue(csvRecord, totalColumnFound, 6);
		this.cPhaseCurrent = getRecordValue(csvRecord, totalColumnFound, 7);
		this.aPhaseActivePower1 = getRecordValue(csvRecord, totalColumnFound, 8);
		this.aPhaseActivePower2 = getRecordValue(csvRecord, totalColumnFound, 9);
		this.aPhaseActivePower3 = getRecordValue(csvRecord, totalColumnFound, 10);
		this.frequency = getRecordValue(csvRecord, totalColumnFound, 11);
		this.aPhasePowerFactor = getRecordValue(csvRecord, totalColumnFound, 12);
		this.bPhasePowerFactor = getRecordValue(csvRecord, totalColumnFound, 13);
		this.cPhasePowerFactor = getRecordValue(csvRecord, totalColumnFound, 14);

		this.activeEnergy1 = getRecordValue(csvRecord, totalColumnFound, 15);
		this.activeEnergy2 = getRecordValue(csvRecord, totalColumnFound, 16);
		this.activeEnergy3 = getRecordValue(csvRecord, totalColumnFound, 17);
		this.activeEnergy4 = getRecordValue(csvRecord, totalColumnFound, 18);
		this.activeEnergy5 = getRecordValue(csvRecord, totalColumnFound, 19);
		this.reactiveEnergy = getRecordValue(csvRecord, totalColumnFound, 20);
		this.meterBalance = getRecordValue(csvRecord, totalColumnFound, 21);
	}

	private String getRecordValue(CSVRecord csvRecord, long totalNumberOfColumn, int limit) {
		return totalNumberOfColumn > limit ? csvRecord.get(limit) : "";
	}

	public void validaeColumns(LoadProfileCSVColumns lpcc, StringBuilder errorReasons, int rowNumber) {
		if(StringUtils.isBlank(lpcc.getMeterNo())) {
			errorReasons.append(generateErrors(rowNumber, "A", "Meter number required"));
		}
		if(StringUtils.isBlank(lpcc.getDateTime())) {
			errorReasons.append(generateErrors(rowNumber, "B", "Date Time required"));
		}
	}

	private String generateErrors(int rowNumber, String column, String reason) {
		return "Line " + rowNumber + " - Column " + column + " - Reason : " + reason + ", ";
	}

	public List<String> getErrorRecord(LoadProfileCSVColumns lpcc, StringBuilder errorReasons){
		List<String> errorRecord = new ArrayList<>();
		getRecord(errorRecord, lpcc);
		errorRecord.add(errorReasons.toString());
		return errorRecord;
	}

	public List<String> getSuccessRecord(LoadProfileCSVColumns lpcc){
		List<String> successRecord = new ArrayList<>();
		getRecord(successRecord, lpcc);
		return successRecord;
	}

	private void getRecord(List<String> record, LoadProfileCSVColumns lpcc) {
		record.add(lpcc.getMeterNo());
		record.add(lpcc.getDateTime());

		record.add(lpcc.getAPhaseVoltage());
		record.add(lpcc.getBPhaseVoltage());
		record.add(lpcc.getCPhaseVoltage());
		record.add(lpcc.getAPhaseCurrent());
		record.add(lpcc.getBPhaseCurrent());
		record.add(lpcc.getCPhaseCurrent());
		record.add(lpcc.getAPhaseActivePower1());
		record.add(lpcc.getAPhaseActivePower2());
		record.add(lpcc.getAPhaseActivePower3());
		record.add(lpcc.getFrequency());
		record.add(lpcc.getAPhasePowerFactor());
		record.add(lpcc.getBPhasePowerFactor());
		record.add(lpcc.getCPhasePowerFactor());

		record.add(lpcc.getActiveEnergy1());
		record.add(lpcc.getActiveEnergy2());
		record.add(lpcc.getActiveEnergy3());
		record.add(lpcc.getActiveEnergy4());
		record.add(lpcc.getActiveEnergy5());
		record.add(lpcc.getReactiveEnergy());
		record.add(lpcc.getMeterBalance());
	}
}
