package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class OceanRuinPieces {
	private static final ResourceLocation[] WARM_RUINS = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/warm_1"),
		new ResourceLocation("underwater_ruin/warm_2"),
		new ResourceLocation("underwater_ruin/warm_3"),
		new ResourceLocation("underwater_ruin/warm_4"),
		new ResourceLocation("underwater_ruin/warm_5"),
		new ResourceLocation("underwater_ruin/warm_6"),
		new ResourceLocation("underwater_ruin/warm_7"),
		new ResourceLocation("underwater_ruin/warm_8")
	};
	private static final ResourceLocation[] RUINS_BRICK = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/brick_1"),
		new ResourceLocation("underwater_ruin/brick_2"),
		new ResourceLocation("underwater_ruin/brick_3"),
		new ResourceLocation("underwater_ruin/brick_4"),
		new ResourceLocation("underwater_ruin/brick_5"),
		new ResourceLocation("underwater_ruin/brick_6"),
		new ResourceLocation("underwater_ruin/brick_7"),
		new ResourceLocation("underwater_ruin/brick_8")
	};
	private static final ResourceLocation[] RUINS_CRACKED = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/cracked_1"),
		new ResourceLocation("underwater_ruin/cracked_2"),
		new ResourceLocation("underwater_ruin/cracked_3"),
		new ResourceLocation("underwater_ruin/cracked_4"),
		new ResourceLocation("underwater_ruin/cracked_5"),
		new ResourceLocation("underwater_ruin/cracked_6"),
		new ResourceLocation("underwater_ruin/cracked_7"),
		new ResourceLocation("underwater_ruin/cracked_8")
	};
	private static final ResourceLocation[] RUINS_MOSSY = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/mossy_1"),
		new ResourceLocation("underwater_ruin/mossy_2"),
		new ResourceLocation("underwater_ruin/mossy_3"),
		new ResourceLocation("underwater_ruin/mossy_4"),
		new ResourceLocation("underwater_ruin/mossy_5"),
		new ResourceLocation("underwater_ruin/mossy_6"),
		new ResourceLocation("underwater_ruin/mossy_7"),
		new ResourceLocation("underwater_ruin/mossy_8")
	};
	private static final ResourceLocation[] BIG_RUINS_BRICK = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/big_brick_1"),
		new ResourceLocation("underwater_ruin/big_brick_2"),
		new ResourceLocation("underwater_ruin/big_brick_3"),
		new ResourceLocation("underwater_ruin/big_brick_8")
	};
	private static final ResourceLocation[] BIG_RUINS_MOSSY = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/big_mossy_1"),
		new ResourceLocation("underwater_ruin/big_mossy_2"),
		new ResourceLocation("underwater_ruin/big_mossy_3"),
		new ResourceLocation("underwater_ruin/big_mossy_8")
	};
	private static final ResourceLocation[] BIG_RUINS_CRACKED = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/big_cracked_1"),
		new ResourceLocation("underwater_ruin/big_cracked_2"),
		new ResourceLocation("underwater_ruin/big_cracked_3"),
		new ResourceLocation("underwater_ruin/big_cracked_8")
	};
	private static final ResourceLocation[] BIG_WARM_RUINS = new ResourceLocation[]{
		new ResourceLocation("underwater_ruin/big_warm_4"),
		new ResourceLocation("underwater_ruin/big_warm_5"),
		new ResourceLocation("underwater_ruin/big_warm_6"),
		new ResourceLocation("underwater_ruin/big_warm_7")
	};

	private static ResourceLocation getSmallWarmRuin(Random random) {
		return Util.getRandom(WARM_RUINS, random);
	}

	private static ResourceLocation getBigWarmRuin(Random random) {
		return Util.getRandom(BIG_WARM_RUINS, random);
	}

	public static void addPieces(
		StructureManager structureManager,
		BlockPos blockPos,
		Rotation rotation,
		StructurePieceAccessor structurePieceAccessor,
		Random random,
		OceanRuinConfiguration oceanRuinConfiguration
	) {
		boolean bl = random.nextFloat() <= oceanRuinConfiguration.largeProbability;
		float f = bl ? 0.9F : 0.8F;
		addPiece(structureManager, blockPos, rotation, structurePieceAccessor, random, oceanRuinConfiguration, bl, f);
		if (bl && random.nextFloat() <= oceanRuinConfiguration.clusterProbability) {
			addClusterRuins(structureManager, random, rotation, blockPos, oceanRuinConfiguration, structurePieceAccessor);
		}
	}

	private static void addClusterRuins(
		StructureManager structureManager,
		Random random,
		Rotation rotation,
		BlockPos blockPos,
		OceanRuinConfiguration oceanRuinConfiguration,
		StructurePieceAccessor structurePieceAccessor
	) {
		BlockPos blockPos2 = new BlockPos(blockPos.getX(), 90, blockPos.getZ());
		BlockPos blockPos3 = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, rotation, BlockPos.ZERO).offset(blockPos2);
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos2, blockPos3);
		BlockPos blockPos4 = new BlockPos(Math.min(blockPos2.getX(), blockPos3.getX()), blockPos2.getY(), Math.min(blockPos2.getZ(), blockPos3.getZ()));
		List<BlockPos> list = allPositions(random, blockPos4);
		int i = Mth.nextInt(random, 4, 8);

		for (int j = 0; j < i; j++) {
			if (!list.isEmpty()) {
				int k = random.nextInt(list.size());
				BlockPos blockPos5 = (BlockPos)list.remove(k);
				Rotation rotation2 = Rotation.getRandom(random);
				BlockPos blockPos6 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, rotation2, BlockPos.ZERO).offset(blockPos5);
				BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos5, blockPos6);
				if (!boundingBox2.intersects(boundingBox)) {
					addPiece(structureManager, blockPos5, rotation2, structurePieceAccessor, random, oceanRuinConfiguration, false, 0.8F);
				}
			}
		}
	}

	private static List<BlockPos> allPositions(Random random, BlockPos blockPos) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();
		list.add(blockPos.offset(-16 + Mth.nextInt(random, 1, 8), 0, 16 + Mth.nextInt(random, 1, 7)));
		list.add(blockPos.offset(-16 + Mth.nextInt(random, 1, 8), 0, Mth.nextInt(random, 1, 7)));
		list.add(blockPos.offset(-16 + Mth.nextInt(random, 1, 8), 0, -16 + Mth.nextInt(random, 4, 8)));
		list.add(blockPos.offset(Mth.nextInt(random, 1, 7), 0, 16 + Mth.nextInt(random, 1, 7)));
		list.add(blockPos.offset(Mth.nextInt(random, 1, 7), 0, -16 + Mth.nextInt(random, 4, 6)));
		list.add(blockPos.offset(16 + Mth.nextInt(random, 1, 7), 0, 16 + Mth.nextInt(random, 3, 8)));
		list.add(blockPos.offset(16 + Mth.nextInt(random, 1, 7), 0, Mth.nextInt(random, 1, 7)));
		list.add(blockPos.offset(16 + Mth.nextInt(random, 1, 7), 0, -16 + Mth.nextInt(random, 4, 8)));
		return list;
	}

	private static void addPiece(
		StructureManager structureManager,
		BlockPos blockPos,
		Rotation rotation,
		StructurePieceAccessor structurePieceAccessor,
		Random random,
		OceanRuinConfiguration oceanRuinConfiguration,
		boolean bl,
		float f
	) {
		switch (oceanRuinConfiguration.biomeTemp) {
			case WARM:
			default:
				ResourceLocation resourceLocation = bl ? getBigWarmRuin(random) : getSmallWarmRuin(random);
				structurePieceAccessor.addPiece(
					new OceanRuinPieces.OceanRuinPiece(structureManager, resourceLocation, blockPos, rotation, f, oceanRuinConfiguration.biomeTemp, bl)
				);
				break;
			case COLD:
				ResourceLocation[] resourceLocations = bl ? BIG_RUINS_BRICK : RUINS_BRICK;
				ResourceLocation[] resourceLocations2 = bl ? BIG_RUINS_CRACKED : RUINS_CRACKED;
				ResourceLocation[] resourceLocations3 = bl ? BIG_RUINS_MOSSY : RUINS_MOSSY;
				int i = random.nextInt(resourceLocations.length);
				structurePieceAccessor.addPiece(
					new OceanRuinPieces.OceanRuinPiece(structureManager, resourceLocations[i], blockPos, rotation, f, oceanRuinConfiguration.biomeTemp, bl)
				);
				structurePieceAccessor.addPiece(
					new OceanRuinPieces.OceanRuinPiece(structureManager, resourceLocations2[i], blockPos, rotation, 0.7F, oceanRuinConfiguration.biomeTemp, bl)
				);
				structurePieceAccessor.addPiece(
					new OceanRuinPieces.OceanRuinPiece(structureManager, resourceLocations3[i], blockPos, rotation, 0.5F, oceanRuinConfiguration.biomeTemp, bl)
				);
		}
	}

	public static class OceanRuinPiece extends TemplateStructurePiece {
		private final OceanRuinFeature.Type biomeType;
		private final float integrity;
		private final boolean isLarge;

		public OceanRuinPiece(
			StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, float f, OceanRuinFeature.Type type, boolean bl
		) {
			super(StructurePieceType.OCEAN_RUIN, 0, structureManager, resourceLocation, resourceLocation.toString(), makeSettings(rotation), blockPos);
			this.integrity = f;
			this.biomeType = type;
			this.isLarge = bl;
		}

		public OceanRuinPiece(ServerLevel serverLevel, CompoundTag compoundTag) {
			super(StructurePieceType.OCEAN_RUIN, compoundTag, serverLevel, resourceLocation -> makeSettings(Rotation.valueOf(compoundTag.getString("Rot"))));
			this.integrity = compoundTag.getFloat("Integrity");
			this.biomeType = OceanRuinFeature.Type.valueOf(compoundTag.getString("BiomeType"));
			this.isLarge = compoundTag.getBoolean("IsLarge");
		}

		private static StructurePlaceSettings makeSettings(Rotation rotation) {
			return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		}

		@Override
		protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
			super.addAdditionalSaveData(serverLevel, compoundTag);
			compoundTag.putString("Rot", this.placeSettings.getRotation().name());
			compoundTag.putFloat("Integrity", this.integrity);
			compoundTag.putString("BiomeType", this.biomeType.toString());
			compoundTag.putBoolean("IsLarge", this.isLarge);
		}

		@Override
		protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
			if ("chest".equals(string)) {
				serverLevelAccessor.setBlock(
					blockPos,
					Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, Boolean.valueOf(serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER))),
					2
				);
				BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
				if (blockEntity instanceof ChestBlockEntity) {
					((ChestBlockEntity)blockEntity)
						.setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, random.nextLong());
				}
			} else if ("drowned".equals(string)) {
				Drowned drowned = EntityType.DROWNED.create(serverLevelAccessor.getLevel());
				drowned.setPersistenceRequired();
				drowned.moveTo(blockPos, 0.0F, 0.0F);
				drowned.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(blockPos), MobSpawnType.STRUCTURE, null, null);
				serverLevelAccessor.addFreshEntityWithPassengers(drowned);
				if (blockPos.getY() > serverLevelAccessor.getSeaLevel()) {
					serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
				} else {
					serverLevelAccessor.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 2);
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
			this.placeSettings.clearProcessors().addProcessor(new BlockRotProcessor(this.integrity)).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
			int i = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
			this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());
			BlockPos blockPos2 = StructureTemplate.transform(
					new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.placeSettings.getRotation(), BlockPos.ZERO
				)
				.offset(this.templatePosition);
			this.templatePosition = new BlockPos(
				this.templatePosition.getX(), this.getHeight(this.templatePosition, worldGenLevel, blockPos2), this.templatePosition.getZ()
			);
			return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
		}

		private int getHeight(BlockPos blockPos, BlockGetter blockGetter, BlockPos blockPos2) {
			int i = blockPos.getY();
			int j = 512;
			int k = i - 1;
			int l = 0;

			for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
				int m = blockPos3.getX();
				int n = blockPos3.getZ();
				int o = blockPos.getY() - 1;
				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(m, o, n);
				BlockState blockState = blockGetter.getBlockState(mutableBlockPos);

				for (FluidState fluidState = blockGetter.getFluidState(mutableBlockPos);
					(blockState.isAir() || fluidState.is(FluidTags.WATER) || blockState.is(BlockTags.ICE)) && o > blockGetter.getMinBuildHeight() + 1;
					fluidState = blockGetter.getFluidState(mutableBlockPos)
				) {
					mutableBlockPos.set(m, --o, n);
					blockState = blockGetter.getBlockState(mutableBlockPos);
				}

				j = Math.min(j, o);
				if (o < k - 2) {
					l++;
				}
			}

			int p = Math.abs(blockPos.getX() - blockPos2.getX());
			if (k - j > 2 && l > p - 2) {
				i = j + 1;
			}

			return i;
		}
	}
}
