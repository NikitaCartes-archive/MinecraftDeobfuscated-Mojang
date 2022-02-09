/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;

@Environment(value=EnvType.CLIENT)
public class KeyboardInput
extends Input {
    private final Options options;
    private static final float MOVING_SLOW_FACTOR = 0.3f;

    public KeyboardInput(Options options) {
        this.options = options;
    }

    private static float calculateImpulse(boolean bl, boolean bl2) {
        if (bl == bl2) {
            return 0.0f;
        }
        return bl ? 1.0f : -1.0f;
    }

    @Override
    public void tick(boolean bl) {
        this.up = this.options.keyUp.isDown();
        this.down = this.options.keyDown.isDown();
        this.left = this.options.keyLeft.isDown();
        this.right = this.options.keyRight.isDown();
        this.forwardImpulse = KeyboardInput.calculateImpulse(this.up, this.down);
        this.leftImpulse = KeyboardInput.calculateImpulse(this.left, this.right);
        this.jumping = this.options.keyJump.isDown();
        this.shiftKeyDown = this.options.keyShift.isDown();
        if (bl) {
            this.leftImpulse *= 0.3f;
            this.forwardImpulse *= 0.3f;
        }
    }
}

