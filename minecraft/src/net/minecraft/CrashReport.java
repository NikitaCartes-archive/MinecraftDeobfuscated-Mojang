package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.util.MemoryReserve;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class CrashReport {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
	private final String title;
	private final Throwable exception;
	private final List<CrashReportCategory> details = Lists.<CrashReportCategory>newArrayList();
	@Nullable
	private Path saveFile;
	private boolean trackingStackTrace = true;
	private StackTraceElement[] uncategorizedStackTrace = new StackTraceElement[0];
	private final SystemReport systemReport = new SystemReport();

	public CrashReport(String string, Throwable throwable) {
		this.title = string;
		this.exception = throwable;
	}

	public String getTitle() {
		return this.title;
	}

	public Throwable getException() {
		return this.exception;
	}

	public String getDetails() {
		StringBuilder stringBuilder = new StringBuilder();
		this.getDetails(stringBuilder);
		return stringBuilder.toString();
	}

	public void getDetails(StringBuilder stringBuilder) {
		if ((this.uncategorizedStackTrace == null || this.uncategorizedStackTrace.length <= 0) && !this.details.isEmpty()) {
			this.uncategorizedStackTrace = ArrayUtils.subarray(((CrashReportCategory)this.details.get(0)).getStacktrace(), 0, 1);
		}

		if (this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0) {
			stringBuilder.append("-- Head --\n");
			stringBuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
			stringBuilder.append("Stacktrace:\n");

			for (StackTraceElement stackTraceElement : this.uncategorizedStackTrace) {
				stringBuilder.append("\t").append("at ").append(stackTraceElement);
				stringBuilder.append("\n");
			}

			stringBuilder.append("\n");
		}

		for (CrashReportCategory crashReportCategory : this.details) {
			crashReportCategory.getDetails(stringBuilder);
			stringBuilder.append("\n\n");
		}

		this.systemReport.appendToCrashReportString(stringBuilder);
	}

	public String getExceptionMessage() {
		StringWriter stringWriter = null;
		PrintWriter printWriter = null;
		Throwable throwable = this.exception;
		if (throwable.getMessage() == null) {
			if (throwable instanceof NullPointerException) {
				throwable = new NullPointerException(this.title);
			} else if (throwable instanceof StackOverflowError) {
				throwable = new StackOverflowError(this.title);
			} else if (throwable instanceof OutOfMemoryError) {
				throwable = new OutOfMemoryError(this.title);
			}

			throwable.setStackTrace(this.exception.getStackTrace());
		}

		String var4;
		try {
			stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);
			var4 = stringWriter.toString();
		} finally {
			IOUtils.closeQuietly(stringWriter);
			IOUtils.closeQuietly(printWriter);
		}

		return var4;
	}

	public String getFriendlyReport(ReportType reportType, List<String> list) {
		StringBuilder stringBuilder = new StringBuilder();
		reportType.appendHeader(stringBuilder, list);
		stringBuilder.append("Time: ");
		stringBuilder.append(DATE_TIME_FORMATTER.format(ZonedDateTime.now()));
		stringBuilder.append("\n");
		stringBuilder.append("Description: ");
		stringBuilder.append(this.title);
		stringBuilder.append("\n\n");
		stringBuilder.append(this.getExceptionMessage());
		stringBuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

		for (int i = 0; i < 87; i++) {
			stringBuilder.append("-");
		}

		stringBuilder.append("\n\n");
		this.getDetails(stringBuilder);
		return stringBuilder.toString();
	}

	public String getFriendlyReport(ReportType reportType) {
		return this.getFriendlyReport(reportType, List.of());
	}

	@Nullable
	public Path getSaveFile() {
		return this.saveFile;
	}

	public boolean saveToFile(Path path, ReportType reportType, List<String> list) {
		if (this.saveFile != null) {
			return false;
		} else {
			try {
				if (path.getParent() != null) {
					FileUtil.createDirectoriesSafe(path.getParent());
				}

				Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

				try {
					writer.write(this.getFriendlyReport(reportType, list));
				} catch (Throwable var8) {
					if (writer != null) {
						try {
							writer.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (writer != null) {
					writer.close();
				}

				this.saveFile = path;
				return true;
			} catch (Throwable var9) {
				LOGGER.error("Could not save crash report to {}", path, var9);
				return false;
			}
		}
	}

	public boolean saveToFile(Path path, ReportType reportType) {
		return this.saveToFile(path, reportType, List.of());
	}

	public SystemReport getSystemReport() {
		return this.systemReport;
	}

	public CrashReportCategory addCategory(String string) {
		return this.addCategory(string, 1);
	}

	public CrashReportCategory addCategory(String string, int i) {
		CrashReportCategory crashReportCategory = new CrashReportCategory(string);
		if (this.trackingStackTrace) {
			int j = crashReportCategory.fillInStackTrace(i);
			StackTraceElement[] stackTraceElements = this.exception.getStackTrace();
			StackTraceElement stackTraceElement = null;
			StackTraceElement stackTraceElement2 = null;
			int k = stackTraceElements.length - j;
			if (k < 0) {
				LOGGER.error("Negative index in crash report handler ({}/{})", stackTraceElements.length, j);
			}

			if (stackTraceElements != null && 0 <= k && k < stackTraceElements.length) {
				stackTraceElement = stackTraceElements[k];
				if (stackTraceElements.length + 1 - j < stackTraceElements.length) {
					stackTraceElement2 = stackTraceElements[stackTraceElements.length + 1 - j];
				}
			}

			this.trackingStackTrace = crashReportCategory.validateStackTrace(stackTraceElement, stackTraceElement2);
			if (stackTraceElements != null && stackTraceElements.length >= j && 0 <= k && k < stackTraceElements.length) {
				this.uncategorizedStackTrace = new StackTraceElement[k];
				System.arraycopy(stackTraceElements, 0, this.uncategorizedStackTrace, 0, this.uncategorizedStackTrace.length);
			} else {
				this.trackingStackTrace = false;
			}
		}

		this.details.add(crashReportCategory);
		return crashReportCategory;
	}

	public static CrashReport forThrowable(Throwable throwable, String string) {
		while (throwable instanceof CompletionException && throwable.getCause() != null) {
			throwable = throwable.getCause();
		}

		CrashReport crashReport;
		if (throwable instanceof ReportedException reportedException) {
			crashReport = reportedException.getReport();
		} else {
			crashReport = new CrashReport(string, throwable);
		}

		return crashReport;
	}

	public static void preload() {
		MemoryReserve.allocate();
		new CrashReport("Don't panic!", new Throwable()).getFriendlyReport(ReportType.CRASH);
	}
}
