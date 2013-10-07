package com.palmercox.rustcryptotester;

class RustCryptException extends Exception {
	private static final long serialVersionUID = 1L;

	private final int code;

	public RustCryptException(final int code, final String msg) {
		super(msg);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
