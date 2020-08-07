package net.minecraft.util;

import javax.annotation.Nullable;

public class ExceptionCollector<T extends Throwable> {
	@Nullable
	private T result;

	public void add(T throwable) {
		if (this.result == null) {
			this.result = throwable;
		} else {
			this.result.addSuppressed(throwable);
		}
	}

	public void throwIfPresent() throws T {
		if (this.result != null) {
			throw this.result;
		}
	}
}
