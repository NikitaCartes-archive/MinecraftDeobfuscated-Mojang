/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.Collection;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class DebugStickItem
extends Item {
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
        BlockPos blockPos;
        Player player = useOnContext.getPlayer();
        Level level = useOnContext.getLevel();
        if (!level.isClientSide && player != null && !this.handleInteraction(player, level.getBlockState(blockPos = useOnContext.getClickedPos()), level, blockPos, true, useOnContext.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean handleInteraction(Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl, ItemStack itemStack) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        }
        Block block = blockState.getBlock();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        Collection<Property<?>> collection = stateDefinition.getProperties();
        String string = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (collection.isEmpty()) {
            DebugStickItem.message(player, Component.translatable(this.getDescriptionId() + ".empty", string));
            return false;
        }
        CompoundTag compoundTag = itemStack.getOrCreateTagElement("DebugProperty");
        String string2 = compoundTag.getString(string);
        Property<?> property = stateDefinition.getProperty(string2);
        if (bl) {
            if (property == null) {
                property = collection.iterator().next();
            }
            BlockState blockState2 = DebugStickItem.cycleState(blockState, property, player.isSecondaryUseActive());
            levelAccessor.setBlock(blockPos, blockState2, 18);
            DebugStickItem.message(player, Component.translatable(this.getDescriptionId() + ".update", property.getName(), DebugStickItem.getNameHelper(blockState2, property)));
        } else {
            property = DebugStickItem.getRelative(collection, property, player.isSecondaryUseActive());
            String string3 = property.getName();
            compoundTag.putString(string, string3);
            DebugStickItem.message(player, Component.translatable(this.getDescriptionId() + ".select", string3, DebugStickItem.getNameHelper(blockState, property)));
        }
        return true;
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean bl) {
        return (BlockState)blockState.setValue(property, (Comparable)DebugStickItem.getRelative(property.getPossibleValues(), blockState.getValue(property), bl));
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

