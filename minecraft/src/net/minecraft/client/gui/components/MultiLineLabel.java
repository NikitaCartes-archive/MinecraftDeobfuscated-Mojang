package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public interface MultiLineLabel {
	MultiLineLabel EMPTY = new MultiLineLabel() {
		@Override
		public int renderCentered(GuiGraphics guiGraphics, int i, int j) {
			return j;
		}

		@Override
		public int renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public int renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public void renderBackgroundCentered(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
		}

		@Override
		public int getLineCount() {
			return 0;
		}

		@Override
		public int getWidth() {
			return 0;
		}
	};

	static MultiLineLabel create(Font font, FormattedText formattedText, int i) {
		return createFixed(
			font,
			(List<MultiLineLabel.TextWithWidth>)font.split(formattedText, i)
				.stream()
				.map(formattedCharSequence -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence)))
				.collect(ImmutableList.toImmutableList())
		);
	}

	static MultiLineLabel create(Font font, FormattedText formattedText, int i, int j) {
		return createFixed(
			font,
			(List<MultiLineLabel.TextWithWidth>)font.split(formattedText, i)
				.stream()
				.limit((long)j)
				.map(formattedCharSequence -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence)))
				.collect(ImmutableList.toImmutableList())
		);
	}

	static MultiLineLabel create(Font font, Component... components) {
		return createFixed(
			font,
			(List<MultiLineLabel.TextWithWidth>)Arrays.stream(components)
				.map(Component::getVisualOrderText)
				.map(formattedCharSequence -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence)))
				.collect(ImmutableList.toImmutableList())
		);
	}

	static MultiLineLabel create(Font font, List<Component> list) {
		return createFixed(
			font,
			(List<MultiLineLabel.TextWithWidth>)list.stream()
				.map(Component::getVisualOrderText)
				.map(formattedCharSequence -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence)))
				.collect(ImmutableList.toImmutableList())
		);
	}

	static MultiLineLabel createFixed(Font font, List<MultiLineLabel.TextWithWidth> list) {
		return list.isEmpty() ? EMPTY : new MultiLineLabel() {
			private final int width = list.stream().mapToInt(textWithWidth -> textWithWidth.width).max().orElse(0);

			@Override
			public int renderCentered(GuiGraphics guiGraphics, int i, int j) {
				return this.renderCentered(guiGraphics, i, j, 9, 16777215);
			}

			@Override
			public int renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					guiGraphics.drawString(font, textWithWidth.text, i - textWithWidth.width / 2, m, l);
					m += k;
				}

				return m;
			}

			@Override
			public int renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					guiGraphics.drawString(font, textWithWidth.text, i, m, l);
					m += k;
				}

				return m;
			}

			@Override
			public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					guiGraphics.drawString(font, textWithWidth.text, i, m, l, false);
					m += k;
				}

				return m;
			}

			@Override
			public void renderBackgroundCentered(GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
				int n = list.stream().mapToInt(textWithWidth -> textWithWidth.width).max().orElse(0);
				if (n > 0) {
					guiGraphics.fill(i - n / 2 - l, j - l, i + n / 2 + l, j + list.size() * k + l, m);
				}
			}

			@Override
			public int getLineCount() {
				return list.size();
			}

			@Override
			public int getWidth() {
				return this.width;
			}
		};
	}

	int renderCentered(GuiGraphics guiGraphics, int i, int j);

	int renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l);

	int renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l);

	int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l);

	void renderBackgroundCentered(GuiGraphics guiGraphics, int i, int j, int k, int l, int m);

	int getLineCount();

	int getWidth();

	@Environment(EnvType.CLIENT)
	public static class TextWithWidth {
		final FormattedCharSequence text;
		final int width;

		TextWithWidth(FormattedCharSequence formattedCharSequence, int i) {
			this.text = formattedCharSequence;
			this.width = i;
		}
	}
}
