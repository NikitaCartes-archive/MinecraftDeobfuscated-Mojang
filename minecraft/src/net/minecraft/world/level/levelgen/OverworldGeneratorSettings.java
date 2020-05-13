package net.minecraft.world.level.levelgen;

public class OverworldGeneratorSettings extends NoiseGeneratorSettings {
	private final boolean isAmplified;

	public OverworldGeneratorSettings() {
		this(new ChunkGeneratorSettings(), false);
	}

	public OverworldGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings, boolean bl) {
		super(chunkGeneratorSettings);
		this.isAmplified = bl;
	}

	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}

	public boolean isAmplified() {
		return this.isAmplified;
	}
}
