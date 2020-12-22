package com.asl.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asl.service.DBActionService;

/**
 * @author Zubayer Ahamed
 * @since Dec 21, 2020
 */
@Service
public class DailyDBActionService implements DBActionService {

	@Autowired protected JdbcTemplate jdbcTemplate;

	@Override
	public List<Map<String, Object>> queryForList(String sql) {
		return jdbcTemplate.queryForList(sql);
	}

	@Override
	@Transactional
	public int update(String sql) {
		return jdbcTemplate.update(sql);
	}

}
