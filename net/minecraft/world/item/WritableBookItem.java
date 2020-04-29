/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WritableBookItem
extends Item {
    public WritableBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(level, blockPos, blockState, useOnContext.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        player.openItemGui(itemStack, interactionHand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(itemStack);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
        if (compoundTag == null) {
            return false;
        }
        if (!compoundTag.contains("pages", 9)) {
            return false;
        }
        ListTag listTag = compoundTag.getList("pages", 8);
        for (int i = 0; i < listTag.size(); ++i) {
            String string = listTag.getString(i);
            if (string.length() <= Short.MAX_VALUE) continue;
            return false;
        }
        return true;
    }
}

