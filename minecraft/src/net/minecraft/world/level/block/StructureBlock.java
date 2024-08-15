package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class StructureBlock extends BaseEntityBlock implements GameMasterBlock {
	public static final MapCodec<StructureBlock> CODEC = simpleCodec(StructureBlock::new);
	public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

	@Override
	public MapCodec<StructureBlock> codec() {
		return CODEC;
	}

	protected StructureBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(MODE, StructureMode.LOAD));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new StructureBlockEntity(blockPos, blockState);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof StructureBlockEntity) {
			return (InteractionResult)(((StructureBlockEntity)blockEntity).usedBy(player) ? InteractionResult.SUCCESS : InteractionResult.PASS);
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		if (!level.isClientSide) {
			if (livingEntity != null) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity instanceof StructureBlockEntity) {
					((StructureBlockEntity)blockEntity).createdBy(livingEntity);
				}
			}
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MODE);
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		if (level instanceof ServerLevel) {
			if (level.getBlockEntity(blockPos) instanceof StructureBlockEntity structureBlockEntity) {
				boolean bl2 = level.hasNeighborSignal(blockPos);
				boolean bl3 = structureBlockEntity.isPowered();
				if (bl2 && !bl3) {
					structureBlockEntity.setPowered(true);
					this.trigger((ServerLevel)level, structureBlockEntity);
				} else if (!bl2 && bl3) {
					structureBlockEntity.setPowered(false);
				}
			}
		}
	}

	private void trigger(ServerLevel serverLevel, StructureBlockEntity structureBlockEntity) {
		switch (structureBlockEntity.getMode()) {
			case SAVE:
				structureBlockEntity.saveStructure(false);
				break;
			case LOAD:
				structureBlockEntity.placeStructure(serverLevel);
				break;
			case CORNER:
				structureBlockEntity.unloadStructure();
			case DATA:
		}
	}
}
