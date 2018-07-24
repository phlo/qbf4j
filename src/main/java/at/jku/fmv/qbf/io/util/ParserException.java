package at.jku.fmv.qbf.io.util;

import java.nio.file.Path;

public class ParserException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static String format(Path file, String msg, int line) {
		return file.toFile()
			+ (line < 0
				? ""
				: ": " + line)
			+ ": error: " + msg;
	}

	public ParserException(Throwable cause) {
		super(cause);
	}

	public ParserException(Path file, String msg) {
		super(format(file, msg, -1));
	}

	public ParserException(Path file, String msg, int line) {
		super(format(file, msg, line));
	}

	public ParserException(Path file, int line, Throwable cause) {
		super(format(file, cause.getMessage(), line), cause);
	}

	public ParserException(Path file, String msg, Throwable cause) {
		super(format(file, msg, -1), cause);
	}

	public ParserException(Path file, String msg, int line, Throwable cause) {
		super(format(file, msg, line), cause);
	}
}
