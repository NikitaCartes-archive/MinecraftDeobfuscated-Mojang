package net.minecraft.world.level.levelgen;

public record TerrainInfo() {
	private final double offset;
	private final double factor;
	private final double peaks;

	public TerrainInfo(double d, double e, double f) {
		this.offset = d;
		this.factor = e;
		this.peaks = f;
	}
}
