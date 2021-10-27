package net.minecraft.world.level.levelgen.blending;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public final class GenerationUpgradeData {
	private final boolean oldBiome;
	private final boolean oldNoise;

	public GenerationUpgradeData(boolean bl, boolean bl2) {
		this.oldBiome = bl;
		this.oldNoise = bl2;
	}

	@Nullable
	public static GenerationUpgradeData read(CompoundTag compoundTag) {
		return compoundTag.isEmpty() ? null : new GenerationUpgradeData(compoundTag.getBoolean("old_biome"), compoundTag.getBoolean("old_noise"));
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putBoolean("old_biome", this.oldBiome);
		compoundTag.putBoolean("old_noise", this.oldNoise);
		return compoundTag;
	}

	public boolean oldBiome() {
		return this.oldBiome;
	}

	public boolean oldNoise() {
		return this.oldNoise;
	}
}
