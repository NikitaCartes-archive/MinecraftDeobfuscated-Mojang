package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ChunkGeneratorSettings {
	protected int villagesSpacing = 32;
	protected final int villagesSeparation = 8;
	protected int monumentsSpacing = 32;
	protected int monumentsSeparation = 5;
	protected int strongholdsDistance = 32;
	protected int strongholdsCount = 128;
	protected int strongholdsSpread = 3;
	protected int templesSpacing = 32;
	protected final int templesSeparation = 8;
	protected final int oceanRuinSpacing = 20;
	protected final int oceanRuinSeparation = 8;
	protected int endCitySpacing = 20;
	protected final int endCitySeparation = 11;
	protected final int shipwreckSpacing = 24;
	protected final int shipwreckSeparation = 4;
	protected int woodlandMansionSpacing = 80;
	protected final int woodlandMansionSeparation = 20;
	protected final int rareNetherStructureSpacing = 24;
	protected final int rareNetherStructureSeparation = 4;
	protected final int rareNetherStructureSalt = 30084232;
	protected int ruinedPortalOverworldSpacing = 25;
	protected final int ruinedPortalOverworldSeparation = 10;
	protected int ruinedPortalNetherSpacing = 15;
	protected final int ruinedPortalNetherSeparation = 7;
	protected BlockState defaultBlock = Blocks.STONE.defaultBlockState();
	protected BlockState defaultFluid = Blocks.WATER.defaultBlockState();

	public int getRareNetherStructureSpacing() {
		return 24;
	}

	public int getRareNetherStructureSeparation() {
		return 4;
	}

	public int getRareNetherStructureSalt() {
		return 30084232;
	}

	public int getVillagesSpacing() {
		return this.villagesSpacing;
	}

	public int getVillagesSeparation() {
		return 8;
	}

	public int getMonumentsSpacing() {
		return this.monumentsSpacing;
	}

	public int getMonumentsSeparation() {
		return this.monumentsSeparation;
	}

	public int getStrongholdsDistance() {
		return this.strongholdsDistance;
	}

	public int getStrongholdsCount() {
		return this.strongholdsCount;
	}

	public int getStrongholdsSpread() {
		return this.strongholdsSpread;
	}

	public int getTemplesSpacing() {
		return this.templesSpacing;
	}

	public int getTemplesSeparation() {
		return 8;
	}

	public int getShipwreckSpacing() {
		return 24;
	}

	public int getShipwreckSeparation() {
		return 4;
	}

	public int getOceanRuinSpacing() {
		return 20;
	}

	public int getOceanRuinSeparation() {
		return 8;
	}

	public int getEndCitySpacing() {
		return this.endCitySpacing;
	}

	public int getEndCitySeparation() {
		return 11;
	}

	public int getWoodlandMansionSpacing() {
		return this.woodlandMansionSpacing;
	}

	public int getWoodlandMansionSeparation() {
		return 20;
	}

	public int getRuinedPortalSpacing(boolean bl) {
		return bl ? this.ruinedPortalNetherSpacing : this.ruinedPortalOverworldSpacing;
	}

	public int getRuinedPortalSeparation(boolean bl) {
		return bl ? 7 : 10;
	}

	public BlockState getDefaultBlock() {
		return this.defaultBlock;
	}

	public BlockState getDefaultFluid() {
		return this.defaultFluid;
	}

	public void setDefaultBlock(BlockState blockState) {
		this.defaultBlock = blockState;
	}

	public void setDefaultFluid(BlockState blockState) {
		this.defaultFluid = blockState;
	}

	public int getBedrockRoofPosition() {
		return 0;
	}

	public int getBedrockFloorPosition() {
		return 256;
	}
}
