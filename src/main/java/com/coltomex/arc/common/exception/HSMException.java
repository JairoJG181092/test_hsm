package com.coltomex.arc.common.exception;

public class HSMException extends Exception {
	private static final long serialVersionUID = 1L;

	public HSMException() {
		super();
	}

	public HSMException(String msg) {
		super(msg);
	}

	public HSMException(String message, Throwable cause) {
		super(message, cause);
	}
}
