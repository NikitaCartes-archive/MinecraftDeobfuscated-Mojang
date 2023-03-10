/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WrittenBookItem
extends Item {
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int PAGE_EDIT_LENGTH = 1024;
    public static final int PAGE_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PAGES = 100;
    public static final int MAX_GENERATION = 2;
    public static final String TAG_TITLE = "title";
    public static final String TAG_FILTERED_TITLE = "filtered_title";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_PAGES = "pages";
    public static final String TAG_FILTERED_PAGES = "filtered_pages";
    public static final String TAG_GENERATION = "generation";
    public static final String TAG_RESOLVED = "resolved";

    public WrittenBookItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
        if (!WritableBookItem.makeSureTagIsValid(compoundTag)) {
            return false;
        }
        if (!compoundTag.contains(TAG_TITLE, 8)) {
            return false;
        }
        String string = compoundTag.getString(TAG_TITLE);
        if (string.length() > 32) {
            return false;
        }
        return compoundTag.contains(TAG_AUTHOR, 8);
    }

    public static int getGeneration(ItemStack itemStack) {
        return itemStack.getTag().getInt(TAG_GENERATION);
    }

    public static int getPageCount(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null ? compoundTag.getList(TAG_PAGES, 8).size() : 0;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        String string;
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && !StringUtil.isNullOrEmpty(string = compoundTag.getString(TAG_TITLE))) {
            return Component.literal(string);
        }
        return super.getName(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemStack.hasTag()) {
            CompoundTag compoundTag = itemStack.getTag();
            String string = compoundTag.getString(TAG_AUTHOR);
            if (!StringUtil.isNullOrEmpty(string)) {
                list.add(Component.translatable("book.byAuthor", string).withStyle(ChatFormatting.GRAY));
            }
            list.add(Component.translatable("book.generation." + compoundTag.getInt(TAG_GENERATION)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(useOnContext.getPlayer(), level, blockPos, blockState, useOnContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        player.openItemGui(itemStack, interactionHand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public static boolean resolveBookComponents(ItemStack itemStack, @Nullable CommandSourceStack commandSourceStack, @Nullable Player player) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null || compoundTag.getBoolean(TAG_RESOLVED)) {
            return false;
        }
        compoundTag.putBoolean(TAG_RESOLVED, true);
        if (!WrittenBookItem.makeSureTagIsValid(compoundTag)) {
            return false;
        }
        ListTag listTag = compoundTag.getList(TAG_PAGES, 8);
        ListTag listTag2 = new ListTag();
        for (int i = 0; i < listTag.size(); ++i) {
            String string = WrittenBookItem.resolvePage(commandSourceStack, player, listTag.getString(i));
            if (string.length() > Short.MAX_VALUE) {
                return false;
            }
            listTag2.add(i, StringTag.valueOf(string));
        }
        if (compoundTag.contains(TAG_FILTERED_PAGES, 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound(TAG_FILTERED_PAGES);
            CompoundTag compoundTag3 = new CompoundTag();
            for (String string2 : compoundTag2.getAllKeys()) {
                String string3 = WrittenBookItem.resolvePage(commandSourceStack, player, compoundTag2.getString(string2));
                if (string3.length() > Short.MAX_VALUE) {
                    return false;
                }
                compoundTag3.putString(string2, string3);
            }
            compoundTag.put(TAG_FILTERED_PAGES, compoundTag3);
        }
        compoundTag.put(TAG_PAGES, listTag2);
        return true;
    }

    private static String resolvePage(@Nullable CommandSourceStack commandSourceStack, @Nullable Player player, String string) {
        MutableComponent component;
        try {
            component = Component.Serializer.fromJsonLenient(string);
            component = ComponentUtils.updateForEntity(commandSourceStack, component, (Entity)player, 0);
        } catch (Exception exception) {
            component = Component.literal(string);
        }
        return Component.Serializer.toJson(component);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}

