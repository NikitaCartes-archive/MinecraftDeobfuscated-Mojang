/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class MobSpawnSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final MobSpawnSettings EMPTY = new MobSpawnSettings(0.1f, (Map<MobCategory, List<SpawnerData>>)Stream.of(MobCategory.values()).collect(ImmutableMap.toImmutableMap(mobCategory -> mobCategory, mobCategory -> ImmutableList.of())), ImmutableMap.of());
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.FLOAT.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(mobSpawnSettings -> Float.valueOf(mobSpawnSettings.creatureGenerationProbability)), Codec.simpleMap(MobCategory.CODEC, SpawnerData.CODEC.listOf().promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(mobSpawnSettings -> mobSpawnSettings.spawners), Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnCost.CODEC, Registry.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(mobSpawnSettings -> mobSpawnSettings.mobSpawnCosts)).apply((Applicative<MobSpawnSettings, ?>)instance, MobSpawnSettings::new));
    private final float creatureGenerationProbability;
    private final Map<MobCategory, List<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;

    private MobSpawnSettings(float f, Map<MobCategory, List<SpawnerData>> map, Map<EntityType<?>, MobSpawnCost> map2) {
        this.creatureGenerationProbability = f;
        this.spawners = map;
        this.mobSpawnCosts = map2;
    }

    public List<SpawnerData> getMobs(MobCategory mobCategory) {
        return this.spawners.get(mobCategory);
    }

    @Nullable
    public MobSpawnCost getMobSpawnCost(EntityType<?> entityType) {
        return this.mobSpawnCosts.get(entityType);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public static class Builder {
        private final Map<MobCategory, List<SpawnerData>> spawners = Stream.of(MobCategory.values()).collect(ImmutableMap.toImmutableMap(mobCategory -> mobCategory, mobCategory -> Lists.newArrayList()));
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1f;

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

        public MobSpawnSettings build() {
            return new MobSpawnSettings(this.creatureGenerationProbability, this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> ImmutableList.copyOf((Collection)entry.getValue()))), ImmutableMap.copyOf(this.mobSpawnCosts));
        }
    }

    public static class MobSpawnCost {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("energy_budget")).forGetter(mobSpawnCost -> mobSpawnCost.energyBudget), ((MapCodec)Codec.DOUBLE.fieldOf("charge")).forGetter(mobSpawnCost -> mobSpawnCost.charge)).apply((Applicative<MobSpawnCost, ?>)instance, MobSpawnCost::new));
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

    public static class SpawnerData
    extends WeighedRandom.WeighedRandomItem {
        public static final Codec<SpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.ENTITY_TYPE.fieldOf("type")).forGetter(spawnerData -> spawnerData.type), ((MapCodec)Codec.INT.fieldOf("weight")).forGetter(spawnerData -> spawnerData.weight), ((MapCodec)Codec.INT.fieldOf("minCount")).forGetter(spawnerData -> spawnerData.minCount), ((MapCodec)Codec.INT.fieldOf("maxCount")).forGetter(spawnerData -> spawnerData.maxCount)).apply((Applicative<SpawnerData, ?>)instance, SpawnerData::new));
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

