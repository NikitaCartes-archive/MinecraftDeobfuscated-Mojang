/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;

public interface AdvancementSubProvider {
    public void generate(Consumer<Advancement> var1);
}

