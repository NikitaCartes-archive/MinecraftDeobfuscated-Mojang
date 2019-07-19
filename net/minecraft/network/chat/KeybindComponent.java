/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;

public class KeybindComponent
extends BaseComponent {
    public static Function<String, Supplier<String>> keyResolver = string -> () -> string;
    private final String name;
    private Supplier<String> nameResolver;

    public KeybindComponent(String string) {
        this.name = string;
    }

    @Override
    public String getContents() {
        if (this.nameResolver == null) {
            this.nameResolver = keyResolver.apply(this.name);
        }
        return this.nameResolver.get();
    }

    @Override
    public KeybindComponent copy() {
        return new KeybindComponent(this.name);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof KeybindComponent) {
            KeybindComponent keybindComponent = (KeybindComponent)object;
            return this.name.equals(keybindComponent.name) && super.equals(object);
        }
        return false;
    }

    @Override
    public String toString() {
        return "KeybindComponent{keybind='" + this.name + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    public String getName() {
        return this.name;
    }

    @Override
    public /* synthetic */ Component copy() {
        return this.copy();
    }
}

