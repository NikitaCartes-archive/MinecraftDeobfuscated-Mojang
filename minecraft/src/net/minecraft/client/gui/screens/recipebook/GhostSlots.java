package net.minecraft.client.gui.screens.recipebook;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class GhostSlots {
	private final Reference2ObjectMap<Slot, GhostSlots.GhostSlot> ingredients = new Reference2ObjectArrayMap<>();
	private final SlotSelectTime slotSelectTime;

	public GhostSlots(SlotSelectTime slotSelectTime) {
		this.slotSelectTime = slotSelectTime;
	}

	public void clear() {
		this.ingredients.clear();
	}

	public void addResult(ItemStack itemStack, Slot slot) {
		this.ingredients.put(slot, new GhostSlots.GhostSlot(List.of(itemStack), true));
	}

	public void addIngredient(List<ItemStack> list, Slot slot) {
		this.ingredients.put(slot, new GhostSlots.GhostSlot(list, false));
	}

	public void render(GuiGraphics guiGraphics, Minecraft minecraft, int i, int j, boolean bl) {
		this.ingredients.forEach((slot, ghostSlot) -> {
			int k = slot.x + i;
			int l = slot.y + j;
			if (ghostSlot.isResultSlot && bl) {
				guiGraphics.fill(k - 4, l - 4, k + 20, l + 20, 822018048);
			} else {
				guiGraphics.fill(k, l, k + 16, l + 16, 822018048);
			}

			ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
			guiGraphics.renderFakeItem(itemStack, k, l);
			guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), k, l, k + 16, l + 16, 822083583);
			if (ghostSlot.isResultSlot) {
				guiGraphics.renderItemDecorations(minecraft.font, itemStack, k, l);
			}
		});
	}

	public void renderTooltip(GuiGraphics guiGraphics, Minecraft minecraft, int i, int j, @Nullable Slot slot) {
		if (slot != null) {
			GhostSlots.GhostSlot ghostSlot = this.ingredients.get(slot);
			if (ghostSlot != null) {
				ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
				guiGraphics.renderComponentTooltip(minecraft.font, Screen.getTooltipFromItem(minecraft, itemStack), i, j);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record GhostSlot(List<ItemStack> items, boolean isResultSlot) {

		public ItemStack getItem(int i) {
			int j = this.items.size();
			return j == 0 ? ItemStack.EMPTY : (ItemStack)this.items.get(i % j);
		}
	}
}
