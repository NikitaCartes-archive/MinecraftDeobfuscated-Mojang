package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.PotatoRefineryMenu;
import net.minecraft.world.inventory.Slot;

@Environment(EnvType.CLIENT)
public class PotatoRefineryScreen extends AbstractContainerScreen<PotatoRefineryMenu> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/potato_refinery.png");
	private static final ResourceLocation LIT_PROGRESS_SPRITE = new ResourceLocation("container/potato_refinery/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = new ResourceLocation("container/potato_refinery/burn_progress");
	private boolean widthTooNarrow;
	private final ResourceLocation texture;
	private final ResourceLocation litProgressSprite;
	private final ResourceLocation burnProgressSprite;

	public PotatoRefineryScreen(PotatoRefineryMenu potatoRefineryMenu, Inventory inventory, Component component) {
		super(potatoRefineryMenu, inventory, component);
		this.imageHeight += 20;
		this.inventoryLabelY += 20;
		this.texture = TEXTURE;
		this.litProgressSprite = LIT_PROGRESS_SPRITE;
		this.burnProgressSprite = BURN_PROGRESS_SPRITE;
	}

	@Override
	public void init() {
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void containerTick() {
		super.containerTick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.renderTooltip(guiGraphics, i, j);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
		int k = this.leftPos;
		int l = this.topPos;
		guiGraphics.blit(this.texture, k, l, 0, 0, this.imageWidth, this.imageHeight);
		if (this.menu.isLit()) {
			int m = 17;
			int n = 12;
			int o = Mth.ceil(this.menu.getLitProgress() * 11.0F) + 1;
			guiGraphics.blitSprite(this.litProgressSprite, 17, 12, 0, 12 - o, k + 51, l + 54 + 12 - o, 17, o);
		}

		int m = 46;
		int n = Mth.ceil(this.menu.getBurnProgress() * 46.0F);
		guiGraphics.blitSprite(this.burnProgressSprite, 46, 16, 0, 0, k + 69, l + 18, n, 16);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return super.mouseClicked(d, e, i);
	}

	@Override
	protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
		super.slotClicked(slot, i, j, clickType);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		return super.keyPressed(i, j, k);
	}

	@Override
	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		return d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
	}

	@Override
	public boolean charTyped(char c, int i) {
		return super.charTyped(c, i);
	}
}
