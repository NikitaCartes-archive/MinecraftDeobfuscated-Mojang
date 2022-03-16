/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ConcentricRingsStructurePlacement
extends StructurePlacement {
    public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create(instance -> ConcentricRingsStructurePlacement.codec(instance).apply((Applicative<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, ?>)instance, ConcentricRingsStructurePlacement::new));
    private final int distance;
    private final int spread;
    private final int count;
    private final HolderSet<Biome> preferredBiomes;

    private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet<Biome>> codec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> instance) {
        Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> p5 = ConcentricRingsStructurePlacement.placementCodec(instance);
        Products.P4<ConcentricRingsStructurePlacement, Integer, Integer, Integer, HolderSet> p4 = instance.group(((MapCodec)Codec.intRange(0, 1023).fieldOf("distance")).forGetter(ConcentricRingsStructurePlacement::distance), ((MapCodec)Codec.intRange(0, 1023).fieldOf("spread")).forGetter(ConcentricRingsStructurePlacement::spread), ((MapCodec)Codec.intRange(1, 4095).fieldOf("count")).forGetter(ConcentricRingsStructurePlacement::count), ((MapCodec)RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY).fieldOf("preferred_biomes")).forGetter(ConcentricRingsStructurePlacement::preferredBiomes));
        return new Products.P9<ConcentricRingsStructurePlacement, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet>(p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5(), p4.t1(), p4.t2(), p4.t3(), p4.t4());
    }

    public ConcentricRingsStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional, int j, int k, int l, HolderSet<Biome> holderSet) {
        super(vec3i, frequencyReductionMethod, f, i, optional);
        this.distance = j;
        this.spread = k;
        this.count = l;
        this.preferredBiomes = holderSet;
    }

    public ConcentricRingsStructurePlacement(int i, int j, int k, HolderSet<Biome> holderSet) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, 0, Optional.empty(), i, j, k, holderSet);
    }

    public int distance() {
        return this.distance;
    }

    public int spread() {
        return this.spread;
    }

    public int count() {
        return this.count;
    }

    public HolderSet<Biome> preferredBiomes() {
        return this.preferredBiomes;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGenerator chunkGenerator, RandomState randomState, long l, int i, int j) {
        List<ChunkPos> list = chunkGenerator.getRingPositionsFor(this, randomState);
        if (list == null) {
            return false;
        }
        return list.contains(new ChunkPos(i, j));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}

