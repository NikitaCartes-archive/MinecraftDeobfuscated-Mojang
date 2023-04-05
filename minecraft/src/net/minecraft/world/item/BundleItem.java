package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

public class BundleItem extends Item {
	private static final String TAG_ITEMS = "Items";
	public static final int MAX_WEIGHT = 64;
	private static final int BUNDLE_IN_BUNDLE_WEIGHT = 4;
	private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

	public BundleItem(Item.Properties properties) {
		super(properties);
	}

	public static float getFullnessDisplay(ItemStack itemStack) {
		return (float)getContentWeight(itemStack) / 64.0F;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
		if (clickAction != ClickAction.SECONDARY) {
			return false;
		} else {
			ItemStack itemStack2 = slot.getItem();
			if (itemStack2.isEmpty()) {
				this.playRemoveOneSound(player);
				removeOne(itemStack).ifPresent(itemStack2x -> add(itemStack, slot.safeInsert(itemStack2x)));
			} else if (itemStack2.getItem().canFitInsideContainerItems()) {
				int i = (64 - getContentWeight(itemStack)) / getWeight(itemStack2);
				int j = add(itemStack, slot.safeTake(itemStack2.getCount(), i, player));
				if (j > 0) {
					this.playInsertSound(player);
				}
			}

			return true;
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
			if (itemStack2.isEmpty()) {
				removeOne(itemStack).ifPresent(itemStackx -> {
					this.playRemoveOneSound(player);
					slotAccess.set(itemStackx);
				});
			} else {
				int i = add(itemStack, itemStack2);
				if (i > 0) {
					this.playInsertSound(player);
					itemStack2.shrink(i);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (dropContents(itemStack, player)) {
			this.playDropContentsSound(player);
			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	@Override
	public boolean isBarVisible(ItemStack itemStack) {
		return getContentWeight(itemStack) > 0;
	}

	@Override
	public int getBarWidth(ItemStack itemStack) {
		return Math.min(1 + 12 * getContentWeight(itemStack) / 64, 13);
	}

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
					ItemStack itemStack4 = itemStack2.copyWithCount(k);
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
		if (itemStack.is(Items.BUNDLE)) {
			return 4 + getContentWeight(itemStack);
		} else {
			if ((itemStack.is(Items.BEEHIVE) || itemStack.is(Items.BEE_NEST)) && itemStack.hasTag()) {
				CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
				if (compoundTag != null && !compoundTag.getList("Bees", 10).isEmpty()) {
					return 64;
				}
			}

			return 64 / itemStack.getMaxStackSize();
		}
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
				if (listTag.isEmpty()) {
					itemStack.removeTagKey("Items");
				}

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

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		NonNullList<ItemStack> nonNullList = NonNullList.create();
		getContents(itemStack).forEach(nonNullList::add);
		return Optional.of(new BundleTooltip(nonNullList, getContentWeight(itemStack)));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(itemStack), 64).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		ItemUtils.onContainerDestroyed(itemEntity, getContents(itemEntity.getItem()));
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}
}
