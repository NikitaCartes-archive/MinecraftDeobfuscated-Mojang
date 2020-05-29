/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class JigsawBlock
extends Block
implements EntityBlock {
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    protected JigsawBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        Direction direction2 = direction.getAxis() == Direction.Axis.Y ? blockPlaceContext.getHorizontalDirection().getOpposite() : Direction.UP;
        return (BlockState)this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new JigsawBlockEntity();
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity && player.canUseGameMasterBlocks()) {
            player.openJigsawBlock((JigsawBlockEntity)blockEntity);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2) {
        Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state);
        Direction direction2 = JigsawBlock.getFrontFacing(structureBlockInfo2.state);
        Direction direction3 = JigsawBlock.getTopFacing(structureBlockInfo.state);
        Direction direction4 = JigsawBlock.getTopFacing(structureBlockInfo2.state);
        JigsawBlockEntity.JointType jointType = JigsawBlockEntity.JointType.byName(structureBlockInfo.nbt.getString("joint")).orElseGet(() -> direction.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);
        boolean bl = jointType == JigsawBlockEntity.JointType.ROLLABLE;
        return direction == direction2.getOpposite() && (bl || direction3 == direction4) && structureBlockInfo.nbt.getString("target").equals(structureBlockInfo2.nbt.getString("name"));
    }

    public static Direction getFrontFacing(BlockState blockState) {
        return blockState.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState blockState) {
        return blockState.getValue(ORIENTATION).top();
    }
}

