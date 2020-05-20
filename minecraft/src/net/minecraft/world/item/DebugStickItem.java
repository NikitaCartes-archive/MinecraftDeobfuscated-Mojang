package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
	public boolean isFoil(ItemStack itemStack) {
		return true;
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
			this.handleInteraction(player, level.getBlockState(blockPos), level, blockPos, true, useOnContext.getItemInHand());
		}

		return InteractionResult.SUCCESS;
	}

	private void handleInteraction(Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl, ItemStack itemStack) {
		if (player.canUseGameMasterBlocks()) {
			Block block = blockState.getBlock();
			StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
			Collection<Property<?>> collection = stateDefinition.getProperties();
			String string = Registry.BLOCK.getKey(block).toString();
			if (collection.isEmpty()) {
				message(player, new TranslatableComponent(this.getDescriptionId() + ".empty", string));
			} else {
				CompoundTag compoundTag = itemStack.getOrCreateTagElement("DebugProperty");
				String string2 = compoundTag.getString(string);
				Property<?> property = stateDefinition.getProperty(string2);
				if (bl) {
					if (property == null) {
						property = (Property<?>)collection.iterator().next();
					}

					BlockState blockState2 = cycleState(blockState, property, player.isSecondaryUseActive());
					levelAccessor.setBlock(blockPos, blockState2, 18);
					message(player, new TranslatableComponent(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockState2, property)));
				} else {
					property = getRelative(collection, property, player.isSecondaryUseActive());
					String string3 = property.getName();
					compoundTag.putString(string, string3);
					message(player, new TranslatableComponent(this.getDescriptionId() + ".select", string3, getNameHelper(blockState, property)));
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
		((ServerPlayer)player).sendMessage(component, ChatType.GAME_INFO, Util.NIL_UUID);
	}

	private static <T extends Comparable<T>> String getNameHelper(BlockState blockState, Property<T> property) {
		return property.getName(blockState.getValue(property));
	}
}
