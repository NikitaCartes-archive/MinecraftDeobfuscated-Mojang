package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StateTestingPredicate implements BlockPredicate {
	protected final Vec3i offset;

	protected static <P extends StateTestingPredicate> P1<Mu<P>, Vec3i> stateTestingCodec(Instance<P> instance) {
		return instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(stateTestingPredicate -> stateTestingPredicate.offset));
	}

	protected StateTestingPredicate(Vec3i vec3i) {
		this.offset = vec3i;
	}

	public final boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return this.test(worldGenLevel.getBlockState(blockPos.offset(this.offset)));
	}

	protected abstract boolean test(BlockState blockState);
}
