package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

public class BundleItem extends Item {
	private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

	public BundleItem(Item.Properties properties) {
		super(properties);
	}

	public static float getFullnessDisplay(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return (float)bundleContents.weight() / 64.0F;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
		if (clickAction != ClickAction.SECONDARY) {
			return false;
		} else {
			BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundleContents == null) {
				return false;
			} else {
				ItemStack itemStack2 = slot.getItem();
				BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
				if (itemStack2.isEmpty()) {
					this.playRemoveOneSound(player);
					ItemStack itemStack3 = mutable.removeOne();
					if (itemStack3 != null) {
						ItemStack itemStack4 = slot.safeInsert(itemStack3);
						mutable.tryInsert(itemStack4);
					}
				} else if (itemStack2.getItem().canFitInsideContainerItems()) {
					int i = mutable.tryTransfer(slot, player);
					if (i > 0) {
						this.playInsertSound(player);
					}
				}

				itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
				return true;
			}
		}
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
		if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
			BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundleContents == null) {
				return false;
			} else {
				BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
				if (itemStack2.isEmpty()) {
					ItemStack itemStack3 = mutable.removeOne();
					if (itemStack3 != null) {
						this.playRemoveOneSound(player);
						slotAccess.set(itemStack3);
					}
				} else {
					int i = mutable.tryInsert(itemStack2);
					if (i > 0) {
						this.playInsertSound(player);
					}
				}

				itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
				return true;
			}
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
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return bundleContents.weight() > 0;
	}

	@Override
	public int getBarWidth(ItemStack itemStack) {
		BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return Math.min(1 + 12 * bundleContents.weight() / 64, 13);
	}

	@Override
	public int getBarColor(ItemStack itemStack) {
		return BAR_COLOR;
	}

	private static boolean dropContents(ItemStack itemStack, Player player) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null && !bundleContents.isEmpty()) {
			itemStack.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			if (player instanceof ServerPlayer) {
				bundleContents.items().forEach(itemStackx -> player.drop(itemStackx, true));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
		return Optional.ofNullable(itemStack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
		BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			list.add(Component.translatable("item.minecraft.bundle.fullness", bundleContents.weight(), 64).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void onDestroyed(ItemEntity itemEntity) {
		BundleContents bundleContents = itemEntity.getItem().get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			itemEntity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			ItemUtils.onContainerDestroyed(itemEntity, bundleContents.items());
		}
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}
}
