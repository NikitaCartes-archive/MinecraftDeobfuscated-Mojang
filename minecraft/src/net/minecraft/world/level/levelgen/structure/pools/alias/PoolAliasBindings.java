package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class PoolAliasBindings {
	public static Codec<? extends PoolAliasBinding> bootstrap(Registry<Codec<? extends PoolAliasBinding>> registry) {
		Registry.register(registry, "random", Random.CODEC);
		Registry.register(registry, "random_group", RandomGroup.CODEC);
		return Registry.register(registry, "direct", Direct.CODEC);
	}
}
