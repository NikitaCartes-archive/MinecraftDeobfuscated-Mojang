package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class CraftPlanksTutorialStep implements TutorialStepInstance {
	private static final Component CRAFT_TITLE = new TranslatableComponent("tutorial.craft_planks.title");
	private static final Component CRAFT_DESCRIPTION = new TranslatableComponent("tutorial.craft_planks.description");
	private final Tutorial tutorial;
	private TutorialToast toast;
	private int timeWaiting;

	public CraftPlanksTutorialStep(Tutorial tutorial) {
		this.tutorial = tutorial;
	}

	@Override
	public void tick() {
		this.timeWaiting++;
		if (this.tutorial.getGameMode() != GameType.SURVIVAL) {
			this.tutorial.setStep(TutorialSteps.NONE);
		} else {
			if (this.timeWaiting == 1) {
				LocalPlayer localPlayer = this.tutorial.getMinecraft().player;
				if (localPlayer != null) {
					if (localPlayer.inventory.contains(ItemTags.PLANKS)) {
						this.tutorial.setStep(TutorialSteps.NONE);
						return;
					}

					if (hasCraftedPlanksPreviously(localPlayer, ItemTags.PLANKS)) {
						this.tutorial.setStep(TutorialSteps.NONE);
						return;
					}
				}
			}

			if (this.timeWaiting >= 1200 && this.toast == null) {
				this.toast = new TutorialToast(TutorialToast.Icons.WOODEN_PLANKS, CRAFT_TITLE, CRAFT_DESCRIPTION, false);
				this.tutorial.getMinecraft().getToasts().addToast(this.toast);
			}
		}
	}

	@Override
	public void clear() {
		if (this.toast != null) {
			this.toast.hide();
			this.toast = null;
		}
	}

	@Override
	public void onGetItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (ItemTags.PLANKS.contains(item)) {
			this.tutorial.setStep(TutorialSteps.NONE);
		}
	}

	public static boolean hasCraftedPlanksPreviously(LocalPlayer localPlayer, Tag<Item> tag) {
		for (Item item : tag.getValues()) {
			if (localPlayer.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0) {
				return true;
			}
		}

		return false;
	}
}
