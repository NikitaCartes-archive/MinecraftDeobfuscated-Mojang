package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Checkbox extends AbstractButton {
	private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_selected_highlighted");
	private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = new ResourceLocation("widget/checkbox_selected");
	private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_highlighted");
	private static final ResourceLocation CHECKBOX_SPRITE = new ResourceLocation("widget/checkbox");
	private static final int TEXT_COLOR = 14737632;
	private boolean selected;
	private final boolean showLabel;

	public Checkbox(int i, int j, int k, int l, Component component, boolean bl) {
		this(i, j, k, l, component, bl, true);
	}

	public Checkbox(int i, int j, int k, int l, Component component, boolean bl, boolean bl2) {
		super(i, j, k, l, component);
		this.selected = bl;
		this.showLabel = bl2;
	}

	@Override
	public void onPress() {
		this.selected = !this.selected;
	}

	public boolean selected() {
		return this.selected;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (this.active) {
			if (this.isFocused()) {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
			} else {
				narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
			}
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.enableDepthTest();
		Font font = minecraft.font;
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		ResourceLocation resourceLocation;
		if (this.selected) {
			resourceLocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
		} else {
			resourceLocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
		}

		guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), 20, this.height);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.showLabel) {
			guiGraphics.drawString(font, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}
}
