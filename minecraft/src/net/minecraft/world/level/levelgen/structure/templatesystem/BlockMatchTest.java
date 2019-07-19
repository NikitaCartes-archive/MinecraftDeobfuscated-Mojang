package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
	private final Block block;

	public BlockMatchTest(Block block) {
		this.block = block;
	}

	public <T> BlockMatchTest(Dynamic<T> dynamic) {
		this(Registry.BLOCK.get(new ResourceLocation(dynamic.get("block").asString(""))));
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return blockState.getBlock() == this.block;
	}

	@Override
	protected RuleTestType getType() {
		return RuleTestType.BLOCK_TEST;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("block"), dynamicOps.createString(Registry.BLOCK.getKey(this.block).toString())))
		);
	}
}
