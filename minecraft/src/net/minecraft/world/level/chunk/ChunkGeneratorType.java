package net.minecraft.world.level.chunk;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.special.G01;
import net.minecraft.world.level.dimension.special.G02;
import net.minecraft.world.level.dimension.special.G03;
import net.minecraft.world.level.dimension.special.G04;
import net.minecraft.world.level.dimension.special.G05;
import net.minecraft.world.level.dimension.special.G06;
import net.minecraft.world.level.dimension.special.G07;
import net.minecraft.world.level.dimension.special.G12;
import net.minecraft.world.level.dimension.special.G13;
import net.minecraft.world.level.dimension.special.G15;
import net.minecraft.world.level.dimension.special.G16;
import net.minecraft.world.level.dimension.special.G17;
import net.minecraft.world.level.dimension.special.G18;
import net.minecraft.world.level.dimension.special.G19;
import net.minecraft.world.level.dimension.special.G20;
import net.minecraft.world.level.dimension.special.G21;
import net.minecraft.world.level.dimension.special.G22;
import net.minecraft.world.level.dimension.special.G23;
import net.minecraft.world.level.dimension.special.G24;
import net.minecraft.world.level.dimension.special.G25;
import net.minecraft.world.level.dimension.special.G26;
import net.minecraft.world.level.dimension.special.G27;
import net.minecraft.world.level.dimension.special.G28;
import net.minecraft.world.level.dimension.special.G29;
import net.minecraft.world.level.dimension.special.G30;
import net.minecraft.world.level.dimension.special.G31;
import net.minecraft.world.level.dimension.special.G32;
import net.minecraft.world.level.dimension.special.G34;
import net.minecraft.world.level.dimension.special.G35;
import net.minecraft.world.level.dimension.special.G36;
import net.minecraft.world.level.dimension.special.G37;
import net.minecraft.world.level.dimension.special.G38;
import net.minecraft.world.level.dimension.special.G39;
import net.minecraft.world.level.dimension.special.G40;
import net.minecraft.world.level.dimension.special.LastPage;
import net.minecraft.world.level.dimension.special.NoneGeneratorSettings;
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

public class ChunkGeneratorType<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> implements ChunkGeneratorFactory<C, T> {
	public static final ChunkGeneratorType<OverworldGeneratorSettings, OverworldLevelSource> SURFACE = register(
		"surface", OverworldLevelSource::new, OverworldGeneratorSettings::new, true
	);
	public static final ChunkGeneratorType<NetherGeneratorSettings, NetherLevelSource> CAVES = register(
		"caves", NetherLevelSource::new, NetherGeneratorSettings::new, true
	);
	public static final ChunkGeneratorType<TheEndGeneratorSettings, TheEndLevelSource> FLOATING_ISLANDS = register(
		"floating_islands", TheEndLevelSource::new, TheEndGeneratorSettings::new, true
	);
	public static final ChunkGeneratorType<DebugGeneratorSettings, DebugLevelSource> DEBUG = register(
		"debug", DebugLevelSource::new, DebugGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<FlatLevelGeneratorSettings, FlatLevelSource> FLAT = register(
		"flat", FlatLevelSource::new, FlatLevelGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G01.Generator> T01 = register("_001", G01.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G02.Generator> T02 = register("_002", G02.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G03.Generator> T03 = register("_003", G03.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G04.Generator> T04 = register("_004", G04.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G05.Generator> T05 = register("_005", G05.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G06.Generator> T06 = register("_006", G06.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G07.Generator> T07 = register("_007", G07.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G12.Generator> T08 = register("_008", G12.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G13.Generator> T09 = register("_009", G13.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G15.Generator> T10 = register("_010", G15.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G16.Generator> T11 = register("_011", G16.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G17.Generator> T12 = register("_012", G17.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G18.Generator> T13 = register("_013", G18.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G19.Generator> T14 = register("_014", G19.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G20.Generator> T15 = register("_015", G20.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G21.Generator> T16 = register("_016", G21.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G22.Generator> T17 = register("_017", G22.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G23.Generator> T18 = register("_018", G23.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G24.Generator> T19 = register("_019", G24.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G25.Generator> T20 = register(
		"_020", G25.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G26.Generator> T21 = register("_021", G26.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G27.Generator> T22 = register("_022", G27.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G28.Generator> T23 = register("_023", G28.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G29.Generator> T24 = register("_024", G29.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G30.Generator> T25 = register(
		"_025", G30.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G31.Generator> T26 = register("_026", G31.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, LastPage.Generator> T27 = register(
		"_027", LastPage.Generator::new, NoneGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G32.Generator> T28 = register("_028", G32.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G34.Generator> T29 = register("_029", G34.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G35.Generator> T30 = register(
		"_030", G35.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<NoneGeneratorSettings, G36.Generator> T31 = register("_031", G36.Generator::new, NoneGeneratorSettings::new, false);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G37.Generator> T32 = register(
		"_032", G37.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G38.Generator> T33 = register(
		"_033", G38.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G39.Generator> T34 = register(
		"_034", G39.Generator::new, OverworldGeneratorSettings::new, false
	);
	public static final ChunkGeneratorType<OverworldGeneratorSettings, G40.Generator> T35 = register(
		"_035", G40.Generator::new, OverworldGeneratorSettings::new, false
	);
	private final ChunkGeneratorFactory<C, T> factory;
	private final boolean isPublic;
	private final Supplier<C> settingsFactory;

	private static <C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> ChunkGeneratorType<C, T> register(
		String string, ChunkGeneratorFactory<C, T> chunkGeneratorFactory, Supplier<C> supplier, boolean bl
	) {
		return Registry.register(Registry.CHUNK_GENERATOR_TYPE, string, new ChunkGeneratorType<>(chunkGeneratorFactory, bl, supplier));
	}

	public ChunkGeneratorType(ChunkGeneratorFactory<C, T> chunkGeneratorFactory, boolean bl, Supplier<C> supplier) {
		this.factory = chunkGeneratorFactory;
		this.isPublic = bl;
		this.settingsFactory = supplier;
	}

	@Override
	public T create(LevelAccessor levelAccessor, BiomeSource biomeSource, C chunkGeneratorSettings) {
		return this.factory.create(levelAccessor, biomeSource, chunkGeneratorSettings);
	}

	public C createSettings() {
		return (C)this.settingsFactory.get();
	}

	@Environment(EnvType.CLIENT)
	public boolean isPublic() {
		return this.isPublic;
	}
}
