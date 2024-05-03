package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceDisc(LevelBasedValue radius, LevelBasedValue height, Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState)
	implements EnchantmentEntityEffect {
	public static final MapCodec<ReplaceDisc> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					LevelBasedValue.CODEC.fieldOf("radius").forGetter(ReplaceDisc::radius),
					LevelBasedValue.CODEC.fieldOf("height").forGetter(ReplaceDisc::height),
					Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceDisc::offset),
					BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceDisc::predicate),
					BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceDisc::blockState)
				)
				.apply(instance, ReplaceDisc::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		BlockPos blockPos = BlockPos.containing(vec3).offset(this.offset);
		RandomSource randomSource = entity.getRandom();
		int j = (int)this.radius.calculate(i);
		int k = (int)this.height.calculate(i);

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-j, 0, -j), blockPos.offset(j, Math.min(k - 1, 0), j))) {
			if (blockPos2.closerToCenterThan(vec3, (double)j) && (Boolean)this.predicate.map(blockPredicate -> blockPredicate.test(serverLevel, blockPos2)).orElse(true)
				)
			 {
				serverLevel.setBlockAndUpdate(blockPos2, this.blockState.getState(randomSource, blockPos2));
			}
		}
	}

	@Override
	public MapCodec<ReplaceDisc> codec() {
		return CODEC;
	}
}
