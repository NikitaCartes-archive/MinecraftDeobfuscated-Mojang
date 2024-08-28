package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem extends Item {
	public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
	public static final int MAX_SHOWN_GRID_ITEMS_Y = 3;
	public static final int MAX_SHOWN_GRID_ITEMS = 12;
	public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
	private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
	private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);
	private final String openBundleModelFrontLocation;
	private final String openBundleModelBackLocation;

	public BundleItem(String string, String string2, Item.Properties properties) {
		super(properties);
		this.openBundleModelFrontLocation = string;
		this.openBundleModelBackLocation = string2;
	}

	public static float getFullnessDisplay(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.weight().floatValue();
	}

	public String getOpenBundleModelFrontLocation() {
		return this.openBundleModelFrontLocation;
	}

	public String getOpenBundleModelBackLocation() {
		return this.openBundleModelBackLocation;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents == null) {
			return false;
		} else {
			ItemStack itemStack2 = slot.getItem();
			BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
			if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
				if (mutable.tryTransfer(slot, player) > 0) {
					this.playInsertSound(player);
				} else {
					playInsertFailSound(player);
				}

				itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
				return true;
			} else if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
				ItemStack itemStack3 = mutable.removeOne();
				if (itemStack3 != null) {
					ItemStack itemStack4 = slot.safeInsert(itemStack3);
					if (itemStack4.getCount() > 0) {
						mutable.tryInsert(itemStack4);
					} else {
						this.playRemoveOneSound(player);
					}
				}

				itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if (clickAction == ClickAction.PRIMARY && itemStack2.isEmpty()) {
			toggleSelectedItem(itemStack, -1);
			return false;
		} else {
			BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundleContents == null) {
				return false;
			} else {
				BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
				if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
					if (slot.allowModification(player) && mutable.tryInsert(itemStack2) > 0) {
						this.playInsertSound(player);
					} else {
						playInsertFailSound(player);
					}

					itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
					return true;
				} else if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
					if (slot.allowModification(player)) {
						ItemStack itemStack3 = mutable.removeOne();
						if (itemStack3 != null) {
							this.playRemoveOneSound(player);
							slotAccess.set(itemStack3);
						}
					}

					itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
					return true;
				} else {
					return false;
				}
			}
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (dropContents(itemStack, player)) {
			this.playDropContentsSound(player);
			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	public boolean isBarVisible(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.weight().compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return Math.min(1 + Mth.mulAndTruncate(bundleContents.weight(), 12), 13);
	}

	@Override
	public int getBarColor(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
	}

	public static void toggleSelectedItem(ItemStack itemStack, int i) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
			mutable.setSelectedItem(i);
			itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
		}
	}

	public static boolean hasSelectedItem(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.getSelectedItem() != -1;
	}

	public static int getSelectedItem(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.getSelectedItem();
	}

	public static ItemStack getSelectedItemStack(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.getItemUnsafe(bundleContents.getSelectedItem());
	}

	public static int getNumberOfItemsToShow(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.getNumberOfItemsToShow();
	}

	private static boolean dropContents(ItemStack itemStack, Player player) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null && !bundleContents.isEmpty()) {
			itemStack.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			if (player instanceof ServerPlayer) {
				bundleContents.itemsCopy().forEach(itemStackx -> player.drop(itemStackx, true));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		return !itemStack.has(DataComponents.HIDE_TOOLTIP) && !itemStack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
			? Optional.ofNullable(itemStack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new)
			: Optional.empty();
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		BundleContents bundleContents = itemEntity.getItem().get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			itemEntity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			ItemUtils.onContainerDestroyed(itemEntity, bundleContents.itemsCopy());
		}
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertFailSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}
}
