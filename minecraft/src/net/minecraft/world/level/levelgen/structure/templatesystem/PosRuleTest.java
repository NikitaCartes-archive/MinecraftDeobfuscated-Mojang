package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

public abstract class PosRuleTest {
	public abstract boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random);

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.mergeInto(
				this.getDynamic(dynamicOps).getValue(),
				dynamicOps.createString("predicate_type"),
				dynamicOps.createString(Registry.POS_RULE_TEST.getKey(this.getType()).toString())
			)
		);
	}

	protected abstract PosRuleTestType getType();

	protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps);
}
