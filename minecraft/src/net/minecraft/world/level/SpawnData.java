package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData() {
	private final CompoundTag entityToSpawn;
	private final Optional<SpawnData.CustomSpawnRules> customSpawnRules;
	public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CompoundTag.CODEC.fieldOf("entity").forGetter(spawnData -> spawnData.entityToSpawn),
					SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(spawnData -> spawnData.customSpawnRules)
				)
				.apply(instance, SpawnData::new)
	);
	public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);
	public static final String DEFAULT_TYPE = "minecraft:pig";

	public SpawnData() {
		this(Util.make(new CompoundTag(), compoundTag -> compoundTag.putString("id", "minecraft:pig")), Optional.empty());
	}

	public SpawnData(CompoundTag compoundTag, Optional<SpawnData.CustomSpawnRules> optional) {
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("id"));
		compoundTag.putString("id", resourceLocation != null ? resourceLocation.toString() : "minecraft:pig");
		this.entityToSpawn = compoundTag;
		this.customSpawnRules = optional;
	}

	public CompoundTag getEntityToSpawn() {
		return this.entityToSpawn;
	}

	public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
		return this.customSpawnRules;
	}

	public static record CustomSpawnRules() {
		private final InclusiveRange<Integer> blockLightLimit;
		private final InclusiveRange<Integer> skyLightLimit;
		private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange(0, 15);
		public static final Codec<SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						InclusiveRange.INT
							.optionalFieldOf("block_light_limit", LIGHT_RANGE)
							.flatXmap(SpawnData.CustomSpawnRules::checkLightBoundaries, SpawnData.CustomSpawnRules::checkLightBoundaries)
							.forGetter(customSpawnRules -> customSpawnRules.blockLightLimit),
						InclusiveRange.INT
							.optionalFieldOf("sky_light_limit", LIGHT_RANGE)
							.flatXmap(SpawnData.CustomSpawnRules::checkLightBoundaries, SpawnData.CustomSpawnRules::checkLightBoundaries)
							.forGetter(customSpawnRules -> customSpawnRules.skyLightLimit)
					)
					.apply(instance, SpawnData.CustomSpawnRules::new)
		);

		public CustomSpawnRules(InclusiveRange<Integer> inclusiveRange, InclusiveRange<Integer> inclusiveRange2) {
			this.blockLightLimit = inclusiveRange;
			this.skyLightLimit = inclusiveRange2;
		}

		private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiveRange) {
			return !LIGHT_RANGE.contains(inclusiveRange) ? DataResult.error("Light values must be withing range " + LIGHT_RANGE) : DataResult.success(inclusiveRange);
		}
	}
}
