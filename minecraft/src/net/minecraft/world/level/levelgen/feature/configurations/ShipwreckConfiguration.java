package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class ShipwreckConfiguration implements FeatureConfiguration {
	public static final Codec<ShipwreckConfiguration> CODEC = Codec.BOOL
		.fieldOf("is_beached")
		.orElse(false)
		.<ShipwreckConfiguration>xmap(ShipwreckConfiguration::new, shipwreckConfiguration -> shipwreckConfiguration.isBeached)
		.codec();
	public final boolean isBeached;

	public ShipwreckConfiguration(boolean bl) {
		this.isBeached = bl;
	}
}
