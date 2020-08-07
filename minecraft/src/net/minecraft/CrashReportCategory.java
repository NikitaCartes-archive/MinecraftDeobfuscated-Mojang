package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory {
	private final CrashReport report;
	private final String title;
	private final List<CrashReportCategory.Entry> entries = Lists.<CrashReportCategory.Entry>newArrayList();
	private StackTraceElement[] stackTrace = new StackTraceElement[0];

	public CrashReportCategory(CrashReport crashReport, String string) {
		this.report = crashReport;
		this.title = string;
	}

	@Environment(EnvType.CLIENT)
	public static String formatLocation(double d, double e, double f) {
		return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", d, e, f, formatLocation(new BlockPos(d, e, f)));
	}

	public static String formatLocation(BlockPos blockPos) {
		return formatLocation(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public static String formatLocation(int i, int j, int k) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			stringBuilder.append(String.format("World: (%d,%d,%d)", i, j, k));
		} catch (Throwable var16) {
			stringBuilder.append("(Error finding world loc)");
		}

		stringBuilder.append(", ");

		try {
			int l = i >> 4;
			int m = k >> 4;
			int n = i & 15;
			int o = j >> 4;
			int p = k & 15;
			int q = l << 4;
			int r = m << 4;
			int s = (l + 1 << 4) - 1;
			int t = (m + 1 << 4) - 1;
			stringBuilder.append(String.format("Chunk: (at %d,%d,%d in %d,%d; contains blocks %d,0,%d to %d,255,%d)", n, o, p, l, m, q, r, s, t));
		} catch (Throwable var15) {
			stringBuilder.append("(Error finding chunk loc)");
		}

		stringBuilder.append(", ");

		try {
			int l = i >> 9;
			int m = k >> 9;
			int n = l << 5;
			int o = m << 5;
			int p = (l + 1 << 5) - 1;
			int q = (m + 1 << 5) - 1;
			int r = l << 9;
			int s = m << 9;
			int t = (l + 1 << 9) - 1;
			int u = (m + 1 << 9) - 1;
			stringBuilder.append(String.format("Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,0,%d to %d,255,%d)", l, m, n, o, p, q, r, s, t, u));
		} catch (Throwable var14) {
			stringBuilder.append("(Error finding world loc)");
		}

		return stringBuilder.toString();
	}

	public CrashReportCategory setDetail(String string, CrashReportDetail<String> crashReportDetail) {
		try {
			this.setDetail(string, crashReportDetail.call());
		} catch (Throwable var4) {
			this.setDetailError(string, var4);
		}

		return this;
	}

	public CrashReportCategory setDetail(String string, Object object) {
		this.entries.add(new CrashReportCategory.Entry(string, object));
		return this;
	}

	public void setDetailError(String string, Throwable throwable) {
		this.setDetail(string, throwable);
	}

	public int fillInStackTrace(int i) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements.length <= 0) {
			return 0;
		} else {
			this.stackTrace = new StackTraceElement[stackTraceElements.length - 3 - i];
			System.arraycopy(stackTraceElements, 3 + i, this.stackTrace, 0, this.stackTrace.length);
			return this.stackTrace.length;
		}
	}

	public boolean validateStackTrace(StackTraceElement stackTraceElement, StackTraceElement stackTraceElement2) {
		if (this.stackTrace.length != 0 && stackTraceElement != null) {
			StackTraceElement stackTraceElement3 = this.stackTrace[0];
			if (stackTraceElement3.isNativeMethod() == stackTraceElement.isNativeMethod()
				&& stackTraceElement3.getClassName().equals(stackTraceElement.getClassName())
				&& stackTraceElement3.getFileName().equals(stackTraceElement.getFileName())
				&& stackTraceElement3.getMethodName().equals(stackTraceElement.getMethodName())) {
				if (stackTraceElement2 != null != this.stackTrace.length > 1) {
					return false;
				} else if (stackTraceElement2 != null && !this.stackTrace[1].equals(stackTraceElement2)) {
					return false;
				} else {
					this.stackTrace[0] = stackTraceElement;
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void trimStacktrace(int i) {
		StackTraceElement[] stackTraceElements = new StackTraceElement[this.stackTrace.length - i];
		System.arraycopy(this.stackTrace, 0, stackTraceElements, 0, stackTraceElements.length);
		this.stackTrace = stackTraceElements;
	}

	public void getDetails(StringBuilder stringBuilder) {
		stringBuilder.append("-- ").append(this.title).append(" --\n");
		stringBuilder.append("Details:");

		for (CrashReportCategory.Entry entry : this.entries) {
			stringBuilder.append("\n\t");
			stringBuilder.append(entry.getKey());
			stringBuilder.append(": ");
			stringBuilder.append(entry.getValue());
		}

		if (this.stackTrace != null && this.stackTrace.length > 0) {
			stringBuilder.append("\nStacktrace:");

			for (StackTraceElement stackTraceElement : this.stackTrace) {
				stringBuilder.append("\n\tat ");
				stringBuilder.append(stackTraceElement);
			}
		}
	}

	public StackTraceElement[] getStacktrace() {
		return this.stackTrace;
	}

	public static void populateBlockDetails(CrashReportCategory crashReportCategory, BlockPos blockPos, @Nullable BlockState blockState) {
		if (blockState != null) {
			crashReportCategory.setDetail("Block", blockState::toString);
		}

		crashReportCategory.setDetail("Block location", (CrashReportDetail<String>)(() -> formatLocation(blockPos)));
	}

	static class Entry {
		private final String key;
		private final String value;

		public Entry(String string, @Nullable Object object) {
			this.key = string;
			if (object == null) {
				this.value = "~~NULL~~";
			} else if (object instanceof Throwable) {
				Throwable throwable = (Throwable)object;
				this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
			} else {
				this.value = object.toString();
			}
		}

		public String getKey() {
			return this.key;
		}

		public String getValue() {
			return this.value;
		}
	}
}
