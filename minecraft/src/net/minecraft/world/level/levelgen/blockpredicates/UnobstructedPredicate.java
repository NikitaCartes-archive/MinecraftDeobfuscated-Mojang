package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.phys.shapes.Shapes;

record UnobstructedPredicate(Vec3i offset) implements BlockPredicate {
	public static MapCodec<UnobstructedPredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(UnobstructedPredicate::offset))
				.apply(instance, UnobstructedPredicate::new)
	);

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.UNOBSTRUCTED;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return worldGenLevel.isUnobstructed(null, Shapes.block().move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()));
	}
}
