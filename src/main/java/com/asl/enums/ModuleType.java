package com.asl.enums;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
public enum ModuleType {
	DAILY("DAILY","dailyimportexport"), 
	MONTHLY("MONTHLY","monthlyimportexport");

	private String code;
	private String service;

	private ModuleType(String code, String service) {
		this.code = code;
		this.service = service;
	}

	public String getCode() {
		return this.code;
	}

	public String getService() {
		return this.service;
	}
}
