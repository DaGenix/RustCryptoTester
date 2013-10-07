package com.palmercox.rustcryptotester;

import java.util.Random;

public abstract class Tester {
	public abstract boolean test(final RustCryptRunner runner, final Random rand) throws Exception;
}
