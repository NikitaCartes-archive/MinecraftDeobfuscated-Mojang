package net.minecraft.world.level.levelgen;

public record RandomWithLegacy(PositionalRandomFactory random, boolean useLegacyInit, long legacyLevelSeed) {
	public RandomSource newLegacyInstance(long l) {
		return new LegacyRandomSource(this.legacyLevelSeed + l);
	}
}
