package com.asl;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.asl.service.FileReadWriteService;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Component
public class ProcessStarter implements CommandLineRunner {

	@Autowired private FileReadWriteService fileReadWriteService;

	@Value("${file.server.read.location}")
	private String filesLocation;

	@Override
	public void run(String... args) throws Exception {
		List<String> files = fileReadWriteService.readAllFilesFromDirectory(Paths.get(filesLocation), "csv");
		
		
		files.stream().forEach(f -> {
			System.out.println(f);
		});
	}

}
