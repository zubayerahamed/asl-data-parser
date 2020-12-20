package com.asl.service.impl;

import com.asl.model.ImportExportHelper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Process extends Thread {

	private ImportExportHelper helper;

	public Process(ImportExportHelper helper) {
		this.helper = helper;
	}

	@Override
	public void run() {
		super.run();
		System.out.println(helper.toString());
	}

}
