package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class LockIconButton extends Button {
	private boolean locked;

	public LockIconButton(int i, int j, Button.OnPress onPress) {
		super(i, j, 20, 20, new TranslatableComponent("narrator.button.difficulty_lock"), onPress);
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return CommonComponents.joinForNarration(
			super.createNarrationMessage(),
			this.isLocked()
				? new TranslatableComponent("narrator.button.difficulty_lock.locked")
				: new TranslatableComponent("narrator.button.difficulty_lock.unlocked")
		);
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean bl) {
		this.locked = bl;
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Button.WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		LockIconButton.Icon icon;
		if (!this.active) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
		} else if (this.isHovered()) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
		} else {
			icon = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
		}

		this.blit(poseStack, this.x, this.y, icon.getX(), icon.getY(), this.width, this.height);
	}

	@Environment(EnvType.CLIENT)
	static enum Icon {
		LOCKED(0, 146),
		LOCKED_HOVER(0, 166),
		LOCKED_DISABLED(0, 186),
		UNLOCKED(20, 146),
		UNLOCKED_HOVER(20, 166),
		UNLOCKED_DISABLED(20, 186);

		private final int x;
		private final int y;

		private Icon(int j, int k) {
			this.x = j;
			this.y = k;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
}
