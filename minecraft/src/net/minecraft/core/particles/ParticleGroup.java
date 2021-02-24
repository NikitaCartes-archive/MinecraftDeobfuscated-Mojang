package net.minecraft.core.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ParticleGroup {
	private final int limit;
	public static final ParticleGroup SPORE_BLOSSOM = new ParticleGroup(1000);

	public ParticleGroup(int i) {
		this.limit = i;
	}

	public int getLimit() {
		return this.limit;
	}
}
