package com.asl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 * @since Dec 17, 2020
 */
@Component
public class AbstractService {

	@Autowired protected JdbcTemplate jdbcTemplate;
}
