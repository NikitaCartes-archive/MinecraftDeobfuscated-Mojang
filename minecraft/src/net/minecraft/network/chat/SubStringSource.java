package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
	private final String plainText;
	private final List<Style> charStyles;
	private final Int2IntFunction reverseCharModifier;

	private SubStringSource(String string, List<Style> list, Int2IntFunction int2IntFunction) {
		this.plainText = string;
		this.charStyles = ImmutableList.copyOf(list);
		this.reverseCharModifier = int2IntFunction;
	}

	public String getPlainText() {
		return this.plainText;
	}

	public List<FormattedCharSequence> substring(int i, int j, boolean bl) {
		if (j == 0) {
			return ImmutableList.of();
		} else {
			List<FormattedCharSequence> list = Lists.<FormattedCharSequence>newArrayList();
			Style style = (Style)this.charStyles.get(i);
			int k = i;

			for (int l = 1; l < j; l++) {
				int m = i + l;
				Style style2 = (Style)this.charStyles.get(m);
				if (!style2.equals(style)) {
					String string = this.plainText.substring(k, m);
					list.add(bl ? FormattedCharSequence.backward(string, style, this.reverseCharModifier) : FormattedCharSequence.forward(string, style));
					style = style2;
					k = m;
				}
			}

			if (k < i + j) {
				String string2 = this.plainText.substring(k, i + j);
				list.add(bl ? FormattedCharSequence.backward(string2, style, this.reverseCharModifier) : FormattedCharSequence.forward(string2, style));
			}

			return bl ? Lists.reverse(list) : list;
		}
	}

	public static SubStringSource create(FormattedText formattedText) {
		return create(formattedText, i -> i, string -> string);
	}

	public static SubStringSource create(FormattedText formattedText, Int2IntFunction int2IntFunction, UnaryOperator<String> unaryOperator) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Style> list = Lists.<Style>newArrayList();
		formattedText.visit((style, string) -> {
			StringDecomposer.iterateFormatted(string, style, (i, stylex, j) -> {
				stringBuilder.appendCodePoint(j);
				int k = Character.charCount(j);

				for (int l = 0; l < k; l++) {
					list.add(stylex);
				}

				return true;
			});
			return Optional.empty();
		}, Style.EMPTY);
		return new SubStringSource((String)unaryOperator.apply(stringBuilder.toString()), list, int2IntFunction);
	}
}
