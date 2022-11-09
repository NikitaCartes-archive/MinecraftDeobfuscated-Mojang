/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public abstract class StructurePlacement {
    public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);
    private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;

    protected static <S extends StructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>> placementCodec(RecordCodecBuilder.Instance<S> instance) {
        return instance.group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StructurePlacement::locateOffset), FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter(StructurePlacement::frequencyReductionMethod), Codec.floatRange(0.0f, 1.0f).optionalFieldOf("frequency", Float.valueOf(1.0f)).forGetter(StructurePlacement::frequency), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt")).forGetter(StructurePlacement::salt), ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone));
    }

    protected StructurePlacement(Vec3i vec3i, FrequencyReductionMethod frequencyReductionMethod, float f, int i, Optional<ExclusionZone> optional) {
        this.locateOffset = vec3i;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = f;
        this.salt = i;
        this.exclusionZone = optional;
    }

    protected Vec3i locateOffset() {
        return this.locateOffset;
    }

    protected FrequencyReductionMethod frequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    protected float frequency() {
        return this.frequency;
    }

    protected int salt() {
        return this.salt;
    }

    protected Optional<ExclusionZone> exclusionZone() {
        return this.exclusionZone;
    }

    public boolean isStructureChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
        if (!this.isPlacementChunk(chunkGeneratorStructureState, i, j)) {
            return false;
        }
        if (this.frequency < 1.0f && !this.frequencyReductionMethod.shouldGenerate(chunkGeneratorStructureState.getLevelSeed(), this.salt, i, j, this.frequency)) {
            return false;
        }
        return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden(chunkGeneratorStructureState, i, j);
    }

    protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState var1, int var2, int var3);

    public BlockPos getLocatePos(ChunkPos chunkPos) {
        return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()).offset(this.locateOffset());
    }

    public abstract StructurePlacementType<?> type();

    private static boolean probabilityReducer(long l, int i, int j, int k, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, i, j, k);
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean legacyProbabilityReducerWithDouble(long l, int i, int j, int k, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(l, j, k);
        return worldgenRandom.nextDouble() < (double)f;
    }

    private static boolean legacyArbitrarySaltProbabilityReducer(long l, int i, int j, int k, float f) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, j, k, 10387320);
        return worldgenRandom.nextFloat() < f;
    }

    private static boolean legacyPillagerOutpostReducer(long l, int i, int j, int k, float f) {
        int m = j >> 4;
        int n = k >> 4;
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setSeed((long)(m ^ n << 4) ^ l);
        worldgenRandom.nextInt();
        return worldgenRandom.nextInt((int)(1.0f / f)) == 0;
    }

    public static enum FrequencyReductionMethod implements StringRepresentable
    {
        DEFAULT("default", StructurePlacement::probabilityReducer),
        LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
        LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
        LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

        public static final Codec<FrequencyReductionMethod> CODEC;
        private final String name;
        private final FrequencyReducer reducer;

        private FrequencyReductionMethod(String string2, FrequencyReducer frequencyReducer) {
            this.name = string2;
            this.reducer = frequencyReducer;
        }

        public boolean shouldGenerate(long l, int i, int j, int k, float f) {
            return this.reducer.shouldGenerate(l, i, j, k, f);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(FrequencyReductionMethod::values);
        }
    }

    @Deprecated
    public record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
        public static final Codec<ExclusionZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false).fieldOf("other_set")).forGetter(ExclusionZone::otherSet), ((MapCodec)Codec.intRange(1, 16).fieldOf("chunk_count")).forGetter(ExclusionZone::chunkCount)).apply((Applicative<ExclusionZone, ?>)instance, ExclusionZone::new));

        boolean isPlacementForbidden(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
            return chunkGeneratorStructureState.hasStructureChunkInRange(this.otherSet, i, j, this.chunkCount);
        }
    }

    @FunctionalInterface
    public static interface FrequencyReducer {
        public boolean shouldGenerate(long var1, int var3, int var4, int var5, float var6);
    }
}

