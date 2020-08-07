package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class Button extends AbstractButton {
	public static final Button.OnTooltip NO_TOOLTIP = (button, poseStack, i, j) -> {
	};
	protected final Button.OnPress onPress;
	protected final Button.OnTooltip onTooltip;

	public Button(int i, int j, int k, int l, Component component, Button.OnPress onPress) {
		this(i, j, k, l, component, onPress, NO_TOOLTIP);
	}

	public Button(int i, int j, int k, int l, Component component, Button.OnPress onPress, Button.OnTooltip onTooltip) {
		super(i, j, k, l, component);
		this.onPress = onPress;
		this.onTooltip = onTooltip;
	}

	@Override
	public void onPress() {
		this.onPress.onPress(this);
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		super.renderButton(poseStack, i, j, f);
		if (this.isHovered()) {
			this.renderToolTip(poseStack, i, j);
		}
	}

	@Override
	public void renderToolTip(PoseStack poseStack, int i, int j) {
		this.onTooltip.onTooltip(this, poseStack, i, j);
	}

	@Environment(EnvType.CLIENT)
	public interface OnPress {
		void onPress(Button button);
	}

	@Environment(EnvType.CLIENT)
	public interface OnTooltip {
		void onTooltip(Button button, PoseStack poseStack, int i, int j);
	}
}
