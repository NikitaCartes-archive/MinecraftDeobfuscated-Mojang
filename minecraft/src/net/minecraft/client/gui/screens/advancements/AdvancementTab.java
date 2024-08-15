package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class AdvancementTab {
	private final Minecraft minecraft;
	private final AdvancementsScreen screen;
	private final AdvancementTabType type;
	private final int index;
	private final AdvancementNode rootNode;
	private final DisplayInfo display;
	private final ItemStack icon;
	private final Component title;
	private final AdvancementWidget root;
	private final Map<AdvancementHolder, AdvancementWidget> widgets = Maps.<AdvancementHolder, AdvancementWidget>newLinkedHashMap();
	private double scrollX;
	private double scrollY;
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private float fade;
	private boolean centered;

	public AdvancementTab(
		Minecraft minecraft,
		AdvancementsScreen advancementsScreen,
		AdvancementTabType advancementTabType,
		int i,
		AdvancementNode advancementNode,
		DisplayInfo displayInfo
	) {
		this.minecraft = minecraft;
		this.screen = advancementsScreen;
		this.type = advancementTabType;
		this.index = i;
		this.rootNode = advancementNode;
		this.display = displayInfo;
		this.icon = displayInfo.getIcon();
		this.title = displayInfo.getTitle();
		this.root = new AdvancementWidget(this, minecraft, advancementNode, displayInfo);
		this.addWidget(this.root, advancementNode.holder());
	}

	public AdvancementTabType getType() {
		return this.type;
	}

	public int getIndex() {
		return this.index;
	}

	public AdvancementNode getRootNode() {
		return this.rootNode;
	}

	public Component getTitle() {
		return this.title;
	}

	public DisplayInfo getDisplay() {
		return this.display;
	}

	public void drawTab(GuiGraphics guiGraphics, int i, int j, boolean bl) {
		this.type.draw(guiGraphics, i, j, bl, this.index);
	}

	public void drawIcon(GuiGraphics guiGraphics, int i, int j) {
		this.type.drawIcon(guiGraphics, i, j, this.index, this.icon);
	}

	public void drawContents(GuiGraphics guiGraphics, int i, int j) {
		if (!this.centered) {
			this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
			this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
			this.centered = true;
		}

		guiGraphics.enableScissor(i, j, i + 234, j + 113);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((float)i, (float)j, 0.0F);
		ResourceLocation resourceLocation = (ResourceLocation)this.display.getBackground().orElse(TextureManager.INTENTIONAL_MISSING_TEXTURE);
		int k = Mth.floor(this.scrollX);
		int l = Mth.floor(this.scrollY);
		int m = k % 16;
		int n = l % 16;

		for (int o = -1; o <= 15; o++) {
			for (int p = -1; p <= 8; p++) {
				guiGraphics.blit(RenderType::guiTextured, resourceLocation, m + 16 * o, n + 16 * p, 0.0F, 0.0F, 16, 16, 16, 16);
			}
		}

		this.root.drawConnectivity(guiGraphics, k, l, true);
		this.root.drawConnectivity(guiGraphics, k, l, false);
		this.root.draw(guiGraphics, k, l);
		guiGraphics.pose().popPose();
		guiGraphics.disableScissor();
	}

	public void drawTooltips(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -200.0F);
		guiGraphics.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
		boolean bl = false;
		int m = Mth.floor(this.scrollX);
		int n = Mth.floor(this.scrollY);
		if (i > 0 && i < 234 && j > 0 && j < 113) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.isMouseOver(m, n, i, j)) {
					bl = true;
					advancementWidget.drawHover(guiGraphics, m, n, this.fade, k, l);
					break;
				}
			}
		}

		guiGraphics.pose().popPose();
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
	public static AdvancementTab create(Minecraft minecraft, AdvancementsScreen advancementsScreen, int i, AdvancementNode advancementNode) {
		Optional<DisplayInfo> optional = advancementNode.advancement().display();
		if (optional.isEmpty()) {
			return null;
		} else {
			for (AdvancementTabType advancementTabType : AdvancementTabType.values()) {
				if (i < advancementTabType.getMax()) {
					return new AdvancementTab(minecraft, advancementsScreen, advancementTabType, i, advancementNode, (DisplayInfo)optional.get());
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

	public void addAdvancement(AdvancementNode advancementNode) {
		Optional<DisplayInfo> optional = advancementNode.advancement().display();
		if (!optional.isEmpty()) {
			AdvancementWidget advancementWidget = new AdvancementWidget(this, this.minecraft, advancementNode, (DisplayInfo)optional.get());
			this.addWidget(advancementWidget, advancementNode.holder());
		}
	}

	private void addWidget(AdvancementWidget advancementWidget, AdvancementHolder advancementHolder) {
		this.widgets.put(advancementHolder, advancementWidget);
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
	public AdvancementWidget getWidget(AdvancementHolder advancementHolder) {
		return (AdvancementWidget)this.widgets.get(advancementHolder);
	}

	public AdvancementsScreen getScreen() {
		return this.screen;
	}
}
