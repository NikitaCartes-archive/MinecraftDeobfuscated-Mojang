package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends Item {
	public DebugStickItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		if (!level.isClientSide) {
			this.handleInteraction(player, blockState, level, blockPos, false, player.getItemInHand(InteractionHand.MAIN_HAND));
		}

		return false;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		Level level = useOnContext.getLevel();
		if (!level.isClientSide && player != null) {
			BlockPos blockPos = useOnContext.getClickedPos();
			if (!this.handleInteraction(player, level.getBlockState(blockPos), level, blockPos, true, useOnContext.getItemInHand())) {
				return InteractionResult.FAIL;
			}
		}

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	private boolean handleInteraction(Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl, ItemStack itemStack) {
		if (!player.canUseGameMasterBlocks()) {
			return false;
		} else {
			Holder<Block> holder = blockState.getBlockHolder();
			StateDefinition<Block, BlockState> stateDefinition = holder.value().getStateDefinition();
			Collection<Property<?>> collection = stateDefinition.getProperties();
			if (collection.isEmpty()) {
				message(player, Component.translatable(this.getDescriptionId() + ".empty", holder.getRegisteredName()));
				return false;
			} else {
				DebugStickState debugStickState = itemStack.get(DataComponents.DEBUG_STICK_STATE);
				if (debugStickState == null) {
					return false;
				} else {
					Property<?> property = (Property<?>)debugStickState.properties().get(holder);
					if (bl) {
						if (property == null) {
							property = (Property<?>)collection.iterator().next();
						}

						BlockState blockState2 = cycleState(blockState, property, player.isSecondaryUseActive());
						levelAccessor.setBlock(blockPos, blockState2, 18);
						message(player, Component.translatable(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockState2, property)));
					} else {
						property = getRelative(collection, property, player.isSecondaryUseActive());
						itemStack.set(DataComponents.DEBUG_STICK_STATE, debugStickState.withProperty(holder, property));
						message(player, Component.translatable(this.getDescriptionId() + ".select", property.getName(), getNameHelper(blockState, property)));
					}

					return true;
				}
			}
		}
	}

	private static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean bl) {
		return blockState.setValue(property, getRelative(property.getPossibleValues(), blockState.getValue(property), bl));
	}

	private static <T> T getRelative(Iterable<T> iterable, @Nullable T object, boolean bl) {
		return bl ? Util.findPreviousInIterable(iterable, object) : Util.findNextInIterable(iterable, object);
	}

	private static void message(Player player, Component component) {
		((ServerPlayer)player).sendSystemMessage(component, true);
	}

	private static <T extends Comparable<T>> String getNameHelper(BlockState blockState, Property<T> property) {
		return property.getName(blockState.getValue(property));
	}
}
