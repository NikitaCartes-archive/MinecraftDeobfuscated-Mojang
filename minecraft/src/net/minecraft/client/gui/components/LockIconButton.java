package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class LockIconButton extends Button {
	private boolean locked;

	public LockIconButton(int i, int j, Button.OnPress onPress) {
		super(i, j, 20, 20, Component.translatable("narrator.button.difficulty_lock"), onPress, DEFAULT_NARRATION);
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return CommonComponents.joinForNarration(
			super.createNarrationMessage(),
			this.isLocked() ? Component.translatable("narrator.button.difficulty_lock.locked") : Component.translatable("narrator.button.difficulty_lock.unlocked")
		);
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean bl) {
		this.locked = bl;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		LockIconButton.Icon icon;
		if (!this.active) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
		} else if (this.isHoveredOrFocused()) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
		} else {
			icon = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
		}

		guiGraphics.blitSprite(RenderType::guiTextured, icon.sprite, this.getX(), this.getY(), this.width, this.height);
	}

	@Environment(EnvType.CLIENT)
	static enum Icon {
		LOCKED(ResourceLocation.withDefaultNamespace("widget/locked_button")),
		LOCKED_HOVER(ResourceLocation.withDefaultNamespace("widget/locked_button_highlighted")),
		LOCKED_DISABLED(ResourceLocation.withDefaultNamespace("widget/locked_button_disabled")),
		UNLOCKED(ResourceLocation.withDefaultNamespace("widget/unlocked_button")),
		UNLOCKED_HOVER(ResourceLocation.withDefaultNamespace("widget/unlocked_button_highlighted")),
		UNLOCKED_DISABLED(ResourceLocation.withDefaultNamespace("widget/unlocked_button_disabled"));

		final ResourceLocation sprite;

		private Icon(final ResourceLocation resourceLocation) {
			this.sprite = resourceLocation;
		}
	}
}
