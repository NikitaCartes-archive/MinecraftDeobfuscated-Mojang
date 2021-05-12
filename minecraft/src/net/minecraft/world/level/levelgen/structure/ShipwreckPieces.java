package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ShipwreckPieces {
	static final BlockPos PIVOT = new BlockPos(4, 0, 15);
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
		StructurePieceAccessor structurePieceAccessor,
		Random random,
		ShipwreckConfiguration shipwreckConfiguration
	) {
		ResourceLocation resourceLocation = Util.getRandom(shipwreckConfiguration.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, random);
		structurePieceAccessor.addPiece(new ShipwreckPieces.ShipwreckPiece(structureManager, resourceLocation, blockPos, rotation, shipwreckConfiguration.isBeached));
	}

	public static class ShipwreckPiece extends TemplateStructurePiece {
		private final boolean isBeached;

		public ShipwreckPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, boolean bl) {
			super(StructurePieceType.SHIPWRECK_PIECE, 0, structureManager, resourceLocation, resourceLocation.toString(), makeSettings(rotation), blockPos);
			this.isBeached = bl;
		}

		public ShipwreckPiece(ServerLevel serverLevel, CompoundTag compoundTag) {
			super(StructurePieceType.SHIPWRECK_PIECE, compoundTag, serverLevel, resourceLocation -> makeSettings(Rotation.valueOf(compoundTag.getString("Rot"))));
			this.isBeached = compoundTag.getBoolean("isBeached");
		}

		@Override
		protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
			super.addAdditionalSaveData(serverLevel, compoundTag);
			compoundTag.putBoolean("isBeached", this.isBeached);
			compoundTag.putString("Rot", this.placeSettings.getRotation().name());
		}

		private static StructurePlaceSettings makeSettings(Rotation rotation) {
			return new StructurePlaceSettings()
				.setRotation(rotation)
				.setMirror(Mirror.NONE)
				.setRotationPivot(ShipwreckPieces.PIVOT)
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
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
			int i = worldGenLevel.getMaxBuildHeight();
			int j = 0;
			Vec3i vec3i = this.template.getSize();
			Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
			int k = vec3i.getX() * vec3i.getZ();
			if (k == 0) {
				j = worldGenLevel.getHeight(types, this.templatePosition.getX(), this.templatePosition.getZ());
			} else {
				BlockPos blockPos2 = this.templatePosition.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);

				for (BlockPos blockPos3 : BlockPos.betweenClosed(this.templatePosition, blockPos2)) {
					int l = worldGenLevel.getHeight(types, blockPos3.getX(), blockPos3.getZ());
					j += l;
					i = Math.min(i, l);
				}

				j /= k;
			}

			int m = this.isBeached ? i - vec3i.getY() / 2 - random.nextInt(3) : j;
			this.templatePosition = new BlockPos(this.templatePosition.getX(), m, this.templatePosition.getZ());
			return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
		}
	}
}
