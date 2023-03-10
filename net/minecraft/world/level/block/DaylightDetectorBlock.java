/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DaylightDetectorBlock
extends BaseEntityBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

    public DaylightDetectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWER, 0)).setValue(INVERTED, false));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWER);
    }

    private static void updateSignalStrength(BlockState blockState, Level level, BlockPos blockPos) {
        int i = level.getBrightness(LightLayer.SKY, blockPos) - level.getSkyDarken();
        float f = level.getSunAngle(1.0f);
        boolean bl = blockState.getValue(INVERTED);
        if (bl) {
            i = 15 - i;
        } else if (i > 0) {
            float g = f < (float)Math.PI ? 0.0f : (float)Math.PI * 2;
            f += (g - f) * 0.2f;
            i = Math.round((float)i * Mth.cos(f));
        }
        i = Mth.clamp(i, 0, 15);
        if (blockState.getValue(POWER) != i) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(POWER, i), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player.mayBuild()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            BlockState blockState2 = (BlockState)blockState.cycle(INVERTED);
            level.setBlock(blockPos, blockState2, 4);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState2));
            DaylightDetectorBlock.updateSignalStrength(blockState2, level, blockPos);
            return InteractionResult.CONSUME;
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DaylightDetectorBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide && level.dimensionType().hasSkyLight()) {
            return DaylightDetectorBlock.createTickerHelper(blockEntityType, BlockEntityType.DAYLIGHT_DETECTOR, DaylightDetectorBlock::tickEntity);
        }
        return null;
    }

    private static void tickEntity(Level level, BlockPos blockPos, BlockState blockState, DaylightDetectorBlockEntity daylightDetectorBlockEntity) {
        if (level.getGameTime() % 20L == 0L) {
            DaylightDetectorBlock.updateSignalStrength(blockState, level, blockPos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, INVERTED);
    }
}

