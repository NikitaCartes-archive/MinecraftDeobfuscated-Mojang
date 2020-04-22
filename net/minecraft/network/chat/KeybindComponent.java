/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class KeybindComponent
extends BaseComponent {
    private static Function<String, Supplier<Component>> keyResolver = string -> () -> new TextComponent((String)string);
    private final String name;
    private Supplier<Component> nameResolver;

    public KeybindComponent(String string) {
        this.name = string;
    }

    @Environment(value=EnvType.CLIENT)
    public static void setKeyResolver(Function<String, Supplier<Component>> function) {
        keyResolver = function;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = keyResolver.apply(this.name);
        }
        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visitSelf(Component.ContentConsumer<T> contentConsumer) {
        return this.getNestedComponent().visit(contentConsumer);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public <T> Optional<T> visitSelf(Component.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return this.getNestedComponent().visit(styledContentConsumer, style);
    }

    @Override
    public KeybindComponent toMutable() {
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
    public /* synthetic */ BaseComponent toMutable() {
        return this.toMutable();
    }

    @Override
    public /* synthetic */ MutableComponent toMutable() {
        return this.toMutable();
    }
}

