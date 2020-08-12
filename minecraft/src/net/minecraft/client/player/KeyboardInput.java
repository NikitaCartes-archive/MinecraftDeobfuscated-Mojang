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
		this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
		this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
		this.jumping = this.options.keyJump.isDown();
		this.shiftKeyDown = this.options.keyShift.isDown();
		if (bl2) {
			this.leftImpulse = (float)((double)this.leftImpulse * 0.2);
			this.forwardImpulse = (float)((double)this.forwardImpulse * 0.2);
		} else if (bl) {
			this.leftImpulse = (float)((double)this.leftImpulse * 0.3);
			this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3);
		}
	}
}
