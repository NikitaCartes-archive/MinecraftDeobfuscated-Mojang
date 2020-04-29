/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WrittenBookItem
extends Item {
    public WrittenBookItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
        if (!WritableBookItem.makeSureTagIsValid(compoundTag)) {
            return false;
        }
        if (!compoundTag.contains("title", 8)) {
            return false;
        }
        String string = compoundTag.getString("title");
        if (string.length() > 32) {
            return false;
        }
        return compoundTag.contains("author", 8);
    }

    public static int getGeneration(ItemStack itemStack) {
        return itemStack.getTag().getInt("generation");
    }

    public static int getPageCount(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null ? compoundTag.getList("pages", 8).size() : 0;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        CompoundTag compoundTag;
        String string;
        if (itemStack.hasTag() && !StringUtil.isNullOrEmpty(string = (compoundTag = itemStack.getTag()).getString("title"))) {
            return new TextComponent(string);
        }
        return super.getName(itemStack);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemStack.hasTag()) {
            CompoundTag compoundTag = itemStack.getTag();
            String string = compoundTag.getString("author");
            if (!StringUtil.isNullOrEmpty(string)) {
                list.add(new TranslatableComponent("book.byAuthor", string).withStyle(ChatFormatting.GRAY));
            }
            list.add(new TranslatableComponent("book.generation." + compoundTag.getInt("generation")).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(level, blockPos, blockState, useOnContext.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        player.openItemGui(itemStack, interactionHand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(itemStack);
    }

    public static boolean resolveBookComponents(ItemStack itemStack, @Nullable CommandSourceStack commandSourceStack, @Nullable Player player) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || compoundTag.getBoolean("resolved")) {
            return false;
        }
        compoundTag.putBoolean("resolved", true);
        if (!WrittenBookItem.makeSureTagIsValid(compoundTag)) {
            return false;
        }
        ListTag listTag = compoundTag.getList("pages", 8);
        for (int i = 0; i < listTag.size(); ++i) {
            MutableComponent component;
            String string = listTag.getString(i);
            try {
                component = Component.Serializer.fromJsonLenient(string);
                component = ComponentUtils.updateForEntity(commandSourceStack, component, player, 0);
            } catch (Exception exception) {
                component = new TextComponent(string);
            }
            listTag.set(i, StringTag.valueOf(Component.Serializer.toJson(component)));
        }
        compoundTag.put("pages", listTag);
        return true;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}

