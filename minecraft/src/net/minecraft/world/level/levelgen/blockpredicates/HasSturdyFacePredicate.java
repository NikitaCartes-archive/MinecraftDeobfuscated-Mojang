package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class HasSturdyFacePredicate implements BlockPredicate {
	private final Vec3i offset;
	private final Direction direction;
	public static final Codec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(hasSturdyFacePredicate -> hasSturdyFacePredicate.offset),
					Direction.CODEC.fieldOf("direction").forGetter(hasSturdyFacePredicate -> hasSturdyFacePredicate.direction)
				)
				.apply(instance, HasSturdyFacePredicate::new)
	);

	public HasSturdyFacePredicate(Vec3i vec3i, Direction direction) {
		this.offset = vec3i;
		this.direction = direction;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.offset(this.offset);
		return worldGenLevel.getBlockState(blockPos2).isFaceSturdy(worldGenLevel, blockPos2, this.direction);
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.HAS_STURDY_FACE;
	}
}
