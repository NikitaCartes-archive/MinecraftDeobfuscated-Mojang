package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public interface MultiLineLabel {
	MultiLineLabel EMPTY = new MultiLineLabel() {
		@Override
		public int renderCentered(PoseStack poseStack, int i, int j) {
			return j;
		}

		@Override
		public int renderCentered(PoseStack poseStack, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l) {
			return j;
		}

		@Override
		public int getLineCount() {
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

	static MultiLineLabel createFixed(Font font, List<MultiLineLabel.TextWithWidth> list) {
		return list.isEmpty() ? EMPTY : new MultiLineLabel() {
			@Override
			public int renderCentered(PoseStack poseStack, int i, int j) {
				return this.renderCentered(poseStack, i, j, 9, 16777215);
			}

			@Override
			public int renderCentered(PoseStack poseStack, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					font.drawShadow(poseStack, textWithWidth.text, (float)(i - textWithWidth.width / 2), (float)m, l);
					m += k;
				}

				return m;
			}

			@Override
			public int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					font.drawShadow(poseStack, textWithWidth.text, (float)i, (float)m, l);
					m += k;
				}

				return m;
			}

			@Override
			public int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l) {
				int m = j;

				for (MultiLineLabel.TextWithWidth textWithWidth : list) {
					font.draw(poseStack, textWithWidth.text, (float)i, (float)m, l);
					m += k;
				}

				return m;
			}

			@Override
			public int getLineCount() {
				return list.size();
			}
		};
	}

	int renderCentered(PoseStack poseStack, int i, int j);

	int renderCentered(PoseStack poseStack, int i, int j, int k, int l);

	int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l);

	int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l);

	int getLineCount();

	@Environment(EnvType.CLIENT)
	public static class TextWithWidth {
		private final FormattedCharSequence text;
		private final int width;

		private TextWithWidth(FormattedCharSequence formattedCharSequence, int i) {
			this.text = formattedCharSequence;
			this.width = i;
		}
	}
}
