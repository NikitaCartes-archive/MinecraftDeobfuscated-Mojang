package net.minecraft.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadingDetector {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String name;
	private final Semaphore lock = new Semaphore(1);
	private final Lock stackTraceLock = new ReentrantLock();
	@Nullable
	private volatile Thread threadThatFailedToAcquire;
	@Nullable
	private volatile ReportedException fullException;

	public ThreadingDetector(String string) {
		this.name = string;
	}

	public void checkAndLock() {
		boolean bl = false;

		try {
			this.stackTraceLock.lock();
			if (!this.lock.tryAcquire()) {
				this.threadThatFailedToAcquire = Thread.currentThread();
				bl = true;
				this.stackTraceLock.unlock();

				try {
					this.lock.acquire();
				} catch (InterruptedException var6) {
					Thread.currentThread().interrupt();
				}

				throw this.fullException;
			}
		} finally {
			if (!bl) {
				this.stackTraceLock.unlock();
			}
		}
	}

	public void checkAndUnlock() {
		try {
			this.stackTraceLock.lock();
			Thread thread = this.threadThatFailedToAcquire;
			if (thread != null) {
				ReportedException reportedException = makeThreadingException(this.name, thread);
				this.fullException = reportedException;
				this.lock.release();
				throw reportedException;
			}

			this.lock.release();
		} finally {
			this.stackTraceLock.unlock();
		}
	}

	public static ReportedException makeThreadingException(String string, @Nullable Thread thread) {
		String string2 = (String)Stream.of(Thread.currentThread(), thread)
			.filter(Objects::nonNull)
			.map(ThreadingDetector::stackTrace)
			.collect(Collectors.joining("\n"));
		CrashReport crashReport = new CrashReport("Accessing " + string + " from multiple threads", new IllegalStateException());
		CrashReportCategory crashReportCategory = crashReport.addCategory("Thread dumps");
		crashReportCategory.setDetail("Thread dumps", string2);
		LOGGER.error("Thread dumps: \n" + string2);
		return new ReportedException(crashReport);
	}

	private static String stackTrace(Thread thread) {
		return thread.getName() + ": \n\tat " + (String)Arrays.stream(thread.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
	}
}
