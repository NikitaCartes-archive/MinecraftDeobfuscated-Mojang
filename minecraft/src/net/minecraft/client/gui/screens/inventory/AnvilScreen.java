package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class AnvilScreen extends AbstractContainerScreen<AnvilMenu> implements ContainerListener {
	private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
	private EditBox name;

	public AnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
		super(anvilMenu, inventory, component);
	}

	@Override
	protected void init() {
		super.init();
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, I18n.get("container.repair"));
		this.name.setCanLoseFocus(false);
		this.name.changeFocus(true);
		this.name.setTextColor(-1);
		this.name.setTextColorUneditable(-1);
		this.name.setBordered(false);
		this.name.setMaxLength(35);
		this.name.setResponder(this::onNameChanged);
		this.children.add(this.name);
		this.menu.addSlotListener(this);
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
		this.menu.removeSlotListener(this);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.player.closeContainer();
		}

		return !this.name.keyPressed(i, j, k) && !this.name.canConsumeInput() ? super.keyPressed(i, j, k) : true;
	}

	@Override
	protected void renderLabels(int i, int j) {
		RenderSystem.disableLighting();
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

		RenderSystem.enableLighting();
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
	public void render(int i, int j, float f) {
		this.renderBackground();
		super.render(i, j, f);
		this.renderTooltip(i, j);
		RenderSystem.disableLighting();
		RenderSystem.disableBlend();
		this.name.render(i, j, f);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(ANVIL_LOCATION);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		this.blit(k + 59, l + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
			this.blit(k + 99, l + 45, this.imageWidth, 0, 28, 21);
		}
	}

	@Override
	public void refreshContainer(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList) {
		this.slotChanged(abstractContainerMenu, 0, abstractContainerMenu.getSlot(0).getItem());
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
		if (i == 0) {
			this.name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
			this.name.setEditable(!itemStack.isEmpty());
		}
	}

	@Override
	public void setContainerData(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}
}
