/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface PackSource {
    public static final UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
    public static final PackSource DEFAULT = PackSource.create(NO_DECORATION, true);
    public static final PackSource BUILT_IN = PackSource.create(PackSource.decorateWithSource("pack.source.builtin"), true);
    public static final PackSource FEATURE = PackSource.create(PackSource.decorateWithSource("pack.source.feature"), false);
    public static final PackSource WORLD = PackSource.create(PackSource.decorateWithSource("pack.source.world"), true);
    public static final PackSource SERVER = PackSource.create(PackSource.decorateWithSource("pack.source.server"), true);

    public Component decorate(Component var1);

    public boolean shouldAddAutomatically();

    public static PackSource create(final UnaryOperator<Component> unaryOperator, final boolean bl) {
        return new PackSource(){

            @Override
            public Component decorate(Component component) {
                return (Component)unaryOperator.apply(component);
            }

            @Override
            public boolean shouldAddAutomatically() {
                return bl;
            }
        };
    }

    private static UnaryOperator<Component> decorateWithSource(String string) {
        MutableComponent component = Component.translatable(string);
        return component2 -> Component.translatable("pack.nameAndSource", component2, component).withStyle(ChatFormatting.GRAY);
    }
}

