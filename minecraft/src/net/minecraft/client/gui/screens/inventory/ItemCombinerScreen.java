package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
	private final ResourceLocation menuResource;

	public ItemCombinerScreen(T itemCombinerMenu, Inventory inventory, Component component, ResourceLocation resourceLocation) {
		super(itemCombinerMenu, inventory, component);
		this.menuResource = resourceLocation;
	}

	protected void subInit() {
	}

	@Override
	protected void init() {
		super.init();
		this.subInit();
		this.menu.addSlotListener(this);
	}

	@Override
	public void removed() {
		super.removed();
		this.menu.removeSlotListener(this);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		RenderSystem.disableBlend();
		this.renderFg(poseStack, i, j, f);
		this.renderTooltip(poseStack, i, j);
	}

	protected void renderFg(PoseStack poseStack, int i, int j, float f) {
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.menuResource);
		this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		this.renderErrorIcon(poseStack, this.leftPos, this.topPos);
	}

	protected abstract void renderErrorIcon(PoseStack poseStack, int i, int j);

	@Override
	public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
	}

	@Override
	public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
	}
}
