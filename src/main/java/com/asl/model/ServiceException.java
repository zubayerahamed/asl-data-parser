package com.asl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Zubayer Ahamed
 * @since Dec 19, 2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServiceException extends Exception {

	private static final long serialVersionUID = 1481278365683480722L;

	private String code;
	private String message;

	public ServiceException(String message) {
		super(message);
		this.message = message;
	}

	public ServiceException(String code, String message) {
		super(message);
		this.message = message;
		this.code = code;
	}

	public ServiceException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.message = message;
	}
}
