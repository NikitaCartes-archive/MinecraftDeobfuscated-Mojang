/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlintAndSteelItem
extends Item {
    public FlintAndSteelItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Player player2 = useOnContext.getPlayer();
        Level levelAccessor = useOnContext.getLevel();
        BlockState blockState = levelAccessor.getBlockState(blockPos = useOnContext.getClickedPos());
        if (FlintAndSteelItem.canLightCampFire(blockState)) {
            levelAccessor.playSound(player2, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.4f + 0.8f);
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.LIT, true), 11);
            if (player2 != null) {
                useOnContext.getItemInHand().hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
            }
            return InteractionResult.SUCCESS;
        }
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (FlintAndSteelItem.canUse(levelAccessor.getBlockState(blockPos2), levelAccessor, blockPos2)) {
            levelAccessor.playSound(player2, blockPos2, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, random.nextFloat() * 0.4f + 0.8f);
            BlockState blockState2 = BaseFireBlock.getState(levelAccessor, blockPos2);
            levelAccessor.setBlock(blockPos2, blockState2, 11);
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player2 instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player2, blockPos2, itemStack);
                itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static boolean canLightCampFire(BlockState blockState) {
        return blockState.getBlock().is(BlockTags.CAMPFIRES) && blockState.getValue(BlockStateProperties.WATERLOGGED) == false && blockState.getValue(BlockStateProperties.LIT) == false;
    }

    public static boolean canUse(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = BaseFireBlock.getState(levelAccessor, blockPos);
        boolean bl = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (levelAccessor.getBlockState(blockPos.relative(direction)).getBlock() != Blocks.OBSIDIAN || NetherPortalBlock.isPortal(levelAccessor, blockPos) == null) continue;
            bl = true;
        }
        return blockState.isAir() && (blockState2.canSurvive(levelAccessor, blockPos) || bl);
    }
}

