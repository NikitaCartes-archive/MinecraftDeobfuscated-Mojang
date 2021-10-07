package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StateTestingPredicate implements BlockPredicate {
	protected final BlockPos offset;

	protected static <P extends StateTestingPredicate> P1<Mu<P>, BlockPos> stateTestingCodec(Instance<P> instance) {
		return instance.group(BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(stateTestingPredicate -> stateTestingPredicate.offset));
	}

	protected StateTestingPredicate(BlockPos blockPos) {
		this.offset = blockPos;
	}

	public final boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return this.test(worldGenLevel.getBlockState(blockPos.offset(this.offset)));
	}

	protected abstract boolean test(BlockState blockState);
}
