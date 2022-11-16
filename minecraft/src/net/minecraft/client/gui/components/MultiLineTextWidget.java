package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class MultiLineTextWidget extends AbstractWidget {
	private final MultiLineLabel multiLineLabel;
	private final int lineHeight;
	private final boolean centered;

	private MultiLineTextWidget(MultiLineLabel multiLineLabel, Font font, Component component, boolean bl) {
		super(0, 0, multiLineLabel.getWidth(), multiLineLabel.getLineCount() * 9, component);
		this.multiLineLabel = multiLineLabel;
		this.lineHeight = 9;
		this.centered = bl;
		this.active = false;
	}

	public static MultiLineTextWidget createCentered(int i, Font font, Component component) {
		MultiLineLabel multiLineLabel = MultiLineLabel.create(font, component, i);
		return new MultiLineTextWidget(multiLineLabel, font, component, true);
	}

	public static MultiLineTextWidget create(int i, Font font, Component component) {
		MultiLineLabel multiLineLabel = MultiLineLabel.create(font, component, i);
		return new MultiLineTextWidget(multiLineLabel, font, component, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		if (this.centered) {
			this.multiLineLabel.renderCentered(poseStack, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, 16777215);
		} else {
			this.multiLineLabel.renderLeftAligned(poseStack, this.getX(), this.getY(), this.lineHeight, 16777215);
		}
	}
}
