package com.palmercox.rustcryptotester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Hex;

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

public final class RustCryptRunner {
	
	private final File rustExec;
	
	public RustCryptRunner(final File rustExec) {
		this.rustExec = rustExec;
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
	
	public byte[] runRustCrypt(final Object... parameters) throws Exception {

		final List<String> smallParams = new ArrayList<>();
		final List<byte[]> largeParams = new ArrayList<>();

		smallParams.add(rustExec.getAbsolutePath());
		
		for (final Object p : parameters) {
			if (p instanceof String) {
				smallParams.add(p.toString());
			} else if (p instanceof Integer) {
				smallParams.add(p.toString());
			} else if (p instanceof byte[]) {
				final byte[] b = (byte[]) p;
				if (b.length <= 32) {
					smallParams.add(Hex.encodeHexString(b));
				} else {
					smallParams.add("-");
					largeParams.add(b);
				}
			}
		}
		
		final ProcessBuilder pb = new ProcessBuilder(smallParams);

		final Process p = pb.start();

		// Write all the large inputs
		if (!largeParams.isEmpty()) {
			final DataOutputStream out = new DataOutputStream(p.getOutputStream());
			for (final byte[] d : largeParams) {
				out.writeInt(d.length);
				out.write(d);
			}
			out.flush();
		}
		p.getOutputStream().close();
		
		final ExecutorService exec = Executors.newFixedThreadPool(2);
		try {
			final Future<ByteArrayOutputStream> in = 
					exec.submit(new OutputReader(p.getInputStream()));
			final Future<ByteArrayOutputStream> err = 
					exec.submit(new OutputReader(p.getErrorStream()));
			
			final int result = p.waitFor();
	
			if(result != 0) {
				throw new RustCryptException(result, getMessage(err.get().toByteArray()));
			}
	
			return in.get().toByteArray();
		} finally {
			exec.shutdown();
		}
	}
}
