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

	public static String formatTickDuration(int i, float f) {
		int j = Mth.floor((float)i / f);
		int k = j / 60;
		j %= 60;
		int l = k / 60;
		k %= 60;
		return l > 0 ? String.format(Locale.ROOT, "%02d:%02d:%02d", l, k, j) : String.format(Locale.ROOT, "%02d:%02d", k, j);
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

	public static boolean isAllowedChatCharacter(char c) {
		return c != 167 && c >= ' ' && c != 127;
	}

	public static boolean isValidPlayerName(String string) {
		return string.length() > 16 ? false : string.chars().filter(i -> i <= 32 || i >= 127).findAny().isEmpty();
	}

	public static String filterText(String string) {
		return filterText(string, false);
	}

	public static String filterText(String string, boolean bl) {
		StringBuilder stringBuilder = new StringBuilder();

		for (char c : string.toCharArray()) {
			if (isAllowedChatCharacter(c)) {
				stringBuilder.append(c);
			} else if (bl && c == '\n') {
				stringBuilder.append(c);
			}
		}

		return stringBuilder.toString();
	}

	public static boolean isWhitespace(int i) {
		return Character.isWhitespace(i) || Character.isSpaceChar(i);
	}

	public static boolean isBlank(@Nullable String string) {
		return string != null && !string.isEmpty() ? string.chars().allMatch(StringUtil::isWhitespace) : true;
	}
}
