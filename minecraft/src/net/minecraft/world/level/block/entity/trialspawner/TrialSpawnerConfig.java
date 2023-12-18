package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public record TrialSpawnerConfig(
	int requiredPlayerRange,
	int spawnRange,
	float totalMobs,
	float simultaneousMobs,
	float totalMobsAddedPerPlayer,
	float simultaneousMobsAddedPerPlayer,
	int ticksBetweenSpawn,
	int targetCooldownLength,
	SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
	SimpleWeightedRandomList<ResourceLocation> lootTablesToEject
) {
	public static TrialSpawnerConfig DEFAULT = new TrialSpawnerConfig(
		14,
		4,
		6.0F,
		2.0F,
		2.0F,
		1.0F,
		40,
		36000,
		SimpleWeightedRandomList.empty(),
		SimpleWeightedRandomList.<ResourceLocation>builder()
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
			.build()
	);
	public static MapCodec<TrialSpawnerConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.intRange(1, 128).optionalFieldOf("required_player_range", DEFAULT.requiredPlayerRange).forGetter(TrialSpawnerConfig::requiredPlayerRange),
					Codec.intRange(1, 128).optionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
					Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
					Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs).forGetter(TrialSpawnerConfig::simultaneousMobs),
					Codec.floatRange(0.0F, Float.MAX_VALUE)
						.optionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
						.forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
					Codec.floatRange(0.0F, Float.MAX_VALUE)
						.optionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
						.forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
					Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn).forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
					Codec.intRange(0, Integer.MAX_VALUE)
						.optionalFieldOf("target_cooldown_length", DEFAULT.targetCooldownLength)
						.forGetter(TrialSpawnerConfig::targetCooldownLength),
					SpawnData.LIST_CODEC.optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
					SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceLocation.CODEC)
						.optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
						.forGetter(TrialSpawnerConfig::lootTablesToEject)
				)
				.apply(instance, TrialSpawnerConfig::new)
	);

	public int calculateTargetTotalMobs(int i) {
		return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)i));
	}

	public int calculateTargetSimultaneousMobs(int i) {
		return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)i));
	}
}
