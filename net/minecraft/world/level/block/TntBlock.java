/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TntBlock
extends Block {
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    public TntBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(UNSTABLE, false));
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.getBlock() == blockState.getBlock()) {
            return;
        }
        if (level.hasNeighborSignal(blockPos)) {
            TntBlock.explode(level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.hasNeighborSignal(blockPos)) {
            TntBlock.explode(level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide() && !player.isCreative() && blockState.getValue(UNSTABLE).booleanValue()) {
            TntBlock.explode(level, blockPos);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level.isClientSide) {
            return;
        }
        PrimedTnt primedTnt = new PrimedTnt(level, (float)blockPos.getX() + 0.5f, blockPos.getY(), (float)blockPos.getZ() + 0.5f, explosion.getSourceMob());
        primedTnt.setFuse((short)(level.random.nextInt(primedTnt.getLife() / 4) + primedTnt.getLife() / 8));
        level.addFreshEntity(primedTnt);
    }

    public static void explode(Level level, BlockPos blockPos) {
        TntBlock.explode(level, blockPos, null);
    }

    private static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        if (level.isClientSide) {
            return;
        }
        PrimedTnt primedTnt = new PrimedTnt(level, (float)blockPos.getX() + 0.5f, blockPos.getY(), (float)blockPos.getZ() + 0.5f, livingEntity);
        level.addFreshEntity(primedTnt);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player2, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemStack = player2.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            TntBlock.explode(level, blockPos, player2);
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            if (!player2.isCreative()) {
                if (item == Items.FLINT_AND_STEEL) {
                    itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(interactionHand));
                } else {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(blockState, level, blockPos, player2, interactionHand, blockHitResult);
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
        if (!level.isClientSide && entity instanceof AbstractArrow) {
            AbstractArrow abstractArrow = (AbstractArrow)entity;
            Entity entity2 = abstractArrow.getOwner();
            if (abstractArrow.isOnFire()) {
                BlockPos blockPos = blockHitResult.getBlockPos();
                TntBlock.explode(level, blockPos, entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null);
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}

