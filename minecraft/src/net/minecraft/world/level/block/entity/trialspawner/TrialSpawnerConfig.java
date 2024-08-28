package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
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
	public static final TrialSpawnerConfig DEFAULT = builder().build();
	public static final Codec<TrialSpawnerConfig> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
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
					SpawnData.LIST_CODEC.optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
					SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE))
						.optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
						.forGetter(TrialSpawnerConfig::lootTablesToEject),
					ResourceKey.codec(Registries.LOOT_TABLE)
						.optionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
						.forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
				)
				.apply(instance, TrialSpawnerConfig::new)
	);
	public static final Codec<Holder<TrialSpawnerConfig>> CODEC = RegistryFileCodec.create(Registries.TRIAL_SPAWNER_CONFIG, DIRECT_CODEC);

	public int calculateTargetTotalMobs(int i) {
		return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)i));
	}

	public int calculateTargetSimultaneousMobs(int i) {
		return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)i));
	}

	public long ticksBetweenItemSpawners() {
		return 160L;
	}

	public static TrialSpawnerConfig.Builder builder() {
		return new TrialSpawnerConfig.Builder();
	}

	public static class Builder {
		private int spawnRange = 4;
		private float totalMobs = 6.0F;
		private float simultaneousMobs = 2.0F;
		private float totalMobsAddedPerPlayer = 2.0F;
		private float simultaneousMobsAddedPerPlayer = 1.0F;
		private int ticksBetweenSpawn = 40;
		private SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition = SimpleWeightedRandomList.empty();
		private SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject = SimpleWeightedRandomList.<ResourceKey<LootTable>>builder()
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
			.add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
			.build();
		private ResourceKey<LootTable> itemsToDropWhenOminous = BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS;

		public TrialSpawnerConfig.Builder spawnRange(int i) {
			this.spawnRange = i;
			return this;
		}

		public TrialSpawnerConfig.Builder totalMobs(float f) {
			this.totalMobs = f;
			return this;
		}

		public TrialSpawnerConfig.Builder simultaneousMobs(float f) {
			this.simultaneousMobs = f;
			return this;
		}

		public TrialSpawnerConfig.Builder totalMobsAddedPerPlayer(float f) {
			this.totalMobsAddedPerPlayer = f;
			return this;
		}

		public TrialSpawnerConfig.Builder simultaneousMobsAddedPerPlayer(float f) {
			this.simultaneousMobsAddedPerPlayer = f;
			return this;
		}

		public TrialSpawnerConfig.Builder ticksBetweenSpawn(int i) {
			this.ticksBetweenSpawn = i;
			return this;
		}

		public TrialSpawnerConfig.Builder spawnPotentialsDefinition(SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList) {
			this.spawnPotentialsDefinition = simpleWeightedRandomList;
			return this;
		}

		public TrialSpawnerConfig.Builder lootTablesToEject(SimpleWeightedRandomList<ResourceKey<LootTable>> simpleWeightedRandomList) {
			this.lootTablesToEject = simpleWeightedRandomList;
			return this;
		}

		public TrialSpawnerConfig.Builder itemsToDropWhenOminous(ResourceKey<LootTable> resourceKey) {
			this.itemsToDropWhenOminous = resourceKey;
			return this;
		}

		public TrialSpawnerConfig build() {
			return new TrialSpawnerConfig(
				this.spawnRange,
				this.totalMobs,
				this.simultaneousMobs,
				this.totalMobsAddedPerPlayer,
				this.simultaneousMobsAddedPerPlayer,
				this.ticksBetweenSpawn,
				this.spawnPotentialsDefinition,
				this.lootTablesToEject,
				this.itemsToDropWhenOminous
			);
		}
	}
}
