package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class BundleTutorial {
	private final Tutorial tutorial;
	private final Options options;
	@Nullable
	private TutorialToast toast;

	public BundleTutorial(Tutorial tutorial, Options options) {
		this.tutorial = tutorial;
		this.options = options;
	}

	private void showToast() {
		if (this.toast != null) {
			this.tutorial.removeTimedToast(this.toast);
		}

		Component component = Component.translatable("tutorial.bundleInsert.title");
		Component component2 = Component.translatable("tutorial.bundleInsert.description");
		this.toast = new TutorialToast(TutorialToast.Icons.RIGHT_CLICK, component, component2, true);
		this.tutorial.addTimedToast(this.toast, 160);
	}

	private void clearToast() {
		if (this.toast != null) {
			this.tutorial.removeTimedToast(this.toast);
			this.toast = null;
		}

		if (!this.options.hideBundleTutorial) {
			this.options.hideBundleTutorial = true;
			this.options.save();
		}
	}

	public void onInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
		if (!this.options.hideBundleTutorial) {
			if (!itemStack.isEmpty() && itemStack2.is(Items.BUNDLE)) {
				if (clickAction == ClickAction.PRIMARY) {
					this.showToast();
				} else if (clickAction == ClickAction.SECONDARY) {
					this.clearToast();
				}
			} else if (itemStack.is(Items.BUNDLE) && !itemStack2.isEmpty() && clickAction == ClickAction.SECONDARY) {
				this.clearToast();
			}
		}
	}
}
