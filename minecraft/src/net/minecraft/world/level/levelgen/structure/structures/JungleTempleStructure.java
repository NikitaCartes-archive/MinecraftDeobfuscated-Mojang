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

public class JungleTempleStructure extends SinglePieceStructure {
	public static final Codec<JungleTempleStructure> CODEC = RecordCodecBuilder.create(instance -> codec(instance).apply(instance, JungleTempleStructure::new));

	public JungleTempleStructure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, boolean bl) {
		super(JungleTemplePiece::new, 12, 15, holderSet, map, decoration, bl);
	}

	@Override
	public StructureType<?> type() {
		return StructureType.JUNGLE_TEMPLE;
	}
}
