/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData(CompoundTag entityToSpawn, Optional<CustomSpawnRules> customSpawnRules) {
    public static final String ENTITY_TAG = "entity";
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)CompoundTag.CODEC.fieldOf(ENTITY_TAG)).forGetter(spawnData -> spawnData.entityToSpawn), CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(spawnData -> spawnData.customSpawnRules)).apply((Applicative<SpawnData, ?>)instance, SpawnData::new));
    public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

    public SpawnData() {
        this(new CompoundTag(), Optional.empty());
    }

    public SpawnData {
        if (compoundTag.contains("id")) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("id"));
            if (resourceLocation != null) {
                compoundTag.putString("id", resourceLocation.toString());
            } else {
                compoundTag.remove("id");
            }
        }
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(InclusiveRange.INT.optionalFieldOf("block_light_limit", LIGHT_RANGE).flatXmap(CustomSpawnRules::checkLightBoundaries, CustomSpawnRules::checkLightBoundaries).forGetter(customSpawnRules -> customSpawnRules.blockLightLimit), InclusiveRange.INT.optionalFieldOf("sky_light_limit", LIGHT_RANGE).flatXmap(CustomSpawnRules::checkLightBoundaries, CustomSpawnRules::checkLightBoundaries).forGetter(customSpawnRules -> customSpawnRules.skyLightLimit)).apply((Applicative<CustomSpawnRules, ?>)instance, CustomSpawnRules::new));

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiveRange) {
            if (!LIGHT_RANGE.contains(inclusiveRange)) {
                return DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE);
            }
            return DataResult.success(inclusiveRange);
        }
    }
}

