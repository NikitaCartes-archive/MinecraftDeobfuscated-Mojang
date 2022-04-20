/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface PackSource {
    public static final PackSource DEFAULT = PackSource.passThrough();
    public static final PackSource BUILT_IN = PackSource.decorating("pack.source.builtin");
    public static final PackSource WORLD = PackSource.decorating("pack.source.world");
    public static final PackSource SERVER = PackSource.decorating("pack.source.server");

    public Component decorate(Component var1);

    public static PackSource passThrough() {
        return component -> component;
    }

    public static PackSource decorating(String string) {
        MutableComponent component = Component.translatable(string);
        return component2 -> Component.translatable("pack.nameAndSource", component2, component).withStyle(ChatFormatting.GRAY);
    }
}

