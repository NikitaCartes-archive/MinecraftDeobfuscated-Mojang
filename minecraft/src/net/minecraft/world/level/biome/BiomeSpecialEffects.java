package net.minecraft.world.level.biome;

import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BiomeSpecialEffects {
	private final int fogColor;
	private final int waterColor;
	private final int waterFogColor;
	private final Optional<AmbientParticleSettings> ambientParticleSettings;

	private BiomeSpecialEffects(int i, int j, int k, Optional<AmbientParticleSettings> optional) {
		this.fogColor = i;
		this.waterColor = j;
		this.waterFogColor = k;
		this.ambientParticleSettings = optional;
	}

	@Environment(EnvType.CLIENT)
	public int getFogColor() {
		return this.fogColor;
	}

	@Environment(EnvType.CLIENT)
	public int getWaterColor() {
		return this.waterColor;
	}

	@Environment(EnvType.CLIENT)
	public int getWaterFogColor() {
		return this.waterFogColor;
	}

	@Environment(EnvType.CLIENT)
	public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
		return this.ambientParticleSettings;
	}

	public static class Builder {
		private OptionalInt fogColor = OptionalInt.empty();
		private OptionalInt waterColor = OptionalInt.empty();
		private OptionalInt waterFogColor = OptionalInt.empty();
		private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();

		public BiomeSpecialEffects.Builder fogColor(int i) {
			this.fogColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder waterColor(int i) {
			this.waterColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder waterFogColor(int i) {
			this.waterFogColor = OptionalInt.of(i);
			return this;
		}

		public BiomeSpecialEffects.Builder ambientParticle(AmbientParticleSettings ambientParticleSettings) {
			this.ambientParticle = Optional.of(ambientParticleSettings);
			return this;
		}

		public BiomeSpecialEffects build() {
			return new BiomeSpecialEffects(
				this.fogColor.orElseThrow(() -> new IllegalStateException("Missing 'fog' color.")),
				this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
				this.waterFogColor.orElseThrow(() -> new IllegalStateException("Missing 'water fog' color.")),
				this.ambientParticle
			);
		}
	}
}
