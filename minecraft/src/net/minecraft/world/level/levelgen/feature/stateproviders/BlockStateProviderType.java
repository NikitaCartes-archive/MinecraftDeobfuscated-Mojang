package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class BlockStateProviderType<P extends BlockStateProvider> {
	public static final BlockStateProviderType<SimpleStateProvider> SIMPLE_STATE_PROVIDER = register("simple_state_provider", SimpleStateProvider::new);
	public static final BlockStateProviderType<WeightedStateProvider> WEIGHTED_STATE_PROVIDER = register("weighted_state_provider", WeightedStateProvider::new);
	public static final BlockStateProviderType<PlainFlowerProvider> PLAIN_FLOWER_PROVIDER = register("plain_flower_provider", PlainFlowerProvider::new);
	public static final BlockStateProviderType<ForestFlowerProvider> FOREST_FLOWER_PROVIDER = register("forest_flower_provider", ForestFlowerProvider::new);
	public static final BlockStateProviderType<RainbowBlockProvider> RAINBOW_BLOCK_PROVIDER = register("rainbow_provider", RainbowBlockProvider::new);
	private final Function<Dynamic<?>, P> deserializer;

	private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String string, Function<Dynamic<?>, P> function) {
		return Registry.register(Registry.BLOCKSTATE_PROVIDER_TYPES, string, new BlockStateProviderType<>(function));
	}

	private BlockStateProviderType(Function<Dynamic<?>, P> function) {
		this.deserializer = function;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}
}
