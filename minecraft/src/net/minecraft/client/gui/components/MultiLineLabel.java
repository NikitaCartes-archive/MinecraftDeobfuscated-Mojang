package net.minecraft.client.gui.components;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public interface MultiLineLabel {
	MultiLineLabel EMPTY = new MultiLineLabel() {
		@Override
		public void renderCentered(GuiGraphics guiGraphics, int i, int j) {
		}

		@Override
		public void renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		}

		@Override
		public void renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		}

		@Override
		public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l) {
			return j;
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

	static MultiLineLabel create(Font font, Component... components) {
		return create(font, Integer.MAX_VALUE, Integer.MAX_VALUE, components);
	}

	static MultiLineLabel create(Font font, int i, Component... components) {
		return create(font, i, Integer.MAX_VALUE, components);
	}

	static MultiLineLabel create(Font font, Component component, int i) {
		return create(font, i, Integer.MAX_VALUE, component);
	}

	static MultiLineLabel create(Font font, int i, int j, Component... components) {
		return components.length == 0 ? EMPTY : new MultiLineLabel() {
			@Nullable
			private List<MultiLineLabel.TextAndWidth> cachedTextAndWidth;
			@Nullable
			private Language splitWithLanguage;

			@Override
			public void renderCentered(GuiGraphics guiGraphics, int i, int j) {
				this.renderCentered(guiGraphics, i, j, 9, -1);
			}

			@Override
			public void renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextAndWidth textAndWidth : this.getSplitMessage()) {
					guiGraphics.drawCenteredString(font, textAndWidth.text, i, m, l);
					m += k;
				}
			}

			@Override
			public void renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextAndWidth textAndWidth : this.getSplitMessage()) {
					guiGraphics.drawString(font, textAndWidth.text, i, m, l);
					m += k;
				}
			}

			@Override
			public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextAndWidth textAndWidth : this.getSplitMessage()) {
					guiGraphics.drawString(font, textAndWidth.text, i, m, l, false);
					m += k;
				}

				return m;
			}

			private List<MultiLineLabel.TextAndWidth> getSplitMessage() {
				Language language = Language.getInstance();
				if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
					return this.cachedTextAndWidth;
				} else {
					this.splitWithLanguage = language;
					List<FormattedCharSequence> list = new ArrayList();

					for (Component component : components) {
						list.addAll(font.split(component, i));
					}

					this.cachedTextAndWidth = new ArrayList();

					for (FormattedCharSequence formattedCharSequence : list.subList(0, Math.min(list.size(), j))) {
						this.cachedTextAndWidth.add(new MultiLineLabel.TextAndWidth(formattedCharSequence, font.width(formattedCharSequence)));
					}

					return this.cachedTextAndWidth;
				}
			}

			@Override
			public int getLineCount() {
				return this.getSplitMessage().size();
			}

			@Override
			public int getWidth() {
				return Math.min(i, this.getSplitMessage().stream().mapToInt(MultiLineLabel.TextAndWidth::width).max().orElse(0));
			}
		};
	}

	void renderCentered(GuiGraphics guiGraphics, int i, int j);

	void renderCentered(GuiGraphics guiGraphics, int i, int j, int k, int l);

	void renderLeftAligned(GuiGraphics guiGraphics, int i, int j, int k, int l);

	int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int i, int j, int k, int l);

	int getLineCount();

	int getWidth();

	@Environment(EnvType.CLIENT)
	public static record TextAndWidth(FormattedCharSequence text, int width) {
	}
}
