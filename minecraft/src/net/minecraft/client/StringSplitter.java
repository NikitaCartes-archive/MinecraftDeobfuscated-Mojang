package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

@Environment(EnvType.CLIENT)
public class StringSplitter {
	private final StringSplitter.WidthProvider widthProvider;

	public StringSplitter(StringSplitter.WidthProvider widthProvider) {
		this.widthProvider = widthProvider;
	}

	public float stringWidth(@Nullable String string) {
		if (string == null) {
			return 0.0F;
		} else {
			MutableFloat mutableFloat = new MutableFloat();
			StringDecomposer.iterateFormatted(string, Style.EMPTY, (i, style, j) -> {
				mutableFloat.add(this.widthProvider.getWidth(j, style));
				return true;
			});
			return mutableFloat.floatValue();
		}
	}

	public float stringWidth(FormattedText formattedText) {
		MutableFloat mutableFloat = new MutableFloat();
		StringDecomposer.iterateFormatted(formattedText, Style.EMPTY, (i, style, j) -> {
			mutableFloat.add(this.widthProvider.getWidth(j, style));
			return true;
		});
		return mutableFloat.floatValue();
	}

	public float stringWidth(FormattedCharSequence formattedCharSequence) {
		MutableFloat mutableFloat = new MutableFloat();
		formattedCharSequence.accept((i, style, j) -> {
			mutableFloat.add(this.widthProvider.getWidth(j, style));
			return true;
		});
		return mutableFloat.floatValue();
	}

	public int plainIndexAtWidth(String string, int i, Style style) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink((float)i);
		StringDecomposer.iterate(string, style, widthLimitedCharSink);
		return widthLimitedCharSink.getPosition();
	}

	public String plainHeadByWidth(String string, int i, Style style) {
		return string.substring(0, this.plainIndexAtWidth(string, i, style));
	}

	public String plainTailByWidth(String string, int i, Style style) {
		MutableFloat mutableFloat = new MutableFloat();
		MutableInt mutableInt = new MutableInt(string.length());
		StringDecomposer.iterateBackwards(string, style, (j, stylex, k) -> {
			float f = mutableFloat.addAndGet(this.widthProvider.getWidth(k, stylex));
			if (f > (float)i) {
				return false;
			} else {
				mutableInt.setValue(j);
				return true;
			}
		});
		return string.substring(mutableInt.intValue());
	}

	@Nullable
	public Style componentStyleAtWidth(FormattedText formattedText, int i) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink((float)i);
		return (Style)formattedText.visit(
				(style, string) -> StringDecomposer.iterateFormatted(string, style, widthLimitedCharSink) ? Optional.empty() : Optional.of(style), Style.EMPTY
			)
			.orElse(null);
	}

	@Nullable
	public Style componentStyleAtWidth(FormattedCharSequence formattedCharSequence, int i) {
		StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink((float)i);
		MutableObject<Style> mutableObject = new MutableObject<>();
		formattedCharSequence.accept((ix, style, j) -> {
			if (!widthLimitedCharSink.accept(ix, style, j)) {
				mutableObject.setValue(style);
				return false;
			} else {
				return true;
			}
		});
		return mutableObject.getValue();
	}

	public FormattedText headByWidth(FormattedText formattedText, int i, Style style) {
		final StringSplitter.WidthLimitedCharSink widthLimitedCharSink = new StringSplitter.WidthLimitedCharSink((float)i);
		return (FormattedText)formattedText.visit(new FormattedText.StyledContentConsumer<FormattedText>() {
			private final ComponentCollector collector = new ComponentCollector();

			@Override
			public Optional<FormattedText> accept(Style style, String string) {
				widthLimitedCharSink.resetPosition();
				if (!StringDecomposer.iterateFormatted(string, style, widthLimitedCharSink)) {
					String string2 = string.substring(0, widthLimitedCharSink.getPosition());
					if (!string2.isEmpty()) {
						this.collector.append(FormattedText.of(string2, style));
					}

					return Optional.of(this.collector.getResultOrEmpty());
				} else {
					if (!string.isEmpty()) {
						this.collector.append(FormattedText.of(string, style));
					}

					return Optional.empty();
				}
			}
		}, style).orElse(formattedText);
	}

	public static int getWordPosition(String string, int i, int j, boolean bl) {
		int k = j;
		boolean bl2 = i < 0;
		int l = Math.abs(i);

		for (int m = 0; m < l; m++) {
			if (bl2) {
				while (bl && k > 0 && (string.charAt(k - 1) == ' ' || string.charAt(k - 1) == '\n')) {
					k--;
				}

				while (k > 0 && string.charAt(k - 1) != ' ' && string.charAt(k - 1) != '\n') {
					k--;
				}
			} else {
				int n = string.length();
				int o = string.indexOf(32, k);
				int p = string.indexOf(10, k);
				if (o == -1 && p == -1) {
					k = -1;
				} else if (o != -1 && p != -1) {
					k = Math.min(o, p);
				} else if (o != -1) {
					k = o;
				} else {
					k = p;
				}

				if (k == -1) {
					k = n;
				} else {
					while (bl && k < n && (string.charAt(k) == ' ' || string.charAt(k) == '\n')) {
						k++;
					}
				}
			}
		}

		return k;
	}

	public void splitLines(String string, int i, Style style, boolean bl, StringSplitter.LinePosConsumer linePosConsumer) {
		int j = 0;
		int k = string.length();
		Style style2 = style;

		while (j < k) {
			StringSplitter.LineBreakFinder lineBreakFinder = new StringSplitter.LineBreakFinder((float)i);
			boolean bl2 = StringDecomposer.iterateFormatted(string, j, style2, style, lineBreakFinder);
			if (bl2) {
				linePosConsumer.accept(style2, j, k);
				break;
			}

			int l = lineBreakFinder.getSplitPosition();
			char c = string.charAt(l);
			int m = c != '\n' && c != ' ' ? l : l + 1;
			linePosConsumer.accept(style2, j, bl ? m : l);
			j = m;
			style2 = lineBreakFinder.getSplitStyle();
		}
	}

	public List<FormattedText> splitLines(String string, int i, Style style) {
		List<FormattedText> list = Lists.<FormattedText>newArrayList();
		this.splitLines(string, i, style, false, (stylex, ix, j) -> list.add(FormattedText.of(string.substring(ix, j), stylex)));
		return list;
	}

	public List<FormattedText> splitLines(FormattedText formattedText, int i, Style style) {
		List<FormattedText> list = Lists.<FormattedText>newArrayList();
		this.splitLines(formattedText, i, style, (formattedTextx, boolean_) -> list.add(formattedTextx));
		return list;
	}

	public void splitLines(FormattedText formattedText, int i, Style style, BiConsumer<FormattedText, Boolean> biConsumer) {
		List<StringSplitter.LineComponent> list = Lists.<StringSplitter.LineComponent>newArrayList();
		formattedText.visit((stylex, string) -> {
			if (!string.isEmpty()) {
				list.add(new StringSplitter.LineComponent(string, stylex));
			}

			return Optional.empty();
		}, style);
		StringSplitter.FlatComponents flatComponents = new StringSplitter.FlatComponents(list);
		boolean bl = true;
		boolean bl2 = false;
		boolean bl3 = false;

		while (bl) {
			bl = false;
			StringSplitter.LineBreakFinder lineBreakFinder = new StringSplitter.LineBreakFinder((float)i);

			for (StringSplitter.LineComponent lineComponent : flatComponents.parts) {
				boolean bl4 = StringDecomposer.iterateFormatted(lineComponent.contents, 0, lineComponent.style, style, lineBreakFinder);
				if (!bl4) {
					int j = lineBreakFinder.getSplitPosition();
					Style style2 = lineBreakFinder.getSplitStyle();
					char c = flatComponents.charAt(j);
					boolean bl5 = c == '\n';
					boolean bl6 = bl5 || c == ' ';
					bl2 = bl5;
					FormattedText formattedText2 = flatComponents.splitAt(j, bl6 ? 1 : 0, style2);
					biConsumer.accept(formattedText2, bl3);
					bl3 = !bl5;
					bl = true;
					break;
				}

				lineBreakFinder.addToOffset(lineComponent.contents.length());
			}
		}

		FormattedText formattedText3 = flatComponents.getRemainder();
		if (formattedText3 != null) {
			biConsumer.accept(formattedText3, bl3);
		} else if (bl2) {
			biConsumer.accept(FormattedText.EMPTY, false);
		}
	}

	@Environment(EnvType.CLIENT)
	static class FlatComponents {
		private final List<StringSplitter.LineComponent> parts;
		private String flatParts;

		public FlatComponents(List<StringSplitter.LineComponent> list) {
			this.parts = list;
			this.flatParts = (String)list.stream().map(lineComponent -> lineComponent.contents).collect(Collectors.joining());
		}

		public char charAt(int i) {
			return this.flatParts.charAt(i);
		}

		public FormattedText splitAt(int i, int j, Style style) {
			ComponentCollector componentCollector = new ComponentCollector();
			ListIterator<StringSplitter.LineComponent> listIterator = this.parts.listIterator();
			int k = i;
			boolean bl = false;

			while (listIterator.hasNext()) {
				StringSplitter.LineComponent lineComponent = (StringSplitter.LineComponent)listIterator.next();
				String string = lineComponent.contents;
				int l = string.length();
				if (!bl) {
					if (k > l) {
						componentCollector.append(lineComponent);
						listIterator.remove();
						k -= l;
					} else {
						String string2 = string.substring(0, k);
						if (!string2.isEmpty()) {
							componentCollector.append(FormattedText.of(string2, lineComponent.style));
						}

						k += j;
						bl = true;
					}
				}

				if (bl) {
					if (k <= l) {
						String string2 = string.substring(k);
						if (string2.isEmpty()) {
							listIterator.remove();
						} else {
							listIterator.set(new StringSplitter.LineComponent(string2, style));
						}
						break;
					}

					listIterator.remove();
					k -= l;
				}
			}

			this.flatParts = this.flatParts.substring(i + j);
			return componentCollector.getResultOrEmpty();
		}

		@Nullable
		public FormattedText getRemainder() {
			ComponentCollector componentCollector = new ComponentCollector();
			this.parts.forEach(componentCollector::append);
			this.parts.clear();
			return componentCollector.getResult();
		}
	}

	@Environment(EnvType.CLIENT)
	class LineBreakFinder implements FormattedCharSink {
		private final float maxWidth;
		private int lineBreak = -1;
		private Style lineBreakStyle = Style.EMPTY;
		private boolean hadNonZeroWidthChar;
		private float width;
		private int lastSpace = -1;
		private Style lastSpaceStyle = Style.EMPTY;
		private int nextChar;
		private int offset;

		public LineBreakFinder(float f) {
			this.maxWidth = Math.max(f, 1.0F);
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			int k = i + this.offset;
			switch (j) {
				case 10:
					return this.finishIteration(k, style);
				case 32:
					this.lastSpace = k;
					this.lastSpaceStyle = style;
				default:
					float f = StringSplitter.this.widthProvider.getWidth(j, style);
					this.width += f;
					if (!this.hadNonZeroWidthChar || !(this.width > this.maxWidth)) {
						this.hadNonZeroWidthChar |= f != 0.0F;
						this.nextChar = k + Character.charCount(j);
						return true;
					} else {
						return this.lastSpace != -1 ? this.finishIteration(this.lastSpace, this.lastSpaceStyle) : this.finishIteration(k, style);
					}
			}
		}

		private boolean finishIteration(int i, Style style) {
			this.lineBreak = i;
			this.lineBreakStyle = style;
			return false;
		}

		private boolean lineBreakFound() {
			return this.lineBreak != -1;
		}

		public int getSplitPosition() {
			return this.lineBreakFound() ? this.lineBreak : this.nextChar;
		}

		public Style getSplitStyle() {
			return this.lineBreakStyle;
		}

		public void addToOffset(int i) {
			this.offset += i;
		}
	}

	@Environment(EnvType.CLIENT)
	static class LineComponent implements FormattedText {
		private final String contents;
		private final Style style;

		public LineComponent(String string, Style style) {
			this.contents = string;
			this.style = style;
		}

		@Override
		public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
			return contentConsumer.accept(this.contents);
		}

		@Override
		public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
			return styledContentConsumer.accept(this.style.applyTo(style), this.contents);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface LinePosConsumer {
		void accept(Style style, int i, int j);
	}

	@Environment(EnvType.CLIENT)
	class WidthLimitedCharSink implements FormattedCharSink {
		private float maxWidth;
		private int position;

		public WidthLimitedCharSink(float f) {
			this.maxWidth = f;
		}

		@Override
		public boolean accept(int i, Style style, int j) {
			this.maxWidth = this.maxWidth - StringSplitter.this.widthProvider.getWidth(j, style);
			if (this.maxWidth >= 0.0F) {
				this.position = i + Character.charCount(j);
				return true;
			} else {
				return false;
			}
		}

		public int getPosition() {
			return this.position;
		}

		public void resetPosition() {
			this.position = 0;
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface WidthProvider {
		float getWidth(int i, Style style);
	}
}
