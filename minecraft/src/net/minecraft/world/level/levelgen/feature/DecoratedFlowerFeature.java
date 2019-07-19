package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;

public class DecoratedFlowerFeature extends DecoratedFeature {
	public DecoratedFlowerFeature(Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration> function) {
		super(function);
	}
}
