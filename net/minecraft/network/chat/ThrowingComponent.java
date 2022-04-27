/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.Component;

public class ThrowingComponent
extends Exception {
    private final Component component;

    public ThrowingComponent(Component component) {
        super(component.getString());
        this.component = component;
    }

    public ThrowingComponent(Component component, Throwable throwable) {
        super(component.getString(), throwable);
        this.component = component;
    }

    public Component getComponent() {
        return this.component;
    }
}

