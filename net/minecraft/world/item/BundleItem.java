/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BundleItem
extends Item {
    private static final int BAR_COLOR = Mth.color(0.4f, 0.4f, 1.0f);

    public BundleItem(Item.Properties properties) {
        super(properties);
    }

    @Environment(value=EnvType.CLIENT)
    public static float getFullnessDisplay(ItemStack itemStack) {
        return (float)BundleItem.getContentWeight(itemStack) / 64.0f;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction, Inventory inventory) {
        if (clickAction == ClickAction.SECONDARY) {
            BundleItem.add(itemStack, itemStack2);
            return true;
        }
        return super.overrideStackedOnOther(itemStack, itemStack2, clickAction, inventory);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction, Inventory inventory) {
        if (clickAction == ClickAction.SECONDARY) {
            if (itemStack2.isEmpty()) {
                BundleItem.removeAll(itemStack, inventory);
            } else {
                BundleItem.add(itemStack, itemStack2);
            }
            return true;
        }
        return super.overrideOtherStackedOnMe(itemStack, itemStack2, clickAction, inventory);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BundleItem.removeAll(itemStack, player.getInventory());
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isBarVisible(ItemStack itemStack) {
        int i = BundleItem.getContentWeight(itemStack);
        return i > 0 && i < 64;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getBarWidth(ItemStack itemStack) {
        return 13 * BundleItem.getContentWeight(itemStack) / 64 + 1;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getBarColor(ItemStack itemStack) {
        return BAR_COLOR;
    }

    private static void add(ItemStack itemStack, ItemStack itemStack2) {
        if (!itemStack2.getItem().canFitInsideContainerItems()) {
            return;
        }
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (!compoundTag.contains("Items")) {
            compoundTag.put("Items", new ListTag());
        }
        int i = BundleItem.getContentWeight(itemStack);
        int j = BundleItem.getWeight(itemStack2);
        int k = Math.min(itemStack2.getCount(), (64 - i) / j);
        if (k == 0) {
            return;
        }
        ListTag listTag = compoundTag.getList("Items", 10);
        Optional<CompoundTag> optional = BundleItem.getMatchingItem(itemStack2, listTag);
        if (optional.isPresent()) {
            CompoundTag compoundTag2 = optional.get();
            ItemStack itemStack3 = ItemStack.of(compoundTag2);
            itemStack3.grow(k);
            itemStack3.save(compoundTag2);
        } else {
            ItemStack itemStack4 = itemStack2.copy();
            itemStack4.setCount(k);
            CompoundTag compoundTag3 = new CompoundTag();
            itemStack4.save(compoundTag3);
            listTag.add(compoundTag3);
        }
        itemStack2.shrink(k);
    }

    private static Optional<CompoundTag> getMatchingItem(ItemStack itemStack, ListTag listTag) {
        if (itemStack.is(Items.BUNDLE)) {
            return Optional.empty();
        }
        return listTag.stream().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).filter(compoundTag -> ItemStack.isSameItemSameTags(ItemStack.of(compoundTag), itemStack)).findFirst();
    }

    private static int getWeight(ItemStack itemStack) {
        if (itemStack.is(Items.BUNDLE)) {
            return 4 + BundleItem.getContentWeight(itemStack);
        }
        return 64 / itemStack.getMaxStackSize();
    }

    private static int getContentWeight(ItemStack itemStack2) {
        return BundleItem.getContents(itemStack2).mapToInt(itemStack -> BundleItem.getWeight(itemStack) * itemStack.getCount()).sum();
    }

    private static void removeAll(ItemStack itemStack2, Inventory inventory) {
        BundleItem.getContents(itemStack2).forEach(itemStack -> {
            if (inventory.player instanceof ServerPlayer || inventory.player.isCreative()) {
                inventory.placeItemBackInInventory((ItemStack)itemStack);
            }
        });
        itemStack2.removeTagKey("Items");
    }

    private static Stream<ItemStack> getContents(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag == null) {
            return Stream.empty();
        }
        ListTag listTag = compoundTag.getList("Items", 10);
        return listTag.stream().map(tag -> ItemStack.of((CompoundTag)tag));
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        BundleItem.getContents(itemStack).forEach(nonNullList::add);
        return Optional.of(new BundleTooltip(nonNullList, BundleItem.getContentWeight(itemStack) < 64));
    }
}

