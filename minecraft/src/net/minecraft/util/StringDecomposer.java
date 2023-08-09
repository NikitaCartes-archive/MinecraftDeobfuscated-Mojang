package net.minecraft.util;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class StringDecomposer {
	private static final char REPLACEMENT_CHAR = '�';
	private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

	private static boolean feedChar(Style style, FormattedCharSink formattedCharSink, int i, char c) {
		return Character.isSurrogate(c) ? formattedCharSink.accept(i, style, 65533) : formattedCharSink.accept(i, style, c);
	}

	public static boolean iterate(String string, Style style, FormattedCharSink formattedCharSink) {
		int i = string.length();

		for (int j = 0; j < i; j++) {
			char c = string.charAt(j);
			if (Character.isHighSurrogate(c)) {
				if (j + 1 >= i) {
					if (!formattedCharSink.accept(j, style, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(j + 1);
				if (Character.isLowSurrogate(d)) {
					if (!formattedCharSink.accept(j, style, Character.toCodePoint(c, d))) {
						return false;
					}

					j++;
				} else if (!formattedCharSink.accept(j, style, 65533)) {
					return false;
				}
			} else if (!feedChar(style, formattedCharSink, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateBackwards(String string, Style style, FormattedCharSink formattedCharSink) {
		int i = string.length();

		for (int j = i - 1; j >= 0; j--) {
			char c = string.charAt(j);
			if (Character.isLowSurrogate(c)) {
				if (j - 1 < 0) {
					if (!formattedCharSink.accept(0, style, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(j - 1);
				if (Character.isHighSurrogate(d)) {
					if (!formattedCharSink.accept(--j, style, Character.toCodePoint(d, c))) {
						return false;
					}
				} else if (!formattedCharSink.accept(j, style, 65533)) {
					return false;
				}
			} else if (!feedChar(style, formattedCharSink, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(String string, Style style, FormattedCharSink formattedCharSink) {
		return iterateFormatted(string, 0, style, formattedCharSink);
	}

	public static boolean iterateFormatted(String string, int i, Style style, FormattedCharSink formattedCharSink) {
		return iterateFormatted(string, i, style, style, formattedCharSink);
	}

	public static boolean iterateFormatted(String string, int i, Style style, Style style2, FormattedCharSink formattedCharSink) {
		int j = string.length();
		Style style3 = style;

		for (int k = i; k < j; k++) {
			char c = string.charAt(k);
			if (c == 167) {
				if (k + 1 >= j) {
					break;
				}

				char d = string.charAt(k + 1);
				ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
				if (chatFormatting != null) {
					style3 = chatFormatting == ChatFormatting.RESET ? style2 : style3.applyLegacyFormat(chatFormatting);
				}

				k++;
			} else if (Character.isHighSurrogate(c)) {
				if (k + 1 >= j) {
					if (!formattedCharSink.accept(k, style3, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(k + 1);
				if (Character.isLowSurrogate(d)) {
					if (!formattedCharSink.accept(k, style3, Character.toCodePoint(c, d))) {
						return false;
					}

					k++;
				} else if (!formattedCharSink.accept(k, style3, 65533)) {
					return false;
				}
			} else if (!feedChar(style3, formattedCharSink, k, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(FormattedText formattedText, Style style, FormattedCharSink formattedCharSink) {
		return formattedText.visit((stylex, string) -> iterateFormatted(string, 0, stylex, formattedCharSink) ? Optional.empty() : STOP_ITERATION, style).isEmpty();
	}

	public static String filterBrokenSurrogates(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		iterate(string, Style.EMPTY, (i, style, j) -> {
			stringBuilder.appendCodePoint(j);
			return true;
		});
		return stringBuilder.toString();
	}

	public static String getPlainText(FormattedText formattedText) {
		StringBuilder stringBuilder = new StringBuilder();
		iterateFormatted(formattedText, Style.EMPTY, (i, style, j) -> {
			stringBuilder.appendCodePoint(j);
			return true;
		});
		return stringBuilder.toString();
	}
}
