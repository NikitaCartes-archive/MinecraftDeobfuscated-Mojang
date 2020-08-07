package net.minecraft.world.level.levelgen.structure;

import java.util.List;
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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ShipwreckPieces {
	private static final BlockPos PIVOT = new BlockPos(4, 0, 15);
	private static final ResourceLocation[] STRUCTURE_LOCATION_BEACHED = new ResourceLocation[]{
		new ResourceLocation("shipwreck/with_mast"),
		new ResourceLocation("shipwreck/sideways_full"),
		new ResourceLocation("shipwreck/sideways_fronthalf"),
		new ResourceLocation("shipwreck/sideways_backhalf"),
		new ResourceLocation("shipwreck/rightsideup_full"),
		new ResourceLocation("shipwreck/rightsideup_fronthalf"),
		new ResourceLocation("shipwreck/rightsideup_backhalf"),
		new ResourceLocation("shipwreck/with_mast_degraded"),
		new ResourceLocation("shipwreck/rightsideup_full_degraded"),
		new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"),
		new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")
	};
	private static final ResourceLocation[] STRUCTURE_LOCATION_OCEAN = new ResourceLocation[]{
		new ResourceLocation("shipwreck/with_mast"),
		new ResourceLocation("shipwreck/upsidedown_full"),
		new ResourceLocation("shipwreck/upsidedown_fronthalf"),
		new ResourceLocation("shipwreck/upsidedown_backhalf"),
		new ResourceLocation("shipwreck/sideways_full"),
		new ResourceLocation("shipwreck/sideways_fronthalf"),
		new ResourceLocation("shipwreck/sideways_backhalf"),
		new ResourceLocation("shipwreck/rightsideup_full"),
		new ResourceLocation("shipwreck/rightsideup_fronthalf"),
		new ResourceLocation("shipwreck/rightsideup_backhalf"),
		new ResourceLocation("shipwreck/with_mast_degraded"),
		new ResourceLocation("shipwreck/upsidedown_full_degraded"),
		new ResourceLocation("shipwreck/upsidedown_fronthalf_degraded"),
		new ResourceLocation("shipwreck/upsidedown_backhalf_degraded"),
		new ResourceLocation("shipwreck/sideways_full_degraded"),
		new ResourceLocation("shipwreck/sideways_fronthalf_degraded"),
		new ResourceLocation("shipwreck/sideways_backhalf_degraded"),
		new ResourceLocation("shipwreck/rightsideup_full_degraded"),
		new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"),
		new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")
	};

	public static void addPieces(
		StructureManager structureManager,
		BlockPos blockPos,
		Rotation rotation,
		List<StructurePiece> list,
		Random random,
		ShipwreckConfiguration shipwreckConfiguration
	) {
		ResourceLocation resourceLocation = Util.getRandom(shipwreckConfiguration.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, random);
		list.add(new ShipwreckPieces.ShipwreckPiece(structureManager, resourceLocation, blockPos, rotation, shipwreckConfiguration.isBeached));
	}

	public static class ShipwreckPiece extends TemplateStructurePiece {
		private final Rotation rotation;
		private final ResourceLocation templateLocation;
		private final boolean isBeached;

		public ShipwreckPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, boolean bl) {
			super(StructurePieceType.SHIPWRECK_PIECE, 0);
			this.templatePosition = blockPos;
			this.rotation = rotation;
			this.templateLocation = resourceLocation;
			this.isBeached = bl;
			this.loadTemplate(structureManager);
		}

		public ShipwreckPiece(StructureManager structureManager, CompoundTag compoundTag) {
			super(StructurePieceType.SHIPWRECK_PIECE, compoundTag);
			this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
			this.isBeached = compoundTag.getBoolean("isBeached");
			this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
			this.loadTemplate(structureManager);
		}

		@Override
		protected void addAdditionalSaveData(CompoundTag compoundTag) {
			super.addAdditionalSaveData(compoundTag);
			compoundTag.putString("Template", this.templateLocation.toString());
			compoundTag.putBoolean("isBeached", this.isBeached);
			compoundTag.putString("Rot", this.rotation.name());
		}

		private void loadTemplate(StructureManager structureManager) {
			StructureTemplate structureTemplate = structureManager.getOrCreate(this.templateLocation);
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings()
				.setRotation(this.rotation)
				.setMirror(Mirror.NONE)
				.setRotationPivot(ShipwreckPieces.PIVOT)
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
			this.setup(structureTemplate, this.templatePosition, structurePlaceSettings);
		}

		@Override
		protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
			if ("map_chest".equals(string)) {
				RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_MAP);
			} else if ("treasure_chest".equals(string)) {
				RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_TREASURE);
			} else if ("supply_chest".equals(string)) {
				RandomizableContainerBlockEntity.setLootTable(serverLevelAccessor, random, blockPos.below(), BuiltInLootTables.SHIPWRECK_SUPPLY);
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
			int i = 256;
			int j = 0;
			BlockPos blockPos2 = this.template.getSize();
			Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
			int k = blockPos2.getX() * blockPos2.getZ();
			if (k == 0) {
				j = worldGenLevel.getHeight(types, this.templatePosition.getX(), this.templatePosition.getZ());
			} else {
				BlockPos blockPos3 = this.templatePosition.offset(blockPos2.getX() - 1, 0, blockPos2.getZ() - 1);

				for (BlockPos blockPos4 : BlockPos.betweenClosed(this.templatePosition, blockPos3)) {
					int l = worldGenLevel.getHeight(types, blockPos4.getX(), blockPos4.getZ());
					j += l;
					i = Math.min(i, l);
				}

				j /= k;
			}

			int m = this.isBeached ? i - blockPos2.getY() / 2 - random.nextInt(3) : j;
			this.templatePosition = new BlockPos(this.templatePosition.getX(), m, this.templatePosition.getZ());
			return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
		}
	}
}
