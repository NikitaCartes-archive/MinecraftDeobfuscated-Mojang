package net.minecraft.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;

@Environment(EnvType.CLIENT)
public class StringDecomposer {
	private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

	private static boolean feedChar(Style style, StringDecomposer.Output output, int i, char c) {
		return Character.isSurrogate(c) ? output.onChar(i, style, 65533) : output.onChar(i, style, c);
	}

	public static boolean iterate(String string, Style style, StringDecomposer.Output output) {
		int i = string.length();

		for (int j = 0; j < i; j++) {
			char c = string.charAt(j);
			if (Character.isHighSurrogate(c)) {
				if (j + 1 >= i) {
					if (!output.onChar(j, style, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(j + 1);
				if (Character.isLowSurrogate(d)) {
					if (!output.onChar(j, style, Character.toCodePoint(c, d))) {
						return false;
					}

					j++;
				} else if (!output.onChar(j, style, 65533)) {
					return false;
				}
			} else if (!feedChar(style, output, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateBackwards(String string, Style style, StringDecomposer.Output output) {
		int i = string.length();

		for (int j = i - 1; j >= 0; j--) {
			char c = string.charAt(j);
			if (Character.isLowSurrogate(c)) {
				if (j - 1 < 0) {
					if (!output.onChar(0, style, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(j - 1);
				if (Character.isHighSurrogate(d)) {
					if (!output.onChar(--j, style, Character.toCodePoint(d, c))) {
						return false;
					}
				} else if (!output.onChar(j, style, 65533)) {
					return false;
				}
			} else if (!feedChar(style, output, j, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(String string, Style style, StringDecomposer.Output output) {
		return iterateFormatted(string, 0, style, output);
	}

	public static boolean iterateFormatted(String string, int i, Style style, StringDecomposer.Output output) {
		return iterateFormatted(string, i, style, style, output);
	}

	public static boolean iterateFormatted(String string, int i, Style style, Style style2, StringDecomposer.Output output) {
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
					if (!output.onChar(k, style3, 65533)) {
						return false;
					}
					break;
				}

				char d = string.charAt(k + 1);
				if (Character.isLowSurrogate(d)) {
					if (!output.onChar(k, style3, Character.toCodePoint(c, d))) {
						return false;
					}

					k++;
				} else if (!output.onChar(k, style3, 65533)) {
					return false;
				}
			} else if (!feedChar(style3, output, k, c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean iterateFormatted(FormattedText formattedText, Style style, StringDecomposer.Output output) {
		return !formattedText.visit((stylex, string) -> iterateFormatted(string, 0, stylex, output) ? Optional.empty() : STOP_ITERATION, style).isPresent();
	}

	public static String filterBrokenSurrogates(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		iterate(string, Style.EMPTY, (i, style, j) -> {
			stringBuilder.appendCodePoint(j);
			return true;
		});
		return stringBuilder.toString();
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Output {
		boolean onChar(int i, Style style, int j);
	}
}
