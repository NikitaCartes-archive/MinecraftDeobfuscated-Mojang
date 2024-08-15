package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
	public ShearsItem(Item.Properties properties) {
		super(properties);
	}

	public static Tool createToolProperties() {
		HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return new Tool(
			List.of(
				Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
				Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.LEAVES), 15.0F),
				Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.WOOL), 5.0F),
				Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.VINE.builtInRegistryHolder(), Blocks.GLOW_LICHEN.builtInRegistryHolder()), 2.0F)
			),
			1.0F,
			1
		);
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (!level.isClientSide && !blockState.is(BlockTags.FIRE)) {
			itemStack.hurtAndBreak(1, livingEntity, EquipmentSlot.MAINHAND);
		}

		return blockState.is(BlockTags.LEAVES)
			|| blockState.is(Blocks.COBWEB)
			|| blockState.is(Blocks.SHORT_GRASS)
			|| blockState.is(Blocks.FERN)
			|| blockState.is(Blocks.DEAD_BUSH)
			|| blockState.is(Blocks.HANGING_ROOTS)
			|| blockState.is(Blocks.VINE)
			|| blockState.is(Blocks.TRIPWIRE)
			|| blockState.is(BlockTags.WOOL);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getBlock() instanceof GrowingPlantHeadBlock growingPlantHeadBlock && !growingPlantHeadBlock.isMaxAge(blockState)) {
			Player player = useOnContext.getPlayer();
			ItemStack itemStack = useOnContext.getItemInHand();
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
			}

			level.playSound(player, blockPos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
			BlockState blockState2 = growingPlantHeadBlock.getMaxAgeState(blockState);
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), blockState2));
			if (player != null) {
				itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(useOnContext.getHand()));
			}

			return InteractionResult.SUCCESS;
		}

		return super.useOn(useOnContext);
	}
}
