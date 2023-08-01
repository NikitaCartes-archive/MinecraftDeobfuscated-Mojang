package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

@Environment(EnvType.CLIENT)
public class PageButton extends Button {
	private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/page_forward_highlighted");
	private static final ResourceLocation PAGE_FORWARD_SPRITE = new ResourceLocation("widget/page_forward");
	private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/page_backward_highlighted");
	private static final ResourceLocation PAGE_BACKWARD_SPRITE = new ResourceLocation("widget/page_backward");
	private final boolean isForward;
	private final boolean playTurnSound;

	public PageButton(int i, int j, boolean bl, Button.OnPress onPress, boolean bl2) {
		super(i, j, 23, 13, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.isForward = bl;
		this.playTurnSound = bl2;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		ResourceLocation resourceLocation;
		if (this.isForward) {
			resourceLocation = this.isHoveredOrFocused() ? PAGE_FORWARD_HIGHLIGHTED_SPRITE : PAGE_FORWARD_SPRITE;
		} else {
			resourceLocation = this.isHoveredOrFocused() ? PAGE_BACKWARD_HIGHLIGHTED_SPRITE : PAGE_BACKWARD_SPRITE;
		}

		guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), 23, 13);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		if (this.playTurnSound) {
			soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
		}
	}
}
