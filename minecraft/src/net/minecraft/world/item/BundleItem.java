package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
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
		return i != 0 && i != 64;
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
				Optional<Tag> optional = listTag.stream()
					.filter(tag -> tag instanceof CompoundTag && ItemStack.isSameItemSameTags(ItemStack.of((CompoundTag)tag), itemStack2))
					.findFirst();
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

	private static int getWeight(ItemStack itemStack) {
		return itemStack.is(Items.BUNDLE) ? 4 + getContentWeight(itemStack) : 64 / itemStack.getMaxStackSize();
	}

	private static int getContentWeight(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (!compoundTag.contains("Items")) {
			return 0;
		} else {
			ListTag listTag = compoundTag.getList("Items", 10);
			return listTag.stream().map(tag -> ItemStack.of((CompoundTag)tag)).mapToInt(itemStackx -> getWeight(itemStackx) * itemStackx.getCount()).sum();
		}
	}

	private static void removeAll(ItemStack itemStack, Inventory inventory) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (compoundTag.contains("Items")) {
			ListTag listTag = compoundTag.getList("Items", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				ItemStack itemStack2 = ItemStack.of(compoundTag2);
				if (inventory.player instanceof ServerPlayer || inventory.player.isCreative()) {
					inventory.placeItemBackInInventory(itemStack2);
				}
			}

			itemStack.removeTagKey("Items");
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (compoundTag.contains("Items", 9)) {
			ListTag listTag = compoundTag.getList("Items", 10);
			int i = 0;
			int j = 0;

			for (Tag tag : listTag) {
				ItemStack itemStack2 = ItemStack.of((CompoundTag)tag);
				if (!itemStack2.isEmpty()) {
					j++;
					if (i <= 8) {
						i++;
						MutableComponent mutableComponent = itemStack2.getHoverName().copy();
						mutableComponent.append(" x").append(String.valueOf(itemStack2.getCount()));
						list.add(mutableComponent);
					}
				}
			}

			if (j - i > 0) {
				list.add(new TranslatableComponent("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
			}
		}
	}
}
