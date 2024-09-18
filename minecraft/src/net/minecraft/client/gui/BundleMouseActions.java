package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class BundleMouseActions implements ItemSlotMouseAction {
	private final Minecraft minecraft;
	private final ScrollWheelHandler scrollWheelHandler;

	public BundleMouseActions(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.scrollWheelHandler = new ScrollWheelHandler();
	}

	@Override
	public boolean matches(Slot slot) {
		return slot.getItem().is(ItemTags.BUNDLES);
	}

	@Override
	public boolean onMouseScrolled(double d, double e, int i, ItemStack itemStack) {
		int j = BundleItem.getNumberOfItemsToShow(itemStack);
		if (j == 0) {
			return false;
		} else {
			Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(d, e);
			int k = vector2i.y == 0 ? -vector2i.x : vector2i.y;
			if (k != 0) {
				int l = BundleItem.getSelectedItem(itemStack);
				l = ScrollWheelHandler.getNextScrollWheelSelection((double)k, l, j);
				this.setSelectedBundleItem(itemStack, i, l);
			}

			return true;
		}
	}

	@Override
	public void onStopHovering(Slot slot) {
		this.unselectedBundleItem(slot.getItem(), slot.index);
	}

	@Override
	public void onSlotClicked(Slot slot, ClickType clickType) {
		if (clickType == ClickType.QUICK_MOVE) {
			this.unselectedBundleItem(slot.getItem(), slot.index);
		}
	}

	private void setSelectedBundleItem(ItemStack itemStack, int i, int j) {
		if (this.minecraft.getConnection() != null && j < BundleItem.getNumberOfItemsToShow(itemStack)) {
			ClientPacketListener clientPacketListener = this.minecraft.getConnection();
			BundleItem.toggleSelectedItem(itemStack, j);
			clientPacketListener.send(new ServerboundSelectBundleItemPacket(i, j));
		}
	}

	public void unselectedBundleItem(ItemStack itemStack, int i) {
		this.setSelectedBundleItem(itemStack, i, -1);
	}
}
