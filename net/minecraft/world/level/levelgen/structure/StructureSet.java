/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List<StructureSelectionEntry> structures, StructurePlacement placement) {
    public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StructureSelectionEntry.CODEC.listOf().fieldOf("structures")).forGetter(StructureSet::structures), ((MapCodec)StructurePlacement.CODEC.fieldOf("placement")).forGetter(StructureSet::placement)).apply((Applicative<StructureSet, ?>)instance, StructureSet::new));
    public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_SET_REGISTRY, DIRECT_CODEC);

    public StructureSet(Holder<ConfiguredStructureFeature<?, ?>> holder, StructurePlacement structurePlacement) {
        this(List.of(new StructureSelectionEntry(holder, 1)), structurePlacement);
    }

    public static StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> holder, int i) {
        return new StructureSelectionEntry(holder, i);
    }

    public static StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> holder) {
        return new StructureSelectionEntry(holder, 1);
    }

    public record StructureSelectionEntry(Holder<ConfiguredStructureFeature<?, ?>> structure, int weight) {
        public static final Codec<StructureSelectionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredStructureFeature.CODEC.fieldOf("structure")).forGetter(StructureSelectionEntry::structure), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("weight")).forGetter(StructureSelectionEntry::weight)).apply((Applicative<StructureSelectionEntry, ?>)instance, StructureSelectionEntry::new));

        public boolean generatesInMatchingBiome(Predicate<Holder<Biome>> predicate) {
            HolderSet<Biome> holderSet = this.structure().value().biomes();
            return holderSet.stream().anyMatch(predicate);
        }
    }
}

