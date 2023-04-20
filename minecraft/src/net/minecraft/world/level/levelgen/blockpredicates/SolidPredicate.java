package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class SolidPredicate extends StateTestingPredicate {
	public static final Codec<SolidPredicate> CODEC = RecordCodecBuilder.create(instance -> stateTestingCodec(instance).apply(instance, SolidPredicate::new));

	public SolidPredicate(Vec3i vec3i) {
		super(vec3i);
	}

	@Override
	protected boolean test(BlockState blockState) {
		return blockState.isSolid();
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.SOLID;
	}
}
