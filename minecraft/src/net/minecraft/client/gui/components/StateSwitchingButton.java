package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
			RenderSystem.disableDepthTest();
			guiGraphics.blitSprite(this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
			RenderSystem.enableDepthTest();
		}
	}
}
