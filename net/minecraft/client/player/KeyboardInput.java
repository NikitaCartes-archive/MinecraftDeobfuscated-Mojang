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

    public KeyboardInput(Options options) {
        this.options = options;
    }

    @Override
    public void tick(boolean bl) {
        this.up = this.options.keyUp.isDown();
        this.down = this.options.keyDown.isDown();
        this.left = this.options.keyLeft.isDown();
        this.right = this.options.keyRight.isDown();
        float f = this.up == this.down ? 0.0f : (this.forwardImpulse = this.up ? 1.0f : -1.0f);
        this.leftImpulse = this.left == this.right ? 0.0f : (this.left ? 1.0f : -1.0f);
        this.jumping = this.options.keyJump.isDown();
        this.shiftKeyDown = this.options.keyShift.isDown();
        if (bl) {
            this.leftImpulse = (float)((double)this.leftImpulse * 0.3);
            this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3);
        }
    }
}

