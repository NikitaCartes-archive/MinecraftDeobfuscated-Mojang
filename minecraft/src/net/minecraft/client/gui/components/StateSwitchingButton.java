package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;

@Environment(EnvType.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
	@Nullable
	protected WidgetSprites sprites;
	protected boolean isStateTriggered;

	public StateSwitchingButton(int i, int j, int k, int l, boolean bl) {
		super(i, j, k, l, CommonComponents.EMPTY);
		this.isStateTriggered = bl;
	}

	public void initTextureValues(WidgetSprites widgetSprites) {
		this.sprites = widgetSprites;
	}

	public void setStateTriggered(boolean bl) {
		this.isStateTriggered = bl;
	}

	public boolean isStateTriggered() {
		return this.isStateTriggered;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		this.defaultButtonNarrationText(narrationElementOutput);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.sprites != null) {
			guiGraphics.blitSprite(
				RenderType::guiTextured, this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height
			);
		}
	}
}
