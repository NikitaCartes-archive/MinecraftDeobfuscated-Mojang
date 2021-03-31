/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum HumanoidArm {
    LEFT(new TranslatableComponent("options.mainHand.left")),
    RIGHT(new TranslatableComponent("options.mainHand.right"));

    private final Component name;

    private HumanoidArm(Component component) {
        this.name = component;
    }

    public HumanoidArm getOpposite() {
        if (this == LEFT) {
            return RIGHT;
        }
        return LEFT;
    }

    public String toString() {
        return this.name.getString();
    }

    public Component getName() {
        return this.name;
    }
}

