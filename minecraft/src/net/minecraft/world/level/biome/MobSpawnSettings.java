package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobSpawnSettings {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final MobSpawnSettings EMPTY = new MobSpawnSettings(
		0.1F,
		(Map<MobCategory, List<MobSpawnSettings.SpawnerData>>)Stream.of(MobCategory.values())
			.collect(ImmutableMap.toImmutableMap(mobCategory -> mobCategory, mobCategory -> ImmutableList.of())),
		ImmutableMap.of(),
		false
	);
	public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.FLOAT
						.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1F))
						.forGetter(mobSpawnSettings -> mobSpawnSettings.creatureGenerationProbability),
					Codec.simpleMap(
							MobCategory.CODEC,
							MobSpawnSettings.SpawnerData.CODEC.listOf().promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
							StringRepresentable.keys(MobCategory.values())
						)
						.fieldOf("spawners")
						.forGetter(mobSpawnSettings -> mobSpawnSettings.spawners),
					Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnSettings.MobSpawnCost.CODEC, Registry.ENTITY_TYPE)
						.fieldOf("spawn_costs")
						.forGetter(mobSpawnSettings -> mobSpawnSettings.mobSpawnCosts),
					Codec.BOOL.fieldOf("player_spawn_friendly").orElse(false).forGetter(MobSpawnSettings::playerSpawnFriendly)
				)
				.apply(instance, MobSpawnSettings::new)
	);
	private final float creatureGenerationProbability;
	private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners;
	private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;
	private final boolean playerSpawnFriendly;

	private MobSpawnSettings(float f, Map<MobCategory, List<MobSpawnSettings.SpawnerData>> map, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> map2, boolean bl) {
		this.creatureGenerationProbability = f;
		this.spawners = map;
		this.mobSpawnCosts = map2;
		this.playerSpawnFriendly = bl;
	}

	public List<MobSpawnSettings.SpawnerData> getMobs(MobCategory mobCategory) {
		return (List<MobSpawnSettings.SpawnerData>)this.spawners.get(mobCategory);
	}

	@Nullable
	public MobSpawnSettings.MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
		return (MobSpawnSettings.MobSpawnCost)this.mobSpawnCosts.get(entityType);
	}

	public float getCreatureProbability() {
		return this.creatureGenerationProbability;
	}

	public boolean playerSpawnFriendly() {
		return this.playerSpawnFriendly;
	}

	public static class Builder {
		private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = (Map<MobCategory, List<MobSpawnSettings.SpawnerData>>)Stream.of(
				MobCategory.values()
			)
			.collect(ImmutableMap.toImmutableMap(mobCategory -> mobCategory, mobCategory -> Lists.newArrayList()));
		private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.<EntityType<?>, MobSpawnSettings.MobSpawnCost>newLinkedHashMap();
		private float creatureGenerationProbability = 0.1F;
		private boolean playerCanSpawn;

		public MobSpawnSettings.Builder addSpawn(MobCategory mobCategory, MobSpawnSettings.SpawnerData spawnerData) {
			((List)this.spawners.get(mobCategory)).add(spawnerData);
			return this;
		}

		public MobSpawnSettings.Builder addMobCharge(EntityType<?> entityType, double d, double e) {
			this.mobSpawnCosts.put(entityType, new MobSpawnSettings.MobSpawnCost(e, d));
			return this;
		}

		public MobSpawnSettings.Builder creatureGenerationProbability(float f) {
			this.creatureGenerationProbability = f;
			return this;
		}

		public MobSpawnSettings.Builder setPlayerCanSpawn() {
			this.playerCanSpawn = true;
			return this;
		}

		public MobSpawnSettings build() {
			return new MobSpawnSettings(
				this.creatureGenerationProbability,
				(Map)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ImmutableList.copyOf((Collection)entry.getValue()))),
				ImmutableMap.copyOf(this.mobSpawnCosts),
				this.playerCanSpawn
			);
		}
	}

	public static class MobSpawnCost {
		public static final Codec<MobSpawnSettings.MobSpawnCost> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.DOUBLE.fieldOf("energy_budget").forGetter(mobSpawnCost -> mobSpawnCost.energyBudget),
						Codec.DOUBLE.fieldOf("charge").forGetter(mobSpawnCost -> mobSpawnCost.charge)
					)
					.apply(instance, MobSpawnSettings.MobSpawnCost::new)
		);
		private final double energyBudget;
		private final double charge;

		private MobSpawnCost(double d, double e) {
			this.energyBudget = d;
			this.charge = e;
		}

		public double getEnergyBudget() {
			return this.energyBudget;
		}

		public double getCharge() {
			return this.charge;
		}
	}

	public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
		public static final Codec<MobSpawnSettings.SpawnerData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Registry.ENTITY_TYPE.fieldOf("type").forGetter(spawnerData -> spawnerData.type),
						Codec.INT.fieldOf("weight").forGetter(spawnerData -> spawnerData.weight),
						Codec.INT.fieldOf("minCount").forGetter(spawnerData -> spawnerData.minCount),
						Codec.INT.fieldOf("maxCount").forGetter(spawnerData -> spawnerData.maxCount)
					)
					.apply(instance, MobSpawnSettings.SpawnerData::new)
		);
		public final EntityType<?> type;
		public final int minCount;
		public final int maxCount;

		public SpawnerData(EntityType<?> entityType, int i, int j, int k) {
			super(i);
			this.type = entityType.getCategory() == MobCategory.MISC ? EntityType.PIG : entityType;
			this.minCount = j;
			this.maxCount = k;
		}

		public String toString() {
			return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
		}
	}
}
