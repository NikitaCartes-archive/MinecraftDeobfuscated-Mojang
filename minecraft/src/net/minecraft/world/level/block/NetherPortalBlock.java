package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherPortalBlock extends PortalBlock {
	public NetherPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.dimension.isNaturalDimension()
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
			&& random.nextInt(2000) < serverLevel.getDifficulty().getId()) {
			while (serverLevel.getBlockState(blockPos).getBlock() == this) {
				blockPos = blockPos.below();
			}

			if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIFIED_PIGLIN)) {
				Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(serverLevel, null, null, null, blockPos.above(), MobSpawnType.STRUCTURE, false, false);
				if (entity != null) {
					entity.changingDimensionDelay = entity.getDimensionChangingDelay();
				}
			}
		}
	}
}
