/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class MobSpawnSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1f;
    public static final WeightedRandomList<SpawnerData> EMPTY_MOB_LIST = WeightedRandomList.create();
    public static final MobSpawnSettings EMPTY = new Builder().build();
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.floatRange(0.0f, 0.9999999f).optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(mobSpawnSettings -> Float.valueOf(mobSpawnSettings.creatureGenerationProbability)), Codec.simpleMap(MobCategory.CODEC, WeightedRandomList.codec(SpawnerData.CODEC).promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(mobSpawnSettings -> mobSpawnSettings.spawners), Codec.simpleMap(Registry.ENTITY_TYPE.byNameCodec(), MobSpawnCost.CODEC, Registry.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(mobSpawnSettings -> mobSpawnSettings.mobSpawnCosts), ((MapCodec)Codec.BOOL.fieldOf("player_spawn_friendly")).orElse(false).forGetter(MobSpawnSettings::playerSpawnFriendly)).apply((Applicative<MobSpawnSettings, ?>)instance, MobSpawnSettings::new));
    private final float creatureGenerationProbability;
    private final Map<MobCategory, WeightedRandomList<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;
    private final boolean playerSpawnFriendly;

    MobSpawnSettings(float f, Map<MobCategory, WeightedRandomList<SpawnerData>> map, Map<EntityType<?>, MobSpawnCost> map2, boolean bl) {
        this.creatureGenerationProbability = f;
        this.spawners = ImmutableMap.copyOf(map);
        this.mobSpawnCosts = ImmutableMap.copyOf(map2);
        this.playerSpawnFriendly = bl;
    }

    public WeightedRandomList<SpawnerData> getMobs(MobCategory mobCategory) {
        return this.spawners.getOrDefault(mobCategory, EMPTY_MOB_LIST);
    }

    @Nullable
    public MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
        return this.mobSpawnCosts.get(entityType);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public boolean playerSpawnFriendly() {
        return this.playerSpawnFriendly;
    }

    public static class MobSpawnCost {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("energy_budget")).forGetter(mobSpawnCost -> mobSpawnCost.energyBudget), ((MapCodec)Codec.DOUBLE.fieldOf("charge")).forGetter(mobSpawnCost -> mobSpawnCost.charge)).apply((Applicative<MobSpawnCost, ?>)instance, MobSpawnCost::new));
        private final double energyBudget;
        private final double charge;

        MobSpawnCost(double d, double e) {
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

    public static class SpawnerData
    extends WeightedEntry.IntrusiveBase {
        public static final Codec<SpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.ENTITY_TYPE.byNameCodec().fieldOf("type")).forGetter(spawnerData -> spawnerData.type), ((MapCodec)Weight.CODEC.fieldOf("weight")).forGetter(WeightedEntry.IntrusiveBase::getWeight), ((MapCodec)Codec.INT.fieldOf("minCount")).forGetter(spawnerData -> spawnerData.minCount), ((MapCodec)Codec.INT.fieldOf("maxCount")).forGetter(spawnerData -> spawnerData.maxCount)).apply((Applicative<SpawnerData, ?>)instance, SpawnerData::new));
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> entityType, int i, int j, int k) {
            this(entityType, Weight.of(i), j, k);
        }

        public SpawnerData(EntityType<?> entityType, Weight weight, int i, int j) {
            super(weight);
            this.type = entityType.getCategory() == MobCategory.MISC ? EntityType.PIG : entityType;
            this.minCount = i;
            this.maxCount = j;
        }

        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.getWeight();
        }
    }

    public static class Builder {
        private final Map<MobCategory, List<SpawnerData>> spawners = Stream.of(MobCategory.values()).collect(ImmutableMap.toImmutableMap(mobCategory -> mobCategory, mobCategory -> Lists.newArrayList()));
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1f;
        private boolean playerCanSpawn;

        public Builder addSpawn(MobCategory mobCategory, SpawnerData spawnerData) {
            this.spawners.get(mobCategory).add(spawnerData);
            return this;
        }

        public Builder addMobCharge(EntityType<?> entityType, double d, double e) {
            this.mobSpawnCosts.put(entityType, new MobSpawnCost(e, d));
            return this;
        }

        public Builder creatureGenerationProbability(float f) {
            this.creatureGenerationProbability = f;
            return this;
        }

        public Builder setPlayerCanSpawn() {
            this.playerCanSpawn = true;
            return this;
        }

        public MobSpawnSettings build() {
            return new MobSpawnSettings(this.creatureGenerationProbability, (Map<MobCategory, WeightedRandomList<SpawnerData>>)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> WeightedRandomList.create((List)entry.getValue()))), ImmutableMap.copyOf(this.mobSpawnCosts), this.playerCanSpawn);
        }
    }
}

