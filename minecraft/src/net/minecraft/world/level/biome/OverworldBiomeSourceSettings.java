package net.minecraft.world.level.biome;

import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public class OverworldBiomeSourceSettings implements BiomeSourceSettings {
	private final long seed;
	private LevelType generatorType = LevelType.NORMAL;
	private OverworldGeneratorSettings generatorSettings = new OverworldGeneratorSettings();

	public OverworldBiomeSourceSettings(long l) {
		this.seed = l;
	}

	public OverworldBiomeSourceSettings setLevelType(LevelType levelType) {
		this.generatorType = levelType;
		return this;
	}

	public OverworldBiomeSourceSettings setGeneratorSettings(OverworldGeneratorSettings overworldGeneratorSettings) {
		this.generatorSettings = overworldGeneratorSettings;
		return this;
	}

	public long getSeed() {
		return this.seed;
	}

	public LevelType getGeneratorType() {
		return this.generatorType;
	}

	public OverworldGeneratorSettings getGeneratorSettings() {
		return this.generatorSettings;
	}
}
