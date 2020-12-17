package com.asl.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.asl.service.FileReadWriteService;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Service
public class FileReadWriteServiceImpl implements FileReadWriteService {

	@Override
	public List<String> readAllFilesFromDirectory(Path path, String extention) throws IOException {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}

		List<String> result;

		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> !Files.isDirectory(p))
						.map(p -> p.toString().toLowerCase()).filter(f -> f.endsWith(extention))
						.collect(Collectors.toList());
		}

		return result;
	}

}
