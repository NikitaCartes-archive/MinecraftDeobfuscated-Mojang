package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
	private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
	private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
	private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

	public static String formatTickDuration(long l) {
		long m = l / 20L;
		long n = m / 60L;
		m %= 60L;
		long o = n / 60L;
		n %= 60L;
		return o > 0L ? String.format(Locale.ROOT, "%02d:%02d:%02d", o, n, m) : String.format(Locale.ROOT, "%02d:%02d", n, m);
	}

	public static String stripColor(String string) {
		return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
	}

	public static boolean isNullOrEmpty(@Nullable String string) {
		return StringUtils.isEmpty(string);
	}

	public static String truncateStringIfNecessary(String string, int i, boolean bl) {
		if (string.length() <= i) {
			return string;
		} else {
			return bl && i > 3 ? string.substring(0, i - 3) + "..." : string.substring(0, i);
		}
	}

	public static int lineCount(String string) {
		if (string.isEmpty()) {
			return 0;
		} else {
			Matcher matcher = LINE_PATTERN.matcher(string);
			int i = 1;

			while (matcher.find()) {
				i++;
			}

			return i;
		}
	}

	public static boolean endsWithNewLine(String string) {
		return LINE_END_PATTERN.matcher(string).find();
	}

	public static String trimChatMessage(String string) {
		return truncateStringIfNecessary(string, 256, false);
	}
}
