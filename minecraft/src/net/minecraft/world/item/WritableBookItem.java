package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WritableBookItem extends Item {
	public WritableBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getBlock() == Blocks.LECTERN) {
			return LecternBlock.tryPlaceBook(level, blockPos, blockState, useOnContext.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.openItemGui(itemStack, interactionHand);
		player.awardStat(Stats.ITEM_USED.get(this));
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
	}

	public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
		if (compoundTag == null) {
			return false;
		} else if (!compoundTag.contains("pages", 9)) {
			return false;
		} else {
			ListTag listTag = compoundTag.getList("pages", 8);

			for (int i = 0; i < listTag.size(); i++) {
				String string = listTag.getString(i);
				if (string.length() > 32767) {
					return false;
				}
			}

			return true;
		}
	}
}
