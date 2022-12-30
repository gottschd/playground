package org.gottschd.stax.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class ByteArrayOutputInputStream extends ByteArrayOutputStream {

	public ByteArrayOutputInputStream() {
		super();
	}

	public InputStream wrapToInputStream() {
		return new ByteArrayInputStream(buf, 0, count);
	}

}
