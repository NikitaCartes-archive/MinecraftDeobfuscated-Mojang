/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;

public interface AdvancementSubProvider {
    public void generate(HolderLookup.Provider var1, Consumer<Advancement> var2);
}

