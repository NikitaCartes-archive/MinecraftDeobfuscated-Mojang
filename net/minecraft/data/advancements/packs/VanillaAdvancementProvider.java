/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements.packs;

import java.util.List;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.packs.VanillaAdventureAdvancements;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
import net.minecraft.data.advancements.packs.VanillaNetherAdvancements;
import net.minecraft.data.advancements.packs.VanillaStoryAdvancements;
import net.minecraft.data.advancements.packs.VanillaTheEndAdvancements;

public class VanillaAdvancementProvider {
    public static AdvancementProvider create(PackOutput packOutput) {
        return new AdvancementProvider(packOutput, List.of(new VanillaTheEndAdvancements(), new VanillaHusbandryAdvancements(), new VanillaAdventureAdvancements(), new VanillaNetherAdvancements(), new VanillaStoryAdvancements()));
    }
}

