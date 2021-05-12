package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class ChainedJsonException extends IOException {
	private final List<ChainedJsonException.Entry> entries = Lists.<ChainedJsonException.Entry>newArrayList();
	private final String message;

	public ChainedJsonException(String string) {
		this.entries.add(new ChainedJsonException.Entry());
		this.message = string;
	}

	public ChainedJsonException(String string, Throwable throwable) {
		super(throwable);
		this.entries.add(new ChainedJsonException.Entry());
		this.message = string;
	}

	public void prependJsonKey(String string) {
		((ChainedJsonException.Entry)this.entries.get(0)).addJsonKey(string);
	}

	public void setFilenameAndFlush(String string) {
		((ChainedJsonException.Entry)this.entries.get(0)).filename = string;
		this.entries.add(0, new ChainedJsonException.Entry());
	}

	public String getMessage() {
		return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
	}

	public static ChainedJsonException forException(Exception exception) {
		if (exception instanceof ChainedJsonException) {
			return (ChainedJsonException)exception;
		} else {
			String string = exception.getMessage();
			if (exception instanceof FileNotFoundException) {
				string = "File not found";
			}

			return new ChainedJsonException(string, exception);
		}
	}

	public static class Entry {
		@Nullable
		String filename;
		private final List<String> jsonKeys = Lists.<String>newArrayList();

		Entry() {
		}

		void addJsonKey(String string) {
			this.jsonKeys.add(0, string);
		}

		@Nullable
		public String getFilename() {
			return this.filename;
		}

		public String getJsonKeys() {
			return StringUtils.join(this.jsonKeys, "->");
		}

		public String toString() {
			if (this.filename != null) {
				return this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys();
			} else {
				return this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys();
			}
		}
	}
}
