package net.minecraft.client.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;

@Environment(EnvType.CLIENT)
public class KeyboardInput extends Input {
	private final Options options;

	public KeyboardInput(Options options) {
		this.options = options;
	}

	@Override
	public void tick(boolean bl, boolean bl2) {
		this.up = this.options.keyUp.isDown();
		this.down = this.options.keyDown.isDown();
		this.left = this.options.keyLeft.isDown();
		this.right = this.options.keyRight.isDown();
		this.forwardImpulse = this.up == this.down ? 0.0F : (float)(this.up ? 1 : -1);
		this.leftImpulse = this.left == this.right ? 0.0F : (float)(this.left ? 1 : -1);
		this.jumping = this.options.keyJump.isDown();
		this.sneakKeyDown = this.options.keySneak.isDown();
		if (!bl2 && (this.sneakKeyDown || bl)) {
			this.leftImpulse = (float)((double)this.leftImpulse * 0.3);
			this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3);
		}
	}
}
