/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedStoneOreBlock
extends Block {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public RedStoneOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LIT, false));
    }

    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        RedStoneOreBlock.interact(blockState, level, blockPos);
        super.attack(blockState, level, blockPos, player);
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        RedStoneOreBlock.interact(level.getBlockState(blockPos), level, blockPos);
        super.stepOn(level, blockPos, entity);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            RedStoneOreBlock.spawnParticles(level, blockPos);
        } else {
            RedStoneOreBlock.interact(blockState, level, blockPos);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.getItem() instanceof BlockItem && new BlockPlaceContext(player, interactionHand, itemStack, blockHitResult).canPlace()) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private static void interact(BlockState blockState, Level level, BlockPos blockPos) {
        RedStoneOreBlock.spawnParticles(level, blockPos);
        if (!blockState.getValue(LIT).booleanValue()) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(LIT, true), 3);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(LIT);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (blockState.getValue(LIT).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, false), 3);
        }
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
            int i = 1 + serverLevel.random.nextInt(5);
            this.popExperience(serverLevel, blockPos, i);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (blockState.getValue(LIT).booleanValue()) {
            RedStoneOreBlock.spawnParticles(level, blockPos);
        }
    }

    private static void spawnParticles(Level level, BlockPos blockPos) {
        double d = 0.5625;
        Random random = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.getBlockState(blockPos2).isSolidRender(level, blockPos2)) continue;
            Direction.Axis axis = direction.getAxis();
            double e = axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getStepX() : (double)random.nextFloat();
            double f = axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getStepY() : (double)random.nextFloat();
            double g = axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getStepZ() : (double)random.nextFloat();
            level.addParticle(DustParticleOptions.REDSTONE, (double)blockPos.getX() + e, (double)blockPos.getY() + f, (double)blockPos.getZ() + g, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}

