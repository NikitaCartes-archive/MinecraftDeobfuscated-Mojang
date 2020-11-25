package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

public class BundleItem extends Item {
	private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

	public BundleItem(Item.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	public static float getFullnessDisplay(ItemStack itemStack) {
		return (float)getContentWeight(itemStack) / 64.0F;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Inventory inventory) {
		if (clickAction != ClickAction.SECONDARY) {
			return false;
		} else {
			ItemStack itemStack2 = slot.getItem();
			if (itemStack2.isEmpty()) {
				removeOne(itemStack).ifPresent(itemStack2x -> add(itemStack, slot.safeInsert(itemStack2x)));
			} else if (itemStack2.getItem().canFitInsideContainerItems()) {
				int i = (64 - getContentWeight(itemStack)) / getWeight(itemStack2);
				add(itemStack, slot.safeTake(itemStack2.getCount(), i, inventory.player));
			}

			return true;
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Inventory inventory) {
		if (clickAction == ClickAction.SECONDARY && slot.allowModification(inventory.player)) {
			if (itemStack2.isEmpty()) {
				removeOne(itemStack).ifPresent(inventory::setCarried);
			} else {
				itemStack2.shrink(add(itemStack, itemStack2));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		return dropContents(itemStack, player) ? InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide()) : InteractionResultHolder.fail(itemStack);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isBarVisible(ItemStack itemStack) {
		int i = getContentWeight(itemStack);
		return i > 0 && i < 64;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getBarWidth(ItemStack itemStack) {
		return 13 * getContentWeight(itemStack) / 64 + 1;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getBarColor(ItemStack itemStack) {
		return BAR_COLOR;
	}

	private static int add(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack2.isEmpty() && itemStack2.getItem().canFitInsideContainerItems()) {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			if (!compoundTag.contains("Items")) {
				compoundTag.put("Items", new ListTag());
			}

			int i = getContentWeight(itemStack);
			int j = getWeight(itemStack2);
			int k = Math.min(itemStack2.getCount(), (64 - i) / j);
			if (k == 0) {
				return 0;
			} else {
				ListTag listTag = compoundTag.getList("Items", 10);
				Optional<CompoundTag> optional = getMatchingItem(itemStack2, listTag);
				if (optional.isPresent()) {
					CompoundTag compoundTag2 = (CompoundTag)optional.get();
					ItemStack itemStack3 = ItemStack.of(compoundTag2);
					itemStack3.grow(k);
					itemStack3.save(compoundTag2);
					listTag.remove(compoundTag2);
					listTag.add(0, compoundTag2);
				} else {
					ItemStack itemStack4 = itemStack2.copy();
					itemStack4.setCount(k);
					CompoundTag compoundTag3 = new CompoundTag();
					itemStack4.save(compoundTag3);
					listTag.add(0, compoundTag3);
				}

				return k;
			}
		} else {
			return 0;
		}
	}

	private static Optional<CompoundTag> getMatchingItem(ItemStack itemStack, ListTag listTag) {
		return itemStack.is(Items.BUNDLE)
			? Optional.empty()
			: listTag.stream()
				.filter(CompoundTag.class::isInstance)
				.map(CompoundTag.class::cast)
				.filter(compoundTag -> ItemStack.isSameItemSameTags(ItemStack.of(compoundTag), itemStack))
				.findFirst();
	}

	private static int getWeight(ItemStack itemStack) {
		return itemStack.is(Items.BUNDLE) ? 4 + getContentWeight(itemStack) : 64 / itemStack.getMaxStackSize();
	}

	private static int getContentWeight(ItemStack itemStack) {
		return getContents(itemStack).mapToInt(itemStackx -> getWeight(itemStackx) * itemStackx.getCount()).sum();
	}

	private static Optional<ItemStack> removeOne(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (!compoundTag.contains("Items")) {
			return Optional.empty();
		} else {
			ListTag listTag = compoundTag.getList("Items", 10);
			if (listTag.isEmpty()) {
				return Optional.empty();
			} else {
				int i = 0;
				CompoundTag compoundTag2 = listTag.getCompound(0);
				ItemStack itemStack2 = ItemStack.of(compoundTag2);
				listTag.remove(0);
				return Optional.of(itemStack2);
			}
		}
	}

	private static boolean dropContents(ItemStack itemStack, Player player) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (!compoundTag.contains("Items")) {
			return false;
		} else {
			if (player instanceof ServerPlayer) {
				ListTag listTag = compoundTag.getList("Items", 10);

				for (int i = 0; i < listTag.size(); i++) {
					CompoundTag compoundTag2 = listTag.getCompound(i);
					ItemStack itemStack2 = ItemStack.of(compoundTag2);
					player.drop(itemStack2, true);
				}
			}

			itemStack.removeTagKey("Items");
			return true;
		}
	}

	private static Stream<ItemStack> getContents(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag == null) {
			return Stream.empty();
		} else {
			ListTag listTag = compoundTag.getList("Items", 10);
			return listTag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		NonNullList<ItemStack> nonNullList = NonNullList.create();
		getContents(itemStack).forEach(nonNullList::add);
		return Optional.of(new BundleTooltip(nonNullList, getContentWeight(itemStack) < 64));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
		if (tooltipFlag.isAdvanced()) {
			list.add(new TranslatableComponent("item.minecraft.bundle.fullness", getContentWeight(itemStack), 64).withStyle(ChatFormatting.GRAY));
		}
	}
}
