/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MobSpawnSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1f;
    public static final WeightedRandomList<SpawnerData> EMPTY_MOB_LIST = WeightedRandomList.create();
    public static final MobSpawnSettings EMPTY = new Builder().build();
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.floatRange(0.0f, 0.9999999f).optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(mobSpawnSettings -> Float.valueOf(mobSpawnSettings.creatureGenerationProbability)), Codec.simpleMap(MobCategory.CODEC, WeightedRandomList.codec(SpawnerData.CODEC).promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(mobSpawnSettings -> mobSpawnSettings.spawners), Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(mobSpawnSettings -> mobSpawnSettings.mobSpawnCosts)).apply((Applicative<MobSpawnSettings, ?>)instance, MobSpawnSettings::new));
    private final float creatureGenerationProbability;
    private final Map<MobCategory, WeightedRandomList<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;

    MobSpawnSettings(float f, Map<MobCategory, WeightedRandomList<SpawnerData>> map, Map<EntityType<?>, MobSpawnCost> map2) {
        this.creatureGenerationProbability = f;
        this.spawners = ImmutableMap.copyOf(map);
        this.mobSpawnCosts = ImmutableMap.copyOf(map2);
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

    public record MobSpawnCost(double energyBudget, double charge) {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.DOUBLE.fieldOf("energy_budget")).forGetter(mobSpawnCost -> mobSpawnCost.energyBudget), ((MapCodec)Codec.DOUBLE.fieldOf("charge")).forGetter(mobSpawnCost -> mobSpawnCost.charge)).apply((Applicative<MobSpawnCost, ?>)instance, MobSpawnCost::new));
    }

    public static class SpawnerData
    extends WeightedEntry.IntrusiveBase {
        public static final Codec<SpawnerData> CODEC = ExtraCodecs.validate(RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type")).forGetter(spawnerData -> spawnerData.type), ((MapCodec)Weight.CODEC.fieldOf("weight")).forGetter(WeightedEntry.IntrusiveBase::getWeight), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("minCount")).forGetter(spawnerData -> spawnerData.minCount), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("maxCount")).forGetter(spawnerData -> spawnerData.maxCount)).apply((Applicative<SpawnerData, ?>)instance, SpawnerData::new)), spawnerData -> {
            if (spawnerData.minCount > spawnerData.maxCount) {
                return DataResult.error(() -> "minCount needs to be smaller or equal to maxCount");
            }
            return DataResult.success(spawnerData);
        });
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
            return new MobSpawnSettings(this.creatureGenerationProbability, (Map<MobCategory, WeightedRandomList<SpawnerData>>)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> WeightedRandomList.create((List)entry.getValue()))), ImmutableMap.copyOf(this.mobSpawnCosts));
        }
    }
}

