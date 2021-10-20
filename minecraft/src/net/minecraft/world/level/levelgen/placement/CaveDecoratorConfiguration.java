package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CaveDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<CaveDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CaveSurface.CODEC.fieldOf("surface").forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.surface),
					Codec.INT.fieldOf("floor_to_ceiling_search_range").forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.floorToCeilingSearchRange),
					Codec.BOOL.fieldOf("allow_water").forGetter(caveDecoratorConfiguration -> caveDecoratorConfiguration.allowWater)
				)
				.apply(instance, CaveDecoratorConfiguration::new)
	);
	public final CaveSurface surface;
	public final int floorToCeilingSearchRange;
	public final boolean allowWater;

	public CaveDecoratorConfiguration(CaveSurface caveSurface, int i, boolean bl) {
		this.surface = caveSurface;
		this.floorToCeilingSearchRange = i;
		this.allowWater = bl;
	}
}
