package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class AdvancementTab extends GuiComponent {
	private final Minecraft minecraft;
	private final AdvancementsScreen screen;
	private final AdvancementTabType type;
	private final int index;
	private final Advancement advancement;
	private final DisplayInfo display;
	private final ItemStack icon;
	private final String title;
	private final AdvancementWidget root;
	private final Map<Advancement, AdvancementWidget> widgets = Maps.<Advancement, AdvancementWidget>newLinkedHashMap();
	private double scrollX;
	private double scrollY;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private float fade;
	private boolean centered;

	public AdvancementTab(
		Minecraft minecraft, AdvancementsScreen advancementsScreen, AdvancementTabType advancementTabType, int i, Advancement advancement, DisplayInfo displayInfo
	) {
		this.minecraft = minecraft;
		this.screen = advancementsScreen;
		this.type = advancementTabType;
		this.index = i;
		this.advancement = advancement;
		this.display = displayInfo;
		this.icon = displayInfo.getIcon();
		this.title = displayInfo.getTitle().getColoredString();
		this.root = new AdvancementWidget(this, minecraft, advancement, displayInfo);
		this.addWidget(this.root, advancement);
	}

	public Advancement getAdvancement() {
		return this.advancement;
	}

	public String getTitle() {
		return this.title;
	}

	public void drawTab(int i, int j, boolean bl) {
		this.type.draw(this, i, j, bl, this.index);
	}

	public void drawIcon(int i, int j, ItemRenderer itemRenderer) {
		this.type.drawIcon(i, j, this.index, itemRenderer, this.icon);
	}

	public void drawContents() {
		if (!this.centered) {
			this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
			this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
			this.centered = true;
		}

		RenderSystem.depthFunc(518);
		fill(0, 0, 234, 113, -16777216);
		RenderSystem.depthFunc(515);
		ResourceLocation resourceLocation = this.display.getBackground();
		if (resourceLocation != null) {
			this.minecraft.getTextureManager().bind(resourceLocation);
		} else {
			this.minecraft.getTextureManager().bind(TextureManager.INTENTIONAL_MISSING_TEXTURE);
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int i = Mth.floor(this.scrollX);
		int j = Mth.floor(this.scrollY);
		int k = i % 16;
		int l = j % 16;

		for (int m = -1; m <= 15; m++) {
			for (int n = -1; n <= 8; n++) {
				blit(k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
			}
		}

		this.root.drawConnectivity(i, j, true);
		this.root.drawConnectivity(i, j, false);
		this.root.draw(i, j);
	}

	public void drawTooltips(int i, int j, int k, int l) {
		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.0F, 0.0F, 200.0F);
		fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
		boolean bl = false;
		int m = Mth.floor(this.scrollX);
		int n = Mth.floor(this.scrollY);
		if (i > 0 && i < 234 && j > 0 && j < 113) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.isMouseOver(m, n, i, j)) {
					bl = true;
					advancementWidget.drawHover(m, n, this.fade, k, l);
					break;
				}
			}
		}

		RenderSystem.popMatrix();
		if (bl) {
			this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
		} else {
			this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
		}
	}

	public boolean isMouseOver(int i, int j, double d, double e) {
		return this.type.isMouseOver(i, j, this.index, d, e);
	}

	@Nullable
	public static AdvancementTab create(Minecraft minecraft, AdvancementsScreen advancementsScreen, int i, Advancement advancement) {
		if (advancement.getDisplay() == null) {
			return null;
		} else {
			for (AdvancementTabType advancementTabType : AdvancementTabType.values()) {
				if (i < advancementTabType.getMax()) {
					return new AdvancementTab(minecraft, advancementsScreen, advancementTabType, i, advancement, advancement.getDisplay());
				}

				i -= advancementTabType.getMax();
			}

			return null;
		}
	}

	public void scroll(double d, double e) {
		if (this.maxX - this.minX > 234) {
			this.scrollX = Mth.clamp(this.scrollX + d, (double)(-(this.maxX - 234)), 0.0);
		}

		if (this.maxY - this.minY > 113) {
			this.scrollY = Mth.clamp(this.scrollY + e, (double)(-(this.maxY - 113)), 0.0);
		}
	}

	public void addAdvancement(Advancement advancement) {
		if (advancement.getDisplay() != null) {
			AdvancementWidget advancementWidget = new AdvancementWidget(this, this.minecraft, advancement, advancement.getDisplay());
			this.addWidget(advancementWidget, advancement);
		}
	}

	private void addWidget(AdvancementWidget advancementWidget, Advancement advancement) {
		this.widgets.put(advancement, advancementWidget);
		int i = advancementWidget.getX();
		int j = i + 28;
		int k = advancementWidget.getY();
		int l = k + 27;
		this.minX = Math.min(this.minX, i);
		this.maxX = Math.max(this.maxX, j);
		this.minY = Math.min(this.minY, k);
		this.maxY = Math.max(this.maxY, l);

		for (AdvancementWidget advancementWidget2 : this.widgets.values()) {
			advancementWidget2.attachToParent();
		}
	}

	@Nullable
	public AdvancementWidget getWidget(Advancement advancement) {
		return (AdvancementWidget)this.widgets.get(advancement);
	}

	public AdvancementsScreen getScreen() {
		return this.screen;
	}
}
