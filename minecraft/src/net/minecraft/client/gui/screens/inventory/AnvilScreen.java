package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class AnvilScreen extends ItemCombinerScreen<AnvilMenu> {
	private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
	private static final Component TOO_EXPENSIVE_TEXT = new TranslatableComponent("container.repair.expensive");
	private EditBox name;

	public AnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
		super(anvilMenu, inventory, component, ANVIL_LOCATION);
		this.titleLabelX = 60;
	}

	@Override
	protected void subInit() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, new TranslatableComponent("container.repair"));
		this.name.setCanLoseFocus(false);
		this.name.setTextColor(-1);
		this.name.setTextColorUneditable(-1);
		this.name.setBordered(false);
		this.name.setMaxLength(35);
		this.name.setResponder(this::onNameChanged);
		this.children.add(this.name);
		this.setInitialFocus(this.name);
	}

	@Override
	public void resize(Minecraft minecraft, int i, int j) {
		String string = this.name.getValue();
		this.init(minecraft, i, j);
		this.name.setValue(string);
	}

	@Override
	public void removed() {
		super.removed();
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.player.closeContainer();
		}

		return !this.name.keyPressed(i, j, k) && !this.name.canConsumeInput() ? super.keyPressed(i, j, k) : true;
	}

	private void onNameChanged(String string) {
		if (!string.isEmpty()) {
			String string2 = string;
			Slot slot = this.menu.getSlot(0);
			if (slot != null && slot.hasItem() && !slot.getItem().hasCustomHoverName() && string.equals(slot.getItem().getHoverName().getString())) {
				string2 = "";
			}

			this.menu.setItemName(string2);
			this.minecraft.player.connection.send(new ServerboundRenameItemPacket(string2));
		}
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		RenderSystem.disableBlend();
		super.renderLabels(poseStack, i, j);
		int k = this.menu.getCost();
		if (k > 0) {
			int l = 8453920;
			Component component;
			if (k >= 40 && !this.minecraft.player.abilities.instabuild) {
				component = TOO_EXPENSIVE_TEXT;
				l = 16736352;
			} else if (!this.menu.getSlot(2).hasItem()) {
				component = null;
			} else {
				component = new TranslatableComponent("container.repair.cost", k);
				if (!this.menu.getSlot(2).mayPickup(this.inventory.player)) {
					l = 16736352;
				}
			}

			if (component != null) {
				int m = this.imageWidth - 8 - this.font.width(component) - 2;
				int n = 69;
				fill(poseStack, m - 2, 67, this.imageWidth - 8, 79, 1325400064);
				this.font.drawShadow(poseStack, component, (float)m, 69.0F, l);
			}
		}
	}

	@Override
	public void renderFg(PoseStack poseStack, int i, int j, float f) {
		this.name.render(poseStack, i, j, f);
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		if (i == 0) {
			this.name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
			this.name.setEditable(!itemStack.isEmpty());
			this.setFocused(this.name);
		}
	}
}
