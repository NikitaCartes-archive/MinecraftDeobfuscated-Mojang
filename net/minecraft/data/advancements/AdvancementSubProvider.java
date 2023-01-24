/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public interface AdvancementSubProvider {
    public static Advancement createPlaceholder(String string) {
        return Advancement.Builder.advancement().build(new ResourceLocation(string));
    }

    public void generate(HolderLookup.Provider var1, Consumer<Advancement> var2);
}

