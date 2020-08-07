package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSequence {
	FormattedCharSequence EMPTY = formattedCharSink -> true;

	@Environment(EnvType.CLIENT)
	boolean accept(FormattedCharSink formattedCharSink);

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence codepoint(int i, Style style) {
		return formattedCharSink -> formattedCharSink.accept(0, style, i);
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence forward(String string, Style style) {
		return string.isEmpty() ? EMPTY : formattedCharSink -> StringDecomposer.iterate(string, style, formattedCharSink);
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence backward(String string, Style style, Int2IntFunction int2IntFunction) {
		return string.isEmpty() ? EMPTY : formattedCharSink -> StringDecomposer.iterateBackwards(string, style, decorateOutput(formattedCharSink, int2IntFunction));
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSink decorateOutput(FormattedCharSink formattedCharSink, Int2IntFunction int2IntFunction) {
		return (i, style, j) -> formattedCharSink.accept(i, style, int2IntFunction.apply(Integer.valueOf(j)));
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence composite(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
		return fromPair(formattedCharSequence, formattedCharSequence2);
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence composite(List<FormattedCharSequence> list) {
		int i = list.size();
		switch (i) {
			case 0:
				return EMPTY;
			case 1:
				return (FormattedCharSequence)list.get(0);
			case 2:
				return fromPair((FormattedCharSequence)list.get(0), (FormattedCharSequence)list.get(1));
			default:
				return fromList(ImmutableList.copyOf(list));
		}
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence fromPair(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
		return formattedCharSink -> formattedCharSequence.accept(formattedCharSink) && formattedCharSequence2.accept(formattedCharSink);
	}

	@Environment(EnvType.CLIENT)
	static FormattedCharSequence fromList(List<FormattedCharSequence> list) {
		return formattedCharSink -> {
			for (FormattedCharSequence formattedCharSequence : list) {
				if (!formattedCharSequence.accept(formattedCharSink)) {
					return false;
				}
			}

			return true;
		};
	}
}
