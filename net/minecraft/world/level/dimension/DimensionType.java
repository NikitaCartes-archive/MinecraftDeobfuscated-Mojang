/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

public record DimensionType(OptionalLong fixedTime, boolean hasSkyLight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean bedWorks, boolean respawnAnchorWorks, int minY, int height, int logicalHeight, TagKey<Block> infiniburn, ResourceLocation effectsLocation, float ambientLight, MonsterSettings monsterSettings) {
    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final int WAY_ABOVE_MAX_Y = MAX_Y << 4;
    public static final int WAY_BELOW_MIN_Y = MIN_Y << 4;
    public static final Codec<DimensionType> DIRECT_CODEC = ExtraCodecs.catchDecoderException(RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.asOptionalLong(Codec.LONG.optionalFieldOf("fixed_time")).forGetter(DimensionType::fixedTime), ((MapCodec)Codec.BOOL.fieldOf("has_skylight")).forGetter(DimensionType::hasSkyLight), ((MapCodec)Codec.BOOL.fieldOf("has_ceiling")).forGetter(DimensionType::hasCeiling), ((MapCodec)Codec.BOOL.fieldOf("ultrawarm")).forGetter(DimensionType::ultraWarm), ((MapCodec)Codec.BOOL.fieldOf("natural")).forGetter(DimensionType::natural), ((MapCodec)Codec.doubleRange(1.0E-5f, 3.0E7).fieldOf("coordinate_scale")).forGetter(DimensionType::coordinateScale), ((MapCodec)Codec.BOOL.fieldOf("bed_works")).forGetter(DimensionType::bedWorks), ((MapCodec)Codec.BOOL.fieldOf("respawn_anchor_works")).forGetter(DimensionType::respawnAnchorWorks), ((MapCodec)Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y")).forGetter(DimensionType::minY), ((MapCodec)Codec.intRange(16, Y_SIZE).fieldOf("height")).forGetter(DimensionType::height), ((MapCodec)Codec.intRange(0, Y_SIZE).fieldOf("logical_height")).forGetter(DimensionType::logicalHeight), ((MapCodec)TagKey.hashedCodec(Registries.BLOCK).fieldOf("infiniburn")).forGetter(DimensionType::infiniburn), ((MapCodec)ResourceLocation.CODEC.fieldOf("effects")).orElse(BuiltinDimensionTypes.OVERWORLD_EFFECTS).forGetter(DimensionType::effectsLocation), ((MapCodec)Codec.FLOAT.fieldOf("ambient_light")).forGetter(DimensionType::ambientLight), MonsterSettings.CODEC.forGetter(DimensionType::monsterSettings)).apply((Applicative<DimensionType, ?>)instance, DimensionType::new)));
    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final Codec<Holder<DimensionType>> CODEC = RegistryFileCodec.create(Registries.DIMENSION_TYPE, DIRECT_CODEC);

    public DimensionType {
        if (j < 16) {
            throw new IllegalStateException("height has to be at least 16");
        }
        if (i + j > MAX_Y + 1) {
            throw new IllegalStateException("min_y + height cannot be higher than: " + (MAX_Y + 1));
        }
        if (k > j) {
            throw new IllegalStateException("logical_height cannot be higher than height");
        }
        if (j % 16 != 0) {
            throw new IllegalStateException("height has to be multiple of 16");
        }
        if (i % 16 != 0) {
            throw new IllegalStateException("min_y has to be a multiple of 16");
        }
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> dynamic) {
        Optional<Number> optional = dynamic.asNumber().result();
        if (optional.isPresent()) {
            int i = optional.get().intValue();
            if (i == -1) {
                return DataResult.success(Level.NETHER);
            }
            if (i == 0) {
                return DataResult.success(Level.OVERWORLD);
            }
            if (i == 1) {
                return DataResult.success(Level.END);
            }
        }
        return Level.RESOURCE_KEY_CODEC.parse(dynamic);
    }

    public static double getTeleportationScale(DimensionType dimensionType, DimensionType dimensionType2) {
        double d = dimensionType.coordinateScale();
        double e = dimensionType2.coordinateScale();
        return d / e;
    }

    public static Path getStorageFolder(ResourceKey<Level> resourceKey, Path path) {
        if (resourceKey == Level.OVERWORLD) {
            return path;
        }
        if (resourceKey == Level.END) {
            return path.resolve("DIM1");
        }
        if (resourceKey == Level.NETHER) {
            return path.resolve("DIM-1");
        }
        return path.resolve("dimensions").resolve(resourceKey.location().getNamespace()).resolve(resourceKey.location().getPath());
    }

    public boolean hasFixedTime() {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long l) {
        double d = Mth.frac((double)this.fixedTime.orElse(l) / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float)(d * 2.0 + e) / 3.0f;
    }

    public int moonPhase(long l) {
        return (int)(l / 24000L % 8L + 8L) % 8;
    }

    public boolean piglinSafe() {
        return this.monsterSettings.piglinSafe();
    }

    public boolean hasRaids() {
        return this.monsterSettings.hasRaids();
    }

    public IntProvider monsterSpawnLightTest() {
        return this.monsterSettings.monsterSpawnLightTest();
    }

    public int monsterSpawnBlockLightLimit() {
        return this.monsterSettings.monsterSpawnBlockLightLimit();
    }

    public record MonsterSettings(boolean piglinSafe, boolean hasRaids, IntProvider monsterSpawnLightTest, int monsterSpawnBlockLightLimit) {
        public static final MapCodec<MonsterSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("piglin_safe")).forGetter(MonsterSettings::piglinSafe), ((MapCodec)Codec.BOOL.fieldOf("has_raids")).forGetter(MonsterSettings::hasRaids), ((MapCodec)IntProvider.codec(0, 15).fieldOf("monster_spawn_light_level")).forGetter(MonsterSettings::monsterSpawnLightTest), ((MapCodec)Codec.intRange(0, 15).fieldOf("monster_spawn_block_light_limit")).forGetter(MonsterSettings::monsterSpawnBlockLightLimit)).apply((Applicative<MonsterSettings, ?>)instance, MonsterSettings::new));
    }
}

