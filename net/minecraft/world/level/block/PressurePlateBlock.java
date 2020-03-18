/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class PressurePlateBlock
extends BasePressurePlateBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final Sensitivity sensitivity;

    protected PressurePlateBlock(Sensitivity sensitivity, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false));
        this.sensitivity = sensitivity;
    }

    @Override
    protected int getSignalForState(BlockState blockState) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected BlockState setSignalForState(BlockState blockState, int i) {
        return (BlockState)blockState.setValue(POWERED, i > 0);
    }

    @Override
    protected void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (this.material == Material.WOOD) {
            levelAccessor.playSound(null, blockPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.8f);
        } else {
            levelAccessor.playSound(null, blockPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
        }
    }

    @Override
    protected void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (this.material == Material.WOOD) {
            levelAccessor.playSound(null, blockPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.7f);
        } else {
            levelAccessor.playSound(null, blockPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.5f);
        }
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos blockPos) {
        List<Entity> list;
        AABB aABB = TOUCH_AABB.move(blockPos);
        switch (this.sensitivity) {
            case EVERYTHING: {
                list = level.getEntities(null, aABB);
                break;
            }
            case MOBS: {
                list = level.getEntitiesOfClass(LivingEntity.class, aABB);
                break;
            }
            default: {
                return 0;
            }
        }
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (entity.isIgnoringBlockTriggers()) continue;
                return 15;
            }
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    public static enum Sensitivity {
        EVERYTHING,
        MOBS;

    }
}

