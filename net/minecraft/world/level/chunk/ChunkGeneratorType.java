/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorFactory;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugGeneratorSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.level.levelgen.NetherLevelSource;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.levelgen.TheEndLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class ChunkGeneratorType<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>>
implements ChunkGeneratorFactory<C, T> {
    public static final ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> SURFACE = ChunkGeneratorType.register("surface", OverworldLevelSource::new, OverworldGeneratorSettings::new, true);
    public static final ChunkGeneratorType<NetherGeneratorSettings, NetherLevelSource> CAVES = ChunkGeneratorType.register("caves", NetherLevelSource::new, NetherGeneratorSettings::new, true);
    public static final ChunkGeneratorType<TheEndGeneratorSettings, TheEndLevelSource> FLOATING_ISLANDS = ChunkGeneratorType.register("floating_islands", TheEndLevelSource::new, TheEndGeneratorSettings::new, true);
    public static final ChunkGeneratorType<DebugGeneratorSettings, DebugLevelSource> DEBUG = ChunkGeneratorType.register("debug", DebugLevelSource::new, DebugGeneratorSettings::new, false);
    public static final ChunkGeneratorType<FlatLevelGeneratorSettings, FlatLevelSource> FLAT = ChunkGeneratorType.register("flat", FlatLevelSource::new, FlatLevelGeneratorSettings::new, false);
    private final ChunkGeneratorFactory<C, T> factory;
    private final boolean isPublic;
    private final Supplier<C> settingsFactory;

    private static <C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> ChunkGeneratorType<C, T> register(String string, ChunkGeneratorFactory<C, T> chunkGeneratorFactory, Supplier<C> supplier, boolean bl) {
        return Registry.register(Registry.CHUNK_GENERATOR_TYPE, string, new ChunkGeneratorType<C, T>(chunkGeneratorFactory, bl, supplier));
    }

    public ChunkGeneratorType(ChunkGeneratorFactory<C, T> chunkGeneratorFactory, boolean bl, Supplier<C> supplier) {
        this.factory = chunkGeneratorFactory;
        this.isPublic = bl;
        this.settingsFactory = supplier;
    }

    @Override
    public T create(Level level, BiomeSource biomeSource, C chunkGeneratorSettings) {
        return this.factory.create(level, biomeSource, chunkGeneratorSettings);
    }

    public C createSettings() {
        return (C)((ChunkGeneratorSettings)this.settingsFactory.get());
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isPublic() {
        return this.isPublic;
    }
}

