package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
	private static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
	private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
	private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
	private static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(
		STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7)
	);
	private static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(
		STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2)
	);

	public static void addPieces(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random) {
		if (random.nextDouble() < 0.5) {
			int i = random.nextInt(8) + 4;
			list.add(new IglooPieces.IglooPiece(structureManager, STRUCTURE_LOCATION_LABORATORY, blockPos, rotation, i * 3));

			for (int j = 0; j < i - 1; j++) {
				list.add(new IglooPieces.IglooPiece(structureManager, STRUCTURE_LOCATION_LADDER, blockPos, rotation, j * 3));
			}
		}

		list.add(new IglooPieces.IglooPiece(structureManager, STRUCTURE_LOCATION_IGLOO, blockPos, rotation, 0));
	}

	public static class IglooPiece extends TemplateStructurePiece {
		private final ResourceLocation templateLocation;
		private final Rotation rotation;

		public IglooPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, int i) {
			super(StructurePieceType.IGLOO, 0);
			this.templateLocation = resourceLocation;
			BlockPos blockPos2 = (BlockPos)IglooPieces.OFFSETS.get(resourceLocation);
			this.templatePosition = blockPos.offset(blockPos2.getX(), blockPos2.getY() - i, blockPos2.getZ());
			this.rotation = rotation;
			this.loadTemplate(structureManager);
		}

		public IglooPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.IGLOO, compoundTag);
			this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
			this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
			this.loadTemplate(structureManager);
		}

		private void loadTemplate(StructureManager structureManager) {
			StructureTemplate structureTemplate = structureManager.getOrCreate(this.templateLocation);
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings()
				.setRotation(this.rotation)
				.setMirror(Mirror.NONE)
				.setRotationPivot((BlockPos)IglooPieces.PIVOTS.get(this.templateLocation))
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
			this.setup(structureTemplate, this.templatePosition, structurePlaceSettings);
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.putString("Template", this.templateLocation.toString());
			compoundTag.putString("Rot", this.rotation.name());
		}

		@Override
		protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
			if ("chest".equals(string)) {
				serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
				BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos.below());
				if (blockEntity instanceof ChestBlockEntity) {
					((ChestBlockEntity)blockEntity).setLootTable(BuiltInLootTables.IGLOO_CHEST, random.nextLong());
				}
			}
		}

		@Override
		public boolean postProcess(
			WorldGenLevel worldGenLevel,
			StructureFeatureManager structureFeatureManager,
			ChunkGenerator chunkGenerator,
			Random random,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings()
				.setRotation(this.rotation)
				.setMirror(Mirror.NONE)
				.setRotationPivot((BlockPos)IglooPieces.PIVOTS.get(this.templateLocation))
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
			BlockPos blockPos2 = (BlockPos)IglooPieces.OFFSETS.get(this.templateLocation);
			BlockPos blockPos3 = this.templatePosition
				.offset(StructureTemplate.calculateRelativePosition(structurePlaceSettings, new BlockPos(3 - blockPos2.getX(), 0, 0 - blockPos2.getZ())));
			int i = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockPos3.getX(), blockPos3.getZ());
			BlockPos blockPos4 = this.templatePosition;
			this.templatePosition = this.templatePosition.offset(0, i - 90 - 1, 0);
			boolean bl = super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
			if (this.templateLocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
				BlockPos blockPos5 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structurePlaceSettings, new BlockPos(3, 0, 5)));
				BlockState blockState = worldGenLevel.getBlockState(blockPos5.below());
				if (!blockState.isAir() && !blockState.is(Blocks.LADDER)) {
					worldGenLevel.setBlock(blockPos5, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
				}
			}

			this.templatePosition = blockPos4;
			return bl;
		}
	}
}
