package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.StonecutterRecipe;

@Environment(EnvType.CLIENT)
public class StonecutterScreen extends AbstractContainerScreen<StonecutterMenu> {
	private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
	private float scrollOffs;
	private boolean scrolling;
	private int startIndex;
	private boolean displayRecipes;

	public StonecutterScreen(StonecutterMenu stonecutterMenu, Inventory inventory, Component component) {
		super(stonecutterMenu, inventory, component);
		stonecutterMenu.registerUpdateListener(this::containerChanged);
	}

	@Override
	public void render(int i, int j, float f) {
		super.render(i, j, f);
		this.renderTooltip(i, j);
	}

	@Override
	protected void renderLabels(int i, int j) {
		this.font.draw(this.title.getColoredString(), 8.0F, 4.0F, 4210752);
		this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 94), 4210752);
	}

	@Override
	protected void renderBg(float f, int i, int j) {
		this.renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(BG_LOCATION);
		int k = this.leftPos;
		int l = this.topPos;
		this.blit(k, l, 0, 0, this.imageWidth, this.imageHeight);
		int m = (int)(41.0F * this.scrollOffs);
		this.blit(k + 119, l + 15 + m, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
		int n = this.leftPos + 52;
		int o = this.topPos + 14;
		int p = this.startIndex + 12;
		this.renderButtons(i, j, n, o, p);
		this.renderRecipes(n, o, p);
	}

	@Override
	protected void renderTooltip(int i, int j) {
		super.renderTooltip(i, j);
		if (this.displayRecipes) {
			int k = this.leftPos + 52;
			int l = this.topPos + 14;
			int m = this.startIndex + 12;
			List<StonecutterRecipe> list = this.menu.getRecipes();

			for (int n = this.startIndex; n < m && n < this.menu.getNumRecipes(); n++) {
				int o = n - this.startIndex;
				int p = k + o % 4 * 16;
				int q = l + o / 4 * 18 + 2;
				if (i >= p && i < p + 16 && j >= q && j < q + 18) {
					this.renderTooltip(((StonecutterRecipe)list.get(n)).getResultItem(), i, j);
				}
			}
		}
	}

	private void renderButtons(int i, int j, int k, int l, int m) {
		for (int n = this.startIndex; n < m && n < this.menu.getNumRecipes(); n++) {
			int o = n - this.startIndex;
			int p = k + o % 4 * 16;
			int q = o / 4;
			int r = l + q * 18 + 2;
			int s = this.imageHeight;
			if (n == this.menu.getSelectedRecipeIndex()) {
				s += 18;
			} else if (i >= p && j >= r && i < p + 16 && j < r + 18) {
				s += 36;
			}

			this.blit(p, r - 1, 0, s, 16, 18);
		}
	}

	private void renderRecipes(int i, int j, int k) {
		List<StonecutterRecipe> list = this.menu.getRecipes();

		for (int l = this.startIndex; l < k && l < this.menu.getNumRecipes(); l++) {
			int m = l - this.startIndex;
			int n = i + m % 4 * 16;
			int o = m / 4;
			int p = j + o * 18 + 2;
			this.minecraft.getItemRenderer().renderAndDecorateItem(((StonecutterRecipe)list.get(l)).getResultItem(), n, p);
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		this.scrolling = false;
		if (this.displayRecipes) {
			int j = this.leftPos + 52;
			int k = this.topPos + 14;
			int l = this.startIndex + 12;

			for (int m = this.startIndex; m < l; m++) {
				int n = m - this.startIndex;
				double f = d - (double)(j + n % 4 * 16);
				double g = e - (double)(k + n / 4 * 18);
				if (f >= 0.0 && g >= 0.0 && f < 16.0 && g < 18.0 && this.menu.clickMenuButton(this.minecraft.player, m)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, m);
					return true;
				}
			}

			j = this.leftPos + 119;
			k = this.topPos + 9;
			if (d >= (double)j && d < (double)(j + 12) && e >= (double)k && e < (double)(k + 54)) {
				this.scrolling = true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (this.scrolling && this.isScrollBarActive()) {
			int j = this.topPos + 14;
			int k = j + 54;
			this.scrollOffs = ((float)e - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
			return true;
		} else {
			return super.mouseDragged(d, e, i, f, g);
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		if (this.isScrollBarActive()) {
			int i = this.getOffscreenRows();
			this.scrollOffs = (float)((double)this.scrollOffs - f / (double)i);
			this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
			this.startIndex = (int)((double)(this.scrollOffs * (float)i) + 0.5) * 4;
		}

		return true;
	}

	private boolean isScrollBarActive() {
		return this.displayRecipes && this.menu.getNumRecipes() > 12;
	}

	protected int getOffscreenRows() {
		return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
	}

	private void containerChanged() {
		this.displayRecipes = this.menu.hasInputItem();
		if (!this.displayRecipes) {
			this.scrollOffs = 0.0F;
			this.startIndex = 0;
		}
	}
}
