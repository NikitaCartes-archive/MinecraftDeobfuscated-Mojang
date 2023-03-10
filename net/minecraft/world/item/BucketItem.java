/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class BucketItem
extends Item
implements DispensibleContainerItem {
    private final Fluid content;

    public BucketItem(Fluid fluid, Item.Properties properties) {
        super(properties);
        this.content = fluid;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = BucketItem.getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos3;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos2, direction, itemStack)) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (this.content == Fluids.EMPTY) {
                BucketPickup bucketPickup;
                ItemStack itemStack2;
                BlockState blockState = level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof BucketPickup && !(itemStack2 = (bucketPickup = (BucketPickup)((Object)blockState.getBlock())).pickupBlock(level, blockPos, blockState)).isEmpty()) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    bucketPickup.getPickupSound().ifPresent(soundEvent -> player.playSound((SoundEvent)soundEvent, 1.0f, 1.0f));
                    level.gameEvent((Entity)player, GameEvent.FLUID_PICKUP, blockPos);
                    ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2);
                    if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemStack2);
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
                }
                return InteractionResultHolder.fail(itemStack);
            }
            BlockState blockState = level.getBlockState(blockPos);
            BlockPos blockPos4 = blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? blockPos : blockPos2;
            if (this.emptyContents(player, level, blockPos3, blockHitResult)) {
                this.checkExtraContent(player, level, itemStack, blockPos3);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos3, itemStack);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(BucketItem.getEmptySuccessItem(itemStack, player), level.isClientSide());
            }
            return InteractionResultHolder.fail(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    public static ItemStack getEmptySuccessItem(ItemStack itemStack, Player player) {
        if (!player.getAbilities().instabuild) {
            return new ItemStack(Items.BUCKET);
        }
        return itemStack;
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack itemStack, BlockPos blockPos) {
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        boolean bl2;
        if (!(this.content instanceof FlowingFluid)) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        boolean bl = blockState.canBeReplaced(this.content);
        boolean bl3 = bl2 = blockState.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)((Object)block)).canPlaceLiquid(level, blockPos, blockState, this.content);
        if (!bl2) {
            return blockHitResult != null && this.emptyContents(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
        }
        if (level.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
            int i = blockPos.getX();
            int j = blockPos.getY();
            int k = blockPos.getZ();
            level.playSound(player, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if (block instanceof LiquidBlockContainer && this.content == Fluids.WATER) {
            ((LiquidBlockContainer)((Object)block)).placeLiquid(level, blockPos, blockState, ((FlowingFluid)this.content).getSource(false));
            this.playEmptySound(player, level, blockPos);
            return true;
        }
        if (!level.isClientSide && bl && !material.isLiquid()) {
            level.destroyBlock(blockPos, true);
        }
        if (level.setBlock(blockPos, this.content.defaultFluidState().createLegacyBlock(), 11) || blockState.getFluidState().isSource()) {
            this.playEmptySound(player, level, blockPos);
            return true;
        }
        return false;
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        SoundEvent soundEvent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        levelAccessor.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        levelAccessor.gameEvent((Entity)player, GameEvent.FLUID_PLACE, blockPos);
    }
}

