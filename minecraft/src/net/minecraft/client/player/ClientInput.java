package net.minecraft.client.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public class ClientInput {
	public Input keyPresses = Input.EMPTY;
	public float leftImpulse;
	public float forwardImpulse;

	public void tick(boolean bl, float f) {
	}

	public Vec2 getMoveVector() {
		return new Vec2(this.leftImpulse, this.forwardImpulse);
	}

	public boolean hasForwardImpulse() {
		return this.forwardImpulse > 1.0E-5F;
	}

	public void makeJump() {
		this.keyPresses = new Input(
			this.keyPresses.forward(),
			this.keyPresses.backward(),
			this.keyPresses.left(),
			this.keyPresses.right(),
			true,
			this.keyPresses.shift(),
			this.keyPresses.sprint()
		);
	}
}
