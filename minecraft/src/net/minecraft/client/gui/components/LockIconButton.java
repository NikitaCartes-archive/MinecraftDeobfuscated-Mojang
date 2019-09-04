package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class LockIconButton extends Button {
	private boolean locked;

	public LockIconButton(int i, int j, Button.OnPress onPress) {
		super(i, j, 20, 20, I18n.get("narrator.button.difficulty_lock"), onPress);
	}

	@Override
	protected String getNarrationMessage() {
		return super.getNarrationMessage()
			+ ". "
			+ (this.isLocked() ? I18n.get("narrator.button.difficulty_lock.locked") : I18n.get("narrator.button.difficulty_lock.unlocked"));
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean bl) {
		this.locked = bl;
	}

	@Override
	public void renderButton(int i, int j, float f) {
		Minecraft.getInstance().getTextureManager().bind(Button.WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		LockIconButton.Icon icon;
		if (!this.active) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
		} else if (this.isHovered()) {
			icon = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
		} else {
			icon = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
		}

		this.blit(this.x, this.y, icon.getX(), icon.getY(), this.width, this.height);
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
