package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PoolElementStructurePieceNoiseEffect extends PoolElementStructurePiece {
	public NoiseEffect noiseEffect;

	public PoolElementStructurePieceNoiseEffect(
		StructureManager structureManager,
		StructurePoolElement structurePoolElement,
		BlockPos blockPos,
		int i,
		Rotation rotation,
		BoundingBox boundingBox,
		NoiseEffect noiseEffect
	) {
		super(structureManager, structurePoolElement, blockPos, i, rotation, boundingBox);
		this.noiseEffect = noiseEffect;
	}

	public PoolElementStructurePieceNoiseEffect(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		super(structurePieceSerializationContext, compoundTag);
		this.noiseEffect = NoiseEffect.valueOf(compoundTag.getString("noise_effect"));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
		compoundTag.putString("noise_effect", this.noiseEffect.name());
	}

	@Override
	public NoiseEffect getNoiseEffect() {
		return this.noiseEffect;
	}
}
