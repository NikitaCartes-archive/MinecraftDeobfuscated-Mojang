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

	private static float calculateImpulse(boolean bl, boolean bl2) {
		if (bl == bl2) {
			return 0.0F;
		} else {
			return bl ? 1.0F : -1.0F;
		}
	}

	@Override
	public void tick(boolean bl, float f) {
		this.up = this.options.keyUp.isDown();
		this.down = this.options.keyDown.isDown();
		this.left = this.options.keyLeft.isDown();
		this.right = this.options.keyRight.isDown();
		this.forwardImpulse = calculateImpulse(this.up, this.down);
		this.leftImpulse = calculateImpulse(this.left, this.right);
		this.jumping = this.options.keyJump.isDown();
		this.shiftKeyDown = this.options.keyShift.isDown();
		if (bl) {
			this.leftImpulse *= f;
			this.forwardImpulse *= f;
		}
	}
}
