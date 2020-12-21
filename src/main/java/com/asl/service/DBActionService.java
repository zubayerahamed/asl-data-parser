package com.asl.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 * @since Dec 21, 2020
 */
@Component
public interface DBActionService {

	public List<Map<String, Object>> queryForList(String sql);

	public int update(String sql);
}
