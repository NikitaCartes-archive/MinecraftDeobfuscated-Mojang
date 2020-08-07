package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class TopSolidHeightMapDecorator extends BaseHeightmapDecorator<NoneDecoratorConfiguration> {
	public TopSolidHeightMapDecorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	protected Heightmap.Types type(NoneDecoratorConfiguration noneDecoratorConfiguration) {
		return Heightmap.Types.OCEAN_FLOOR_WG;
	}
}
