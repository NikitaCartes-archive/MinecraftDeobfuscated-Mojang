/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public record ConfiguredWorldCarver<WC extends CarverConfiguration>(WorldCarver<WC> worldCarver, WC config) {
    public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER.byNameCodec().dispatch(configuredWorldCarver -> configuredWorldCarver.worldCarver, WorldCarver::configuredCodec);
    public static final Codec<Holder<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
    public static final Codec<HolderSet<ConfiguredWorldCarver<?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);

    public boolean isStartChunk(Random random) {
        return this.worldCarver.isStartChunk(this.config, random);
    }

    public boolean carve(CarvingContext carvingContext, ChunkAccess chunkAccess, Function<BlockPos, Holder<Biome>> function, Random random, Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {
        if (SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            return false;
        }
        return this.worldCarver.carve(carvingContext, this.config, chunkAccess, function, random, aquifer, chunkPos, carvingMask);
    }
}

