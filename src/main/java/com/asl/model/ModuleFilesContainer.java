package com.asl.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author Zubayer Ahamed
 * @since Dec 20, 2020
 */
@Data
public class ModuleFilesContainer {

	private Map<String, String> dailyFiles = new HashMap<>();
	private Map<String, String> monthlyFiles = new HashMap<>();
	private Map<String, String> eventFiles = new HashMap<>();
	private Map<String, String> loadProfileFiles = new HashMap<>();
}
