package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
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

public record StructureSet(List<StructureSet.StructureSelectionEntry> structures, StructurePlacement placement) {
	public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					StructureSet.StructureSelectionEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures),
					StructurePlacement.CODEC.fieldOf("placement").forGetter(StructureSet::placement)
				)
				.apply(instance, StructureSet::new)
	);
	public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registry.STRUCTURE_SET_REGISTRY, DIRECT_CODEC);

	public StructureSet(Holder<ConfiguredStructureFeature<?, ?>> holder, StructurePlacement structurePlacement) {
		this(List.of(new StructureSet.StructureSelectionEntry(holder, 1)), structurePlacement);
	}

	public static StructureSet.StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> holder, int i) {
		return new StructureSet.StructureSelectionEntry(holder, i);
	}

	public static StructureSet.StructureSelectionEntry entry(Holder<ConfiguredStructureFeature<?, ?>> holder) {
		return new StructureSet.StructureSelectionEntry(holder, 1);
	}

	public static record StructureSelectionEntry(Holder<ConfiguredStructureFeature<?, ?>> structure, int weight) {
		public static final Codec<StructureSet.StructureSelectionEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ConfiguredStructureFeature.CODEC.fieldOf("structure").forGetter(StructureSet.StructureSelectionEntry::structure),
						ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(StructureSet.StructureSelectionEntry::weight)
					)
					.apply(instance, StructureSet.StructureSelectionEntry::new)
		);

		public boolean generatesInMatchingBiome(Predicate<Holder<Biome>> predicate) {
			HolderSet<Biome> holderSet = this.structure().value().biomes();
			return holderSet.stream().anyMatch(predicate);
		}
	}
}
