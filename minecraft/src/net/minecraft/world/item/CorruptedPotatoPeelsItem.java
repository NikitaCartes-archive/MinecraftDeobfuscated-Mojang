package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CorruptedPotatoPeelsItem extends Item {
	public CorruptedPotatoPeelsItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(Blocks.TERREDEPOMME) && useOnContext.getClickedFace() == Direction.UP) {
			Player player = useOnContext.getPlayer();
			useOnContext.getItemInHand().shrink(1);
			level.playSound(player, blockPos, SoundEvents.CORRUPTED_PEELGRASS_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat(0.9F, 1.1F));
			level.setBlockAndUpdate(blockPos, Blocks.CORRUPTED_PEELGRASS_BLOCK.defaultBlockState());
			if (player instanceof ServerPlayer serverPlayer && player.level().dimension().equals(Level.OVERWORLD)) {
				CriteriaTriggers.BRING_HOME_CORRUPTION.trigger(serverPlayer);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return super.useOn(useOnContext);
		}
	}
}
