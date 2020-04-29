package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
	private final Block block;
	private final float probability;

	public RandomBlockMatchTest(Block block, float f) {
		this.block = block;
		this.probability = f;
	}

	public <T> RandomBlockMatchTest(Dynamic<T> dynamic) {
		this(Registry.BLOCK.get(new ResourceLocation(dynamic.get("block").asString(""))), dynamic.get("probability").asFloat(1.0F));
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return blockState.is(this.block) && random.nextFloat() < this.probability;
	}

	@Override
	protected RuleTestType getType() {
		return RuleTestType.RANDOM_BLOCK_TEST;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("block"),
					dynamicOps.createString(Registry.BLOCK.getKey(this.block).toString()),
					dynamicOps.createString("probability"),
					dynamicOps.createFloat(this.probability)
				)
			)
		);
	}
}
