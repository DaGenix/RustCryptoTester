package com.palmercox.rustcryptotester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class RustCryptRunner {
	private final File rustExec;
	private final ExecutorService exec;
	
	public RustCryptRunner(final File rustExec) {
		this.rustExec = rustExec;
		exec = Executors.newCachedThreadPool();
	}

	private static final class OutputReader implements Callable<ByteArrayOutputStream> {
		private final InputStream stream;
		
		public OutputReader(final InputStream stream) {
			this.stream = stream;
		}
		
		@Override
		public ByteArrayOutputStream call() throws Exception {
			final ByteArrayOutputStream data = new ByteArrayOutputStream();
			final byte[] buff = new byte[4096];
			int cnt = 0;
			while ((cnt = stream.read(buff)) >= 0) {
				data.write(buff, 0, cnt);
			}
			return data;
		}
	}

	private static String getMessage(final byte[] data) throws Exception {
		final InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(data));
		final StringBuilder sb = new StringBuilder();
		int c = 0;
		while ((c = r.read()) != -1) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	public byte[] runRustCrypt(final byte[] data, final Object... parameters) throws Exception {
		final List<String> params = new ArrayList<>();

		params.add(rustExec.getAbsolutePath());
		
		for (final Object p : parameters) {
			params.add(p.toString());
		}
		
		final ProcessBuilder pb = new ProcessBuilder(params);

		final Process p = pb.start();

		if (data != null) {
			p.getOutputStream().write(data);
		}
		p.getOutputStream().close();
		
		final Future<ByteArrayOutputStream> in = 
				exec.submit(new OutputReader(p.getInputStream()));
		final Future<ByteArrayOutputStream> err = 
				exec.submit(new OutputReader(p.getErrorStream()));
		
		final int result = p.waitFor();

		if(result != 0) {
			throw new RustCryptException(result, getMessage(err.get().toByteArray()));
		}

		return in.get().toByteArray();
	}
	
	public final void close() {
		exec.shutdown();
	}
}
