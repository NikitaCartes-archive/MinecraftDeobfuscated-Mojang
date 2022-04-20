/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindResolver;
import org.jetbrains.annotations.Nullable;

public class KeybindContents
implements ComponentContents {
    private final String name;
    @Nullable
    private Supplier<Component> nameResolver;

    public KeybindContents(String string) {
        this.name = string;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
        }
        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return this.getNestedComponent().visit(contentConsumer);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return this.getNestedComponent().visit(styledContentConsumer, style);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof KeybindContents)) return false;
        KeybindContents keybindContents = (KeybindContents)object;
        if (!this.name.equals(keybindContents.name)) return false;
        return true;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return "keybind{" + this.name + "}";
    }

    public String getName() {
        return this.name;
    }
}

