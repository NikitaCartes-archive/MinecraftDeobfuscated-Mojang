package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class ThreadingDetector {
	public static void checkAndLock(Semaphore semaphore, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> debugBuffer, String string) {
		boolean bl = semaphore.tryAcquire();
		if (!bl) {
			throw makeThreadingException(string, debugBuffer);
		}
	}

	public static ReportedException makeThreadingException(String string, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> debugBuffer) {
		String string2 = (String)Thread.getAllStackTraces()
			.keySet()
			.stream()
			.filter(Objects::nonNull)
			.map(thread -> thread.getName() + ": \n\tat " + (String)Arrays.stream(thread.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat ")))
			.collect(Collectors.joining("\n"));
		CrashReport crashReport = new CrashReport("Accessing " + string + " from multiple threads", new IllegalStateException());
		CrashReportCategory crashReportCategory = crashReport.addCategory("Thread dumps");
		crashReportCategory.setDetail("Thread dumps", string2);
		if (debugBuffer != null) {
			StringBuilder stringBuilder = new StringBuilder();

			for (Pair<Thread, StackTraceElement[]> pair : debugBuffer.dump()) {
				stringBuilder.append("Thread ")
					.append(pair.getFirst().getName())
					.append(": \n\tat ")
					.append((String)Arrays.stream(pair.getSecond()).map(Object::toString).collect(Collectors.joining("\n\tat ")))
					.append("\n");
			}

			crashReportCategory.setDetail("Last threads", stringBuilder.toString());
		}

		return new ReportedException(crashReport);
	}
}
