/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SuspiciousSandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem
extends Item {
    public static final int TICKS_BETWEEN_SWEEPS = 10;
    private static final int USE_DURATION = 225;

    public BrushItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        if (player != null) {
            player.startUsingItem(useOnContext.getHand());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 225;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity2, ItemStack itemStack, int i) {
        if (i < 0 || !(livingEntity2 instanceof Player)) {
            livingEntity2.releaseUsingItem();
            return;
        }
        Player player = (Player)livingEntity2;
        BlockHitResult blockHitResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            livingEntity2.releaseUsingItem();
            return;
        }
        int j = this.getUseDuration(itemStack) - i + 1;
        if (j == 1 || j % 10 == 0) {
            SuspiciousSandBlockEntity suspiciousSandBlockEntity;
            boolean bl;
            BlockEntity blockEntity;
            BlockState blockState = level.getBlockState(blockPos);
            this.spawnDustParticles(level, blockHitResult, blockState, livingEntity2.getViewVector(0.0f));
            level.playSound(player, blockPos, SoundEvents.BRUSH_BRUSHING, SoundSource.PLAYERS);
            if (!level.isClientSide() && blockState.is(Blocks.SUSPICIOUS_SAND) && (blockEntity = level.getBlockEntity(blockPos)) instanceof SuspiciousSandBlockEntity && (bl = (suspiciousSandBlockEntity = (SuspiciousSandBlockEntity)blockEntity).brush(level.getGameTime(), player, blockHitResult.getDirection()))) {
                itemStack.hurtAndBreak(1, livingEntity2, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            }
        }
    }

    public void spawnDustParticles(Level level, BlockHitResult blockHitResult, BlockState blockState, Vec3 vec3) {
        double d = 3.0;
        int i = level.getRandom().nextInt(7, 12);
        BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
        Direction direction = blockHitResult.getDirection();
        DustParticlesDelta dustParticlesDelta = DustParticlesDelta.fromDirection(vec3, direction);
        Vec3 vec32 = blockHitResult.getLocation();
        for (int j = 0; j < i; ++j) {
            level.addParticle(blockParticleOption, vec32.x - (double)(direction == Direction.WEST ? 1.0E-6f : 0.0f), vec32.y, vec32.z - (double)(direction == Direction.NORTH ? 1.0E-6f : 0.0f), dustParticlesDelta.xd() * 3.0 * level.getRandom().nextDouble(), 0.0, dustParticlesDelta.zd() * 3.0 * level.getRandom().nextDouble());
        }
    }

    record DustParticlesDelta(double xd, double yd, double zd) {
        private static final double ALONG_SIDE_DELTA = 1.0;
        private static final double OUT_FROM_SIDE_DELTA = 0.1;

        public static DustParticlesDelta fromDirection(Vec3 vec3, Direction direction) {
            double d = 0.0;
            return switch (direction) {
                default -> throw new IncompatibleClassChangeError();
                case Direction.DOWN -> new DustParticlesDelta(-vec3.x(), 0.0, vec3.z());
                case Direction.UP -> new DustParticlesDelta(vec3.z(), 0.0, -vec3.x());
                case Direction.NORTH -> new DustParticlesDelta(1.0, 0.0, -0.1);
                case Direction.SOUTH -> new DustParticlesDelta(-1.0, 0.0, 0.1);
                case Direction.WEST -> new DustParticlesDelta(-0.1, 0.0, -1.0);
                case Direction.EAST -> new DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }
}

