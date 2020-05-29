/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoneMealItem
extends Item {
    public BoneMealItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (BoneMealItem.growCrop(useOnContext.getItemInHand(), level, blockPos)) {
            if (!level.isClientSide) {
                level.levelEvent(2005, blockPos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = blockState.isFaceSturdy(level, blockPos, useOnContext.getClickedFace());
        if (bl && BoneMealItem.growWaterPlant(useOnContext.getItemInHand(), level, blockPos2, useOnContext.getClickedFace())) {
            if (!level.isClientSide) {
                level.levelEvent(2005, blockPos2, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
        BonemealableBlock bonemealableBlock;
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BonemealableBlock && (bonemealableBlock = (BonemealableBlock)((Object)blockState.getBlock())).isValidBonemealTarget(level, blockPos, blockState, level.isClientSide)) {
            if (level instanceof ServerLevel) {
                if (bonemealableBlock.isBonemealSuccess(level, level.random, blockPos, blockState)) {
                    bonemealableBlock.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
                }
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    public static boolean growWaterPlant(ItemStack itemStack, Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (!level.getBlockState(blockPos).is(Blocks.WATER) || level.getFluidState(blockPos).getAmount() != 8) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        block0: for (int i = 0; i < 128; ++i) {
            int j;
            BlockPos blockPos2 = blockPos;
            Biome biome = level.getBiome(blockPos2);
            BlockState blockState = Blocks.SEAGRASS.defaultBlockState();
            for (j = 0; j < i / 16; ++j) {
                blockPos2 = blockPos2.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                biome = level.getBiome(blockPos2);
                if (level.getBlockState(blockPos2).isCollisionShapeFullBlock(level, blockPos2)) continue block0;
            }
            if (biome == Biomes.WARM_OCEAN || biome == Biomes.DEEP_WARM_OCEAN) {
                if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
                    blockState = (BlockState)((Block)BlockTags.WALL_CORALS.getRandomElement(level.random)).defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, direction);
                } else if (random.nextInt(4) == 0) {
                    blockState = ((Block)BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random)).defaultBlockState();
                }
            }
            if (blockState.getBlock().is(BlockTags.WALL_CORALS)) {
                for (j = 0; !blockState.canSurvive(level, blockPos2) && j < 4; ++j) {
                    blockState = (BlockState)blockState.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                }
            }
            if (!blockState.canSurvive(level, blockPos2)) continue;
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (blockState2.is(Blocks.WATER) && level.getFluidState(blockPos2).getAmount() == 8) {
                level.setBlock(blockPos2, blockState, 3);
                continue;
            }
            if (!blockState2.is(Blocks.SEAGRASS) || random.nextInt(10) != 0) continue;
            ((BonemealableBlock)((Object)Blocks.SEAGRASS)).performBonemeal((ServerLevel)level, random, blockPos2, blockState2);
        }
        itemStack.shrink(1);
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static void addGrowthParticles(LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        double e;
        BlockState blockState;
        if (i == 0) {
            i = 15;
        }
        if ((blockState = levelAccessor.getBlockState(blockPos)).isAir()) {
            return;
        }
        double d = 0.5;
        if (!blockState.getFluidState().isEmpty()) {
            i *= 3;
            e = 1.0;
            d = 3.0;
        } else if (blockState.isSolidRender(levelAccessor, blockPos)) {
            blockPos = blockPos.above();
            i *= 3;
            d = 3.0;
            e = 1.0;
        } else {
            e = blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
        }
        levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
        for (int j = 0; j < i; ++j) {
            double n;
            double m;
            double f = random.nextGaussian() * 0.02;
            double g = random.nextGaussian() * 0.02;
            double h = random.nextGaussian() * 0.02;
            double k = 0.5 - d;
            double l = (double)blockPos.getX() + k + random.nextDouble() * d * 2.0;
            if (levelAccessor.getBlockState(new BlockPos(l, m = (double)blockPos.getY() + random.nextDouble() * e, n = (double)blockPos.getZ() + k + random.nextDouble() * d * 2.0).below()).isAir()) continue;
            levelAccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, l, m, n, f, g, h);
        }
    }
}

