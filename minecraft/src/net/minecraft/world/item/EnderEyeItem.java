package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EnderEyeItem extends Item {
	public EnderEyeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.is(Blocks.END_PORTAL_FRAME) || (Boolean)blockState.getValue(EndPortalFrameBlock.HAS_EYE)) {
			return InteractionResult.PASS;
		} else if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockState blockState2 = blockState.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(true));
			Block.pushEntitiesUp(blockState, blockState2, level, blockPos);
			level.setBlock(blockPos, blockState2, 2);
			level.updateNeighbourForOutputSignal(blockPos, Blocks.END_PORTAL_FRAME);
			useOnContext.getItemInHand().shrink(1);
			level.levelEvent(1503, blockPos, 0);
			BlockPattern.BlockPatternMatch blockPatternMatch = EndPortalFrameBlock.getOrCreatePortalShape().find(level, blockPos);
			if (blockPatternMatch != null) {
				BlockPos blockPos2 = blockPatternMatch.getFrontTopLeft().offset(-3, 0, -3);

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						level.setBlock(blockPos2.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
					}
				}

				level.globalLevelEvent(1038, blockPos2.offset(1, 0, 1), 0);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(((BlockHitResult)hitResult).getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			player.startUsingItem(interactionHand);
			if (level instanceof ServerLevel) {
				BlockPos blockPos = ((ServerLevel)level)
					.getChunkSource()
					.getGenerator()
					.findNearestMapFeature((ServerLevel)level, StructureFeature.STRONGHOLD, player.blockPosition(), 100, false);
				if (blockPos != null) {
					EyeOfEnder eyeOfEnder = new EyeOfEnder(level, player.getX(), player.getY(0.5), player.getZ());
					eyeOfEnder.setItem(itemStack);
					eyeOfEnder.signalTo(blockPos);
					level.addFreshEntity(eyeOfEnder);
					if (player instanceof ServerPlayer) {
						CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer)player, blockPos);
					}

					level.playSound(
						null,
						player.getX(),
						player.getY(),
						player.getZ(),
						SoundEvents.ENDER_EYE_LAUNCH,
						SoundSource.NEUTRAL,
						0.5F,
						0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
					);
					level.levelEvent(null, 1003, player.blockPosition(), 0);
					if (!player.getAbilities().instabuild) {
						itemStack.shrink(1);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					player.swing(interactionHand, true);
					return InteractionResultHolder.success(itemStack);
				}
			}

			return InteractionResultHolder.consume(itemStack);
		}
	}
}
