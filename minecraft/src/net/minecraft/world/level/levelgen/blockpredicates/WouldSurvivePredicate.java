package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class WouldSurvivePredicate implements BlockPredicate {
	public static final Codec<WouldSurvivePredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(wouldSurvivePredicate -> wouldSurvivePredicate.offset),
					BlockState.CODEC.fieldOf("state").forGetter(wouldSurvivePredicate -> wouldSurvivePredicate.state)
				)
				.apply(instance, WouldSurvivePredicate::new)
	);
	private final Vec3i offset;
	private final BlockState state;

	protected WouldSurvivePredicate(Vec3i vec3i, BlockState blockState) {
		this.offset = vec3i;
		this.state = blockState;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return this.state.canSurvive(worldGenLevel, blockPos.offset(this.offset));
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.WOULD_SURVIVE;
	}
}
