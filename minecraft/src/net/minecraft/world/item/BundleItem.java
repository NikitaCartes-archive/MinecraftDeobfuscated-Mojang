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
	public boolean overrideStackedOnOther(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction, Inventory inventory) {
		if (clickAction == ClickAction.SECONDARY) {
			add(itemStack, itemStack2);
			return true;
		} else {
			return super.overrideStackedOnOther(itemStack, itemStack2, clickAction, inventory);
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction, Inventory inventory) {
		if (clickAction == ClickAction.SECONDARY) {
			if (itemStack2.isEmpty()) {
				removeAll(itemStack, inventory);
			} else {
				add(itemStack, itemStack2);
			}

			return true;
		} else {
			return super.overrideOtherStackedOnMe(itemStack, itemStack2, clickAction, inventory);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		removeAll(itemStack, player.getInventory());
		return InteractionResultHolder.success(itemStack);
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

	private static void add(ItemStack itemStack, ItemStack itemStack2) {
		if (itemStack2.getItem().canFitInsideContainerItems()) {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			if (!compoundTag.contains("Items")) {
				compoundTag.put("Items", new ListTag());
			}

			int i = getContentWeight(itemStack);
			int j = getWeight(itemStack2);
			int k = Math.min(itemStack2.getCount(), (64 - i) / j);
			if (k != 0) {
				ListTag listTag = compoundTag.getList("Items", 10);
				Optional<CompoundTag> optional = getMatchingItem(itemStack2, listTag);
				if (optional.isPresent()) {
					CompoundTag compoundTag2 = (CompoundTag)optional.get();
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

	private static void removeAll(ItemStack itemStack, Inventory inventory) {
		getContents(itemStack).forEach(itemStackx -> {
			if (inventory.player instanceof ServerPlayer || inventory.player.isCreative()) {
				inventory.placeItemBackInInventory(itemStackx);
			}
		});
		itemStack.removeTagKey("Items");
	}

	private static Stream<ItemStack> getContents(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag == null) {
			return Stream.empty();
		} else {
			ListTag listTag = compoundTag.getList("Items", 10);
			return listTag.stream().map(tag -> ItemStack.of((CompoundTag)tag));
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		NonNullList<ItemStack> nonNullList = NonNullList.create();
		getContents(itemStack).forEach(nonNullList::add);
		return Optional.of(new BundleTooltip(nonNullList, getContentWeight(itemStack) < 64));
	}
}
