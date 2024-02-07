package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;

public record StringTemplate(List<String> segments, List<String> variables) {
	public static StringTemplate fromString(String string, int i) {
		Builder<String> builder = ImmutableList.builder();
		Builder<String> builder2 = ImmutableList.builder();
		int j = string.length();
		int k = 0;
		int l = string.indexOf(36);

		while (l != -1) {
			if (l != j - 1 && string.charAt(l + 1) == '(') {
				builder.add(string.substring(k, l));
				int m = string.indexOf(41, l + 1);
				if (m == -1) {
					throw new IllegalArgumentException("Unterminated macro variable in macro '" + string + "' on line " + i);
				}

				String string2 = string.substring(l + 2, m);
				if (!isValidVariableName(string2)) {
					throw new IllegalArgumentException("Invalid macro variable name '" + string2 + "' on line " + i);
				}

				builder2.add(string2);
				k = m + 1;
				l = string.indexOf(36, k);
			} else {
				l = string.indexOf(36, l + 1);
			}
		}

		if (k == 0) {
			throw new IllegalArgumentException("Macro without variables on line " + i);
		} else {
			if (k != j) {
				builder.add(string.substring(k));
			}

			return new StringTemplate(builder.build(), builder2.build());
		}
	}

	private static boolean isValidVariableName(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '_') {
				return false;
			}
		}

		return true;
	}

	public String substitute(List<String> list) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < this.variables.size(); i++) {
			stringBuilder.append((String)this.segments.get(i)).append((String)list.get(i));
			CommandFunction.checkCommandLineLength(stringBuilder);
		}

		if (this.segments.size() > this.variables.size()) {
			stringBuilder.append((String)this.segments.get(this.segments.size() - 1));
		}

		CommandFunction.checkCommandLineLength(stringBuilder);
		return stringBuilder.toString();
	}
}
