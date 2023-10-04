package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlock extends BaseEntityBlock {
	public static final MapCodec<SpawnerBlock> CODEC = simpleCodec(SpawnerBlock::new);

	@Override
	public MapCodec<SpawnerBlock> codec() {
		return CODEC;
	}

	protected SpawnerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SpawnerBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityType.MOB_SPAWNER, level.isClientSide ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (bl) {
			int i = 15 + serverLevel.random.nextInt(15) + serverLevel.random.nextInt(15);
			this.popExperience(serverLevel, blockPos, i);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
		Optional<Component> optional = this.getSpawnEntityDisplayName(itemStack);
		if (optional.isPresent()) {
			list.add((Component)optional.get());
		} else {
			list.add(CommonComponents.EMPTY);
			list.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
			list.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
		}
	}

	private Optional<Component> getSpawnEntityDisplayName(ItemStack itemStack) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null && compoundTag.contains("SpawnData", 10)) {
			String string = compoundTag.getCompound("SpawnData").getCompound("entity").getString("id");
			ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
			if (resourceLocation != null) {
				return BuiltInRegistries.ENTITY_TYPE
					.getOptional(resourceLocation)
					.map(entityType -> Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY));
			}
		}

		return Optional.empty();
	}
}
