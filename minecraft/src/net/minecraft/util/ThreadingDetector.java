package net.minecraft.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class ThreadingDetector {
	public static void checkAndLock(ReentrantLock reentrantLock, String string) {
		if (reentrantLock.isLocked() && !reentrantLock.isHeldByCurrentThread()) {
			throw makeThreadingException(string);
		} else {
			reentrantLock.lock();
		}
	}

	public static ReportedException makeThreadingException(String string) {
		String string2 = (String)Thread.getAllStackTraces()
			.keySet()
			.stream()
			.filter(Objects::nonNull)
			.map(thread -> thread.getName() + ": \n\tat " + (String)Arrays.stream(thread.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat ")))
			.collect(Collectors.joining("\n"));
		CrashReport crashReport = new CrashReport("Accessing " + string + " from multiple threads", new IllegalStateException());
		CrashReportCategory crashReportCategory = crashReport.addCategory("Thread dumps");
		crashReportCategory.setDetail("Thread dumps", string2);
		return new ReportedException(crashReport);
	}
}
