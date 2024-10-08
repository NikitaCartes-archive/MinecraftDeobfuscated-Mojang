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
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

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

	private void setSlot(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay, boolean bl) {
		List<ItemStack> list = slotDisplay.resolveForStacks(contextMap);
		if (!list.isEmpty()) {
			this.ingredients.put(slot, new GhostSlots.GhostSlot(list, bl));
		}
	}

	protected void setInput(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay) {
		this.setSlot(slot, contextMap, slotDisplay, false);
	}

	protected void setResult(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay) {
		this.setSlot(slot, contextMap, slotDisplay, true);
	}

	public void render(GuiGraphics guiGraphics, Minecraft minecraft, boolean bl) {
		this.ingredients.forEach((slot, ghostSlot) -> {
			int i = slot.x;
			int j = slot.y;
			if (ghostSlot.isResultSlot && bl) {
				guiGraphics.fill(i - 4, j - 4, i + 20, j + 20, 822018048);
			} else {
				guiGraphics.fill(i, j, i + 16, j + 16, 822018048);
			}

			ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
			guiGraphics.renderFakeItem(itemStack, i, j);
			guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), i, j, i + 16, j + 16, 822083583);
			if (ghostSlot.isResultSlot) {
				guiGraphics.renderItemDecorations(minecraft.font, itemStack, i, j);
			}
		});
	}

	public void renderTooltip(GuiGraphics guiGraphics, Minecraft minecraft, int i, int j, @Nullable Slot slot) {
		if (slot != null) {
			GhostSlots.GhostSlot ghostSlot = this.ingredients.get(slot);
			if (ghostSlot != null) {
				ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
				guiGraphics.renderComponentTooltip(minecraft.font, Screen.getTooltipFromItem(minecraft, itemStack), i, j, itemStack.get(DataComponents.TOOLTIP_STYLE));
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
