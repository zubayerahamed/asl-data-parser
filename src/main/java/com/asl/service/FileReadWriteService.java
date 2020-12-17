package com.asl.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Component
public interface FileReadWriteService {

	/**
	 * Read all files from directory
	 * @param directory
	 * @return
	 */
	public List<String> readAllFilesFromDirectory(Path path, String extention) throws IOException;
}
