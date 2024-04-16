package net.minecraft.world.level.levelgen.structure.structures;

import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class ShipwreckPieces {
	private static final int NUMBER_OF_BLOCKS_ALLOWED_IN_WORLD_GEN_REGION = 32;
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
	static final Map<String, ResourceKey<LootTable>> MARKERS_TO_LOOT = Map.of(
		"map_chest", BuiltInLootTables.SHIPWRECK_MAP, "treasure_chest", BuiltInLootTables.SHIPWRECK_TREASURE, "supply_chest", BuiltInLootTables.SHIPWRECK_SUPPLY
	);

	public static ShipwreckPieces.ShipwreckPiece addRandomPiece(
		StructureTemplateManager structureTemplateManager,
		BlockPos blockPos,
		Rotation rotation,
		StructurePieceAccessor structurePieceAccessor,
		RandomSource randomSource,
		boolean bl
	) {
		ResourceLocation resourceLocation = Util.getRandom(bl ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, randomSource);
		ShipwreckPieces.ShipwreckPiece shipwreckPiece = new ShipwreckPieces.ShipwreckPiece(structureTemplateManager, resourceLocation, blockPos, rotation, bl);
		structurePieceAccessor.addPiece(shipwreckPiece);
		return shipwreckPiece;
	}

	public static class ShipwreckPiece extends TemplateStructurePiece {
		private final boolean isBeached;

		public ShipwreckPiece(StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, boolean bl) {
			super(StructurePieceType.SHIPWRECK_PIECE, 0, structureTemplateManager, resourceLocation, resourceLocation.toString(), makeSettings(rotation), blockPos);
			this.isBeached = bl;
		}

		public ShipwreckPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
			super(
				StructurePieceType.SHIPWRECK_PIECE, compoundTag, structureTemplateManager, resourceLocation -> makeSettings(Rotation.valueOf(compoundTag.getString("Rot")))
			);
			this.isBeached = compoundTag.getBoolean("isBeached");
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
			super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
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
		protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
			ResourceKey<LootTable> resourceKey = (ResourceKey<LootTable>)ShipwreckPieces.MARKERS_TO_LOOT.get(string);
			if (resourceKey != null) {
				RandomizableContainer.setBlockEntityLootTable(serverLevelAccessor, randomSource, blockPos.below(), resourceKey);
			}
		}

		@Override
		public void postProcess(
			WorldGenLevel worldGenLevel,
			StructureManager structureManager,
			ChunkGenerator chunkGenerator,
			RandomSource randomSource,
			BoundingBox boundingBox,
			ChunkPos chunkPos,
			BlockPos blockPos
		) {
			if (this.isTooBigToFitInWorldGenRegion()) {
				super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
			} else {
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

				this.adjustPositionHeight(this.isBeached ? this.calculateBeachedPosition(i, randomSource) : j);
				super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
			}
		}

		public boolean isTooBigToFitInWorldGenRegion() {
			Vec3i vec3i = this.template.getSize();
			return vec3i.getX() > 32 || vec3i.getY() > 32;
		}

		public int calculateBeachedPosition(int i, RandomSource randomSource) {
			return i - this.template.getSize().getY() / 2 - randomSource.nextInt(3);
		}

		public void adjustPositionHeight(int i) {
			this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());
		}
	}
}
