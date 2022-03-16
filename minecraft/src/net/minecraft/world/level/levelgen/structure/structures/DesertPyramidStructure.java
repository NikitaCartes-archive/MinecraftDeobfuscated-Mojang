package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class DesertPyramidStructure extends SinglePieceStructure {
	public static final Codec<DesertPyramidStructure> CODEC = RecordCodecBuilder.create(instance -> codec(instance).apply(instance, DesertPyramidStructure::new));

	public DesertPyramidStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		super(DesertPyramidPiece::new, 21, 21, holderSet, map, decoration, bl);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.DESERT_PYRAMID;
	}
}
