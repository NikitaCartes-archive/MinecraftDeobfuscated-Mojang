package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CartographyTableBlock extends Block {
	public static final MapCodec<CartographyTableBlock> CODEC = simpleCodec(CartographyTableBlock::new);
	private static final Component CONTAINER_TITLE = Component.translatable("container.cartography_table");

	@Override
	public MapCodec<CartographyTableBlock> codec() {
		return CODEC;
	}

	protected CartographyTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			player.openMenu(blockState.getMenuProvider(level, blockPos));
			player.awardStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
			return InteractionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return new SimpleMenuProvider((i, inventory, player) -> new CartographyTableMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), CONTAINER_TITLE);
	}
}
