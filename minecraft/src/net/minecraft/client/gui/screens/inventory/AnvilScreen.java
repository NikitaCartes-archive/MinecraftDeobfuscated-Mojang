package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
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
	private EditBox name;

	public AnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
		super(anvilMenu, inventory, component, ANVIL_LOCATION);
	}

	@Override
	protected void subInit() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, I18n.get("container.repair"));
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
	protected void renderLabels(int i, int j) {
		RenderSystem.disableBlend();
		this.font.draw(this.title.getColoredString(), 60.0F, 6.0F, 4210752);
		int k = this.menu.getCost();
		if (k > 0) {
			int l = 8453920;
			boolean bl = true;
			String string = I18n.get("container.repair.cost", k);
			if (k >= 40 && !this.minecraft.player.abilities.instabuild) {
				string = I18n.get("container.repair.expensive");
				l = 16736352;
			} else if (!this.menu.getSlot(2).hasItem()) {
				bl = false;
			} else if (!this.menu.getSlot(2).mayPickup(this.inventory.player)) {
				l = 16736352;
			}

			if (bl) {
				int m = this.imageWidth - 8 - this.font.width(string) - 2;
				int n = 69;
				fill(m - 2, 67, this.imageWidth - 8, 79, 1325400064);
				this.font.drawShadow(string, (float)m, 69.0F, l);
			}
		}
	}

	@Override
	public void renderFg(int i, int j, float f) {
		this.name.render(i, j, f);
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
