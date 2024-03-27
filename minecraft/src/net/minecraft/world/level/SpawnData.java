package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules, Optional<ResourceLocation> equipmentLootTable) {
	public static final String ENTITY_TAG = "entity";
	public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CompoundTag.CODEC.fieldOf("entity").forGetter(spawnData -> spawnData.entityToSpawn),
					SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(spawnData -> spawnData.customSpawnRules),
					ResourceLocation.CODEC.optionalFieldOf("equipment_loot_table").forGetter(spawnData -> spawnData.equipmentLootTable)
				)
				.apply(instance, SpawnData::new)
	);
	public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

	public SpawnData() {
		this(new CompoundTag(), Optional.empty(), Optional.empty());
	}

	public SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules, Optional<ResourceLocation> equipmentLootTable) {
		if (entityToSpawn.contains("id")) {
			ResourceLocation resourceLocation = ResourceLocation.tryParse(entityToSpawn.getString("id"));
			if (resourceLocation != null) {
				entityToSpawn.putString("id", resourceLocation.toString());
			} else {
				entityToSpawn.remove("id");
			}
		}

		this.entityToSpawn = entityToSpawn;
		this.customSpawnRules = customSpawnRules;
		this.equipmentLootTable = equipmentLootTable;
	}

	public CompoundTag getEntityToSpawn() {
		return this.entityToSpawn;
	}

	public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
		return this.customSpawnRules;
	}

	public Optional<ResourceLocation> getEquipmentLootTable() {
		return this.equipmentLootTable;
	}

	public static record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
		private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange(0, 15);
		public static final Codec<SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						lightLimit("block_light_limit").forGetter(customSpawnRules -> customSpawnRules.blockLightLimit),
						lightLimit("sky_light_limit").forGetter(customSpawnRules -> customSpawnRules.skyLightLimit)
					)
					.apply(instance, SpawnData.CustomSpawnRules::new)
		);

		private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiveRange) {
			return !LIGHT_RANGE.contains(inclusiveRange)
				? DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE)
				: DataResult.success(inclusiveRange);
		}

		private static MapCodec<InclusiveRange<Integer>> lightLimit(String string) {
			return InclusiveRange.INT.lenientOptionalFieldOf(string, LIGHT_RANGE).validate(SpawnData.CustomSpawnRules::checkLightBoundaries);
		}

		public boolean isValidPosition(BlockPos blockPos, ServerLevel serverLevel) {
			return this.blockLightLimit.isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos))
				&& this.skyLightLimit.isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos));
		}
	}
}
