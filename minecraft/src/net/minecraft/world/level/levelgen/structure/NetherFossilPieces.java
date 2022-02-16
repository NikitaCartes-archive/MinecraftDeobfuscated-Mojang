package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class NetherFossilPieces {
	private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{
		new ResourceLocation("nether_fossils/fossil_1"),
		new ResourceLocation("nether_fossils/fossil_2"),
		new ResourceLocation("nether_fossils/fossil_3"),
		new ResourceLocation("nether_fossils/fossil_4"),
		new ResourceLocation("nether_fossils/fossil_5"),
		new ResourceLocation("nether_fossils/fossil_6"),
		new ResourceLocation("nether_fossils/fossil_7"),
		new ResourceLocation("nether_fossils/fossil_8"),
		new ResourceLocation("nether_fossils/fossil_9"),
		new ResourceLocation("nether_fossils/fossil_10"),
		new ResourceLocation("nether_fossils/fossil_11"),
		new ResourceLocation("nether_fossils/fossil_12"),
		new ResourceLocation("nether_fossils/fossil_13"),
		new ResourceLocation("nether_fossils/fossil_14")
	};

	public static void addPieces(StructureManager structureManager, StructurePieceAccessor structurePieceAccessor, Random random, BlockPos blockPos) {
		Rotation rotation = Rotation.getRandom(random);
		structurePieceAccessor.addPiece(new NetherFossilPieces.NetherFossilPiece(structureManager, Util.getRandom(FOSSILS, random), blockPos, rotation));
	}

	public static class NetherFossilPiece extends TemplateStructurePiece {
		public NetherFossilPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation) {
			super(StructurePieceType.NETHER_FOSSIL, 0, structureManager, resourceLocation, resourceLocation.toString(), makeSettings(rotation), blockPos);
		}

		public NetherFossilPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.NETHER_FOSSIL, compoundTag, structureManager, resourceLocation -> makeSettings(Rotation.valueOf(compoundTag.getString("Rot"))));
		}

		private static StructurePlaceSettings makeSettings(Rotation rotation) {
			return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
			compoundTag.putString("Rot", this.placeSettings.getRotation().name());
		}

		@Override
		protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			boundingBox.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
			super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
		}
	}
}
