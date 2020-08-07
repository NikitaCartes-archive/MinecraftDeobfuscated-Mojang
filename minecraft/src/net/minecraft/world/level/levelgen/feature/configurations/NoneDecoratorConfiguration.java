package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public class NoneDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<NoneDecoratorConfiguration> CODEC = Codec.unit((Supplier<NoneDecoratorConfiguration>)(() -> NoneDecoratorConfiguration.INSTANCE));
	public static final NoneDecoratorConfiguration INSTANCE = new NoneDecoratorConfiguration();
}
