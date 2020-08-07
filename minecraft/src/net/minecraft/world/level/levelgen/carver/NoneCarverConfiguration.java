package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public class NoneCarverConfiguration implements CarverConfiguration {
	public static final Codec<NoneCarverConfiguration> CODEC = Codec.unit((Supplier<NoneCarverConfiguration>)(() -> NoneCarverConfiguration.INSTANCE));
	public static final NoneCarverConfiguration INSTANCE = new NoneCarverConfiguration();
}
