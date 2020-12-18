package com.asl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.asl.service.impl.MonthlyFileReadWriteServiceImpl;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Component
public class ProcessStarter implements CommandLineRunner {

	private Map<String, String> filesMap = new HashMap<>();

	@Autowired private MonthlyFileReadWriteServiceImpl fileReadWriteService;

	@Override
	public void run(String... args) throws Exception {
		fileReadWriteService.startProcess(filesMap);
		System.out.println(filesMap.size());
	}

}
