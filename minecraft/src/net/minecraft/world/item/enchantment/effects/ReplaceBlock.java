package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceBlock(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState) implements EnchantmentEntityEffect {
	public static final MapCodec<ReplaceBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceBlock::offset),
					BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlock::predicate),
					BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceBlock::blockState)
				)
				.apply(instance, ReplaceBlock::new)
	);

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		BlockPos blockPos = BlockPos.containing(vec3).offset(this.offset);
		if ((Boolean)this.predicate.map(blockPredicate -> blockPredicate.test(serverLevel, blockPos)).orElse(true)) {
			serverLevel.setBlockAndUpdate(blockPos, this.blockState.getState(entity.getRandom(), blockPos));
		}
	}

	@Override
	public MapCodec<ReplaceBlock> codec() {
		return CODEC;
	}
}
