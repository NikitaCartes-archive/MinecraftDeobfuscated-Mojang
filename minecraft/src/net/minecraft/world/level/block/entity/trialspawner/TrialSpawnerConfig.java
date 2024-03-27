package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record TrialSpawnerConfig(
	int spawnRange,
	float totalMobs,
	float simultaneousMobs,
	float totalMobsAddedPerPlayer,
	float simultaneousMobsAddedPerPlayer,
	int ticksBetweenSpawn,
	SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
	SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject,
	ResourceKey<LootTable> itemsToDropWhenOminous
) {
	public static final TrialSpawnerConfig DEFAULT = new TrialSpawnerConfig(
		4,
		6.0F,
		2.0F,
		2.0F,
		1.0F,
		40,
		SimpleWeightedRandomList.empty(),
		SimpleWeightedRandomList.<ResourceKey<LootTable>>builder()
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
			.build(),
		BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
	);
	public static final Codec<TrialSpawnerConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(1, 128).lenientOptionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
					Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
					Codec.floatRange(0.0F, Float.MAX_VALUE)
						.lenientOptionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs)
						.forGetter(TrialSpawnerConfig::simultaneousMobs),
					Codec.floatRange(0.0F, Float.MAX_VALUE)
						.lenientOptionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
						.forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
					Codec.floatRange(0.0F, Float.MAX_VALUE)
						.lenientOptionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
						.forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
					Codec.intRange(0, Integer.MAX_VALUE)
						.lenientOptionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn)
						.forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
					SpawnData.LIST_CODEC.lenientOptionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
					SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE))
						.lenientOptionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
						.forGetter(TrialSpawnerConfig::lootTablesToEject),
					ResourceKey.codec(Registries.LOOT_TABLE)
						.lenientOptionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
						.forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
				)
				.apply(instance, TrialSpawnerConfig::new)
	);

	public int calculateTargetTotalMobs(int i) {
		return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)i));
	}

	public int calculateTargetSimultaneousMobs(int i) {
		return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)i));
	}

	public long ticksBetweenItemSpawners() {
		return 160L;
	}
}
