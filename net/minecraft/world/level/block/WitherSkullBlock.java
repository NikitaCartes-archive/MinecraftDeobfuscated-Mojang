/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class WitherSkullBlock
extends SkullBlock {
    @Nullable
    private static BlockPattern witherPatternFull;
    @Nullable
    private static BlockPattern witherPatternBase;

    protected WitherSkullBlock(BlockBehaviour.Properties properties) {
        super(SkullBlock.Types.WITHER_SKELETON, properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SkullBlockEntity) {
            WitherSkullBlock.checkSpawn(level, blockPos, (SkullBlockEntity)blockEntity);
        }
    }

    public static void checkSpawn(Level level, BlockPos blockPos, SkullBlockEntity skullBlockEntity) {
        boolean bl;
        if (level.isClientSide) {
            return;
        }
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl2 = bl = blockState.is(Blocks.WITHER_SKELETON_SKULL) || blockState.is(Blocks.WITHER_SKELETON_WALL_SKULL);
        if (!bl || blockPos.getY() < level.getMinBuildHeight() || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPattern blockPattern = WitherSkullBlock.getOrCreateWitherFull();
        BlockPattern.BlockPatternMatch blockPatternMatch = blockPattern.find(level, blockPos);
        if (blockPatternMatch == null) {
            return;
        }
        for (int i = 0; i < blockPattern.getWidth(); ++i) {
            for (int j = 0; j < blockPattern.getHeight(); ++j) {
                BlockInWorld blockInWorld = blockPatternMatch.getBlock(i, j, 0);
                level.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                level.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
            }
        }
        WitherBoss witherBoss = EntityType.WITHER.create(level);
        BlockPos blockPos2 = blockPatternMatch.getBlock(1, 2, 0).getPos();
        witherBoss.moveTo((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.55, (double)blockPos2.getZ() + 0.5, blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f, 0.0f);
        witherBoss.yBodyRot = blockPatternMatch.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f;
        witherBoss.makeInvulnerable();
        for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, witherBoss.getBoundingBox().inflate(50.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, witherBoss);
        }
        level.addFreshEntity(witherBoss);
        for (int k = 0; k < blockPattern.getWidth(); ++k) {
            for (int l = 0; l < blockPattern.getHeight(); ++l) {
                level.blockUpdated(blockPatternMatch.getBlock(k, l, 0).getPos(), Blocks.AIR);
            }
        }
    }

    public static boolean canSpawnMob(Level level, BlockPos blockPos, ItemStack itemStack) {
        if (itemStack.is(Items.WITHER_SKELETON_SKULL) && blockPos.getY() >= level.getMinBuildHeight() + 2 && level.getDifficulty() != Difficulty.PEACEFUL && !level.isClientSide) {
            return WitherSkullBlock.getOrCreateWitherBase().find(level, blockPos) != null;
        }
        return false;
    }

    private static BlockPattern getOrCreateWitherFull() {
        if (witherPatternFull == null) {
            witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return witherPatternFull;
    }

    private static BlockPattern getOrCreateWitherBase() {
        if (witherPatternBase == null) {
            witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', blockInWorld -> blockInWorld.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('~', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();
        }
        return witherPatternBase;
    }
}

