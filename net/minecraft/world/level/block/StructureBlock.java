/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StructureBlock
extends BaseEntityBlock
implements GameMasterBlock {
    public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

    protected StructureBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(MODE, StructureMode.LOAD));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new StructureBlockEntity(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof StructureBlockEntity) {
            return ((StructureBlockEntity)blockEntity).usedBy(player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        BlockEntity blockEntity;
        if (level.isClientSide) {
            return;
        }
        if (livingEntity != null && (blockEntity = level.getBlockEntity(blockPos)) instanceof StructureBlockEntity) {
            ((StructureBlockEntity)blockEntity).createdBy(livingEntity);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof StructureBlockEntity)) {
            return;
        }
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)blockEntity;
        boolean bl2 = level.hasNeighborSignal(blockPos);
        boolean bl3 = structureBlockEntity.isPowered();
        if (bl2 && !bl3) {
            structureBlockEntity.setPowered(true);
            this.trigger((ServerLevel)level, structureBlockEntity);
        } else if (!bl2 && bl3) {
            structureBlockEntity.setPowered(false);
        }
    }

    private void trigger(ServerLevel serverLevel, StructureBlockEntity structureBlockEntity) {
        switch (structureBlockEntity.getMode()) {
            case SAVE: {
                structureBlockEntity.saveStructure(false);
                break;
            }
            case LOAD: {
                structureBlockEntity.loadStructure(serverLevel, false);
                break;
            }
            case CORNER: {
                structureBlockEntity.unloadStructure();
                break;
            }
            case DATA: {
                break;
            }
        }
    }
}

