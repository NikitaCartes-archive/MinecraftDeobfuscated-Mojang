package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AdvancementWidget {
	private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");
	private static final int HEIGHT = 26;
	private static final int BOX_X = 0;
	private static final int BOX_WIDTH = 200;
	private static final int FRAME_WIDTH = 26;
	private static final int ICON_X = 8;
	private static final int ICON_Y = 5;
	private static final int ICON_WIDTH = 26;
	private static final int TITLE_PADDING_LEFT = 3;
	private static final int TITLE_PADDING_RIGHT = 5;
	private static final int TITLE_X = 32;
	private static final int TITLE_Y = 9;
	private static final int TITLE_MAX_WIDTH = 163;
	private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
	private final AdvancementTab tab;
	private final AdvancementNode advancementNode;
	private final DisplayInfo display;
	private final FormattedCharSequence title;
	private final int width;
	private final List<FormattedCharSequence> description;
	private final Minecraft minecraft;
	@Nullable
	private AdvancementWidget parent;
	private final List<AdvancementWidget> children = Lists.<AdvancementWidget>newArrayList();
	@Nullable
	private AdvancementProgress progress;
	private final int x;
	private final int y;

	public AdvancementWidget(AdvancementTab advancementTab, Minecraft minecraft, AdvancementNode advancementNode, DisplayInfo displayInfo) {
		this.tab = advancementTab;
		this.advancementNode = advancementNode;
		this.display = displayInfo;
		this.minecraft = minecraft;
		this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(displayInfo.getTitle(), 163));
		this.x = Mth.floor(displayInfo.getX() * 28.0F);
		this.y = Mth.floor(displayInfo.getY() * 27.0F);
		int i = this.getMaxProgressWidth();
		int j = 29 + minecraft.font.width(this.title) + i;
		this.description = Language.getInstance()
			.getVisualOrder(
				this.findOptimalLines(ComponentUtils.mergeStyles(displayInfo.getDescription().copy(), Style.EMPTY.withColor(displayInfo.getType().getChatColor())), j)
			);

		for (FormattedCharSequence formattedCharSequence : this.description) {
			j = Math.max(j, minecraft.font.width(formattedCharSequence));
		}

		this.width = j + 3 + 5;
	}

	private int getMaxProgressWidth() {
		int i = this.advancementNode.advancement().requirements().size();
		if (i <= 1) {
			return 0;
		} else {
			int j = 8;
			Component component = Component.translatable("advancements.progress", i, i);
			return this.minecraft.font.width(component) + 8;
		}
	}

	private static float getMaxWidth(StringSplitter stringSplitter, List<FormattedText> list) {
		return (float)list.stream().mapToDouble(stringSplitter::stringWidth).max().orElse(0.0);
	}

	private List<FormattedText> findOptimalLines(Component component, int i) {
		StringSplitter stringSplitter = this.minecraft.font.getSplitter();
		List<FormattedText> list = null;
		float f = Float.MAX_VALUE;

		for (int j : TEST_SPLIT_OFFSETS) {
			List<FormattedText> list2 = stringSplitter.splitLines(component, i - j, Style.EMPTY);
			float g = Math.abs(getMaxWidth(stringSplitter, list2) - (float)i);
			if (g <= 10.0F) {
				return list2;
			}

			if (g < f) {
				f = g;
				list = list2;
			}
		}

		return list;
	}

	@Nullable
	private AdvancementWidget getFirstVisibleParent(AdvancementNode advancementNode) {
		do {
			advancementNode = advancementNode.parent();
		} while (advancementNode != null && advancementNode.advancement().display().isEmpty());

		return advancementNode != null && !advancementNode.advancement().display().isEmpty() ? this.tab.getWidget(advancementNode.holder()) : null;
	}

	public void drawConnectivity(GuiGraphics guiGraphics, int i, int j, boolean bl) {
		if (this.parent != null) {
			int k = i + this.parent.x + 13;
			int l = i + this.parent.x + 26 + 4;
			int m = j + this.parent.y + 13;
			int n = i + this.x + 13;
			int o = j + this.y + 13;
			int p = bl ? -16777216 : -1;
			if (bl) {
				guiGraphics.hLine(l, k, m - 1, p);
				guiGraphics.hLine(l + 1, k, m, p);
				guiGraphics.hLine(l, k, m + 1, p);
				guiGraphics.hLine(n, l - 1, o - 1, p);
				guiGraphics.hLine(n, l - 1, o, p);
				guiGraphics.hLine(n, l - 1, o + 1, p);
				guiGraphics.vLine(l - 1, o, m, p);
				guiGraphics.vLine(l + 1, o, m, p);
			} else {
				guiGraphics.hLine(l, k, m, p);
				guiGraphics.hLine(n, l, o, p);
				guiGraphics.vLine(l, o, m, p);
			}
		}

		for (AdvancementWidget advancementWidget : this.children) {
			advancementWidget.drawConnectivity(guiGraphics, i, j, bl);
		}
	}

	public void draw(GuiGraphics guiGraphics, int i, int j) {
		if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
			float f = this.progress == null ? 0.0F : this.progress.getPercent();
			AdvancementWidgetType advancementWidgetType;
			if (f >= 1.0F) {
				advancementWidgetType = AdvancementWidgetType.OBTAINED;
			} else {
				advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
			}

			guiGraphics.blitSprite(RenderType::guiTextured, advancementWidgetType.frameSprite(this.display.getType()), i + this.x + 3, j + this.y, 26, 26);
			guiGraphics.renderFakeItem(this.display.getIcon(), i + this.x + 8, j + this.y + 5);
		}

		for (AdvancementWidget advancementWidget : this.children) {
			advancementWidget.draw(guiGraphics, i, j);
		}
	}

	public int getWidth() {
		return this.width;
	}

	public void setProgress(AdvancementProgress advancementProgress) {
		this.progress = advancementProgress;
	}

	public void addChild(AdvancementWidget advancementWidget) {
		this.children.add(advancementWidget);
	}

	public void drawHover(GuiGraphics guiGraphics, int i, int j, float f, int k, int l) {
		boolean bl = k + i + this.x + this.width + 26 >= this.tab.getScreen().width;
		Component component = this.progress == null ? null : this.progress.getProgressText();
		int m = component == null ? 0 : this.minecraft.font.width(component);
		boolean bl2 = 113 - j - this.y - 26 <= 6 + this.description.size() * 9;
		float g = this.progress == null ? 0.0F : this.progress.getPercent();
		int n = Mth.floor(g * (float)this.width);
		AdvancementWidgetType advancementWidgetType;
		AdvancementWidgetType advancementWidgetType2;
		AdvancementWidgetType advancementWidgetType3;
		if (g >= 1.0F) {
			n = this.width / 2;
			advancementWidgetType = AdvancementWidgetType.OBTAINED;
			advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
			advancementWidgetType3 = AdvancementWidgetType.OBTAINED;
		} else if (n < 2) {
			n = this.width / 2;
			advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
			advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
			advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
		} else if (n > this.width - 2) {
			n = this.width / 2;
			advancementWidgetType = AdvancementWidgetType.OBTAINED;
			advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
			advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
		} else {
			advancementWidgetType = AdvancementWidgetType.OBTAINED;
			advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
			advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
		}

		int o = this.width - n;
		int p = j + this.y;
		int q;
		if (bl) {
			q = i + this.x - this.width + 26 + 6;
		} else {
			q = i + this.x;
		}

		int r = 32 + this.description.size() * 9;
		if (!this.description.isEmpty()) {
			if (bl2) {
				guiGraphics.blitSprite(RenderType::guiTextured, TITLE_BOX_SPRITE, q, p + 26 - r, this.width, r);
			} else {
				guiGraphics.blitSprite(RenderType::guiTextured, TITLE_BOX_SPRITE, q, p, this.width, r);
			}
		}

		guiGraphics.blitSprite(RenderType::guiTextured, advancementWidgetType.boxSprite(), 200, 26, 0, 0, q, p, n, 26);
		guiGraphics.blitSprite(RenderType::guiTextured, advancementWidgetType2.boxSprite(), 200, 26, 200 - o, 0, q + n, p, o, 26);
		guiGraphics.blitSprite(RenderType::guiTextured, advancementWidgetType3.frameSprite(this.display.getType()), i + this.x + 3, j + this.y, 26, 26);
		if (bl) {
			guiGraphics.drawString(this.minecraft.font, this.title, q + 5, j + this.y + 9, -1);
			if (component != null) {
				guiGraphics.drawString(this.minecraft.font, component, i + this.x - m, j + this.y + 9, -1);
			}
		} else {
			guiGraphics.drawString(this.minecraft.font, this.title, i + this.x + 32, j + this.y + 9, -1);
			if (component != null) {
				guiGraphics.drawString(this.minecraft.font, component, i + this.x + this.width - m - 5, j + this.y + 9, -1);
			}
		}

		if (bl2) {
			for (int s = 0; s < this.description.size(); s++) {
				guiGraphics.drawString(this.minecraft.font, (FormattedCharSequence)this.description.get(s), q + 5, p + 26 - r + 7 + s * 9, -5592406, false);
			}
		} else {
			for (int s = 0; s < this.description.size(); s++) {
				guiGraphics.drawString(this.minecraft.font, (FormattedCharSequence)this.description.get(s), q + 5, j + this.y + 9 + 17 + s * 9, -5592406, false);
			}
		}

		guiGraphics.renderFakeItem(this.display.getIcon(), i + this.x + 8, j + this.y + 5);
	}

	public boolean isMouseOver(int i, int j, int k, int l) {
		if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
			int m = i + this.x;
			int n = m + 26;
			int o = j + this.y;
			int p = o + 26;
			return k >= m && k <= n && l >= o && l <= p;
		} else {
			return false;
		}
	}

	public void attachToParent() {
		if (this.parent == null && this.advancementNode.parent() != null) {
			this.parent = this.getFirstVisibleParent(this.advancementNode);
			if (this.parent != null) {
				this.parent.addChild(this);
			}
		}
	}

	public int getY() {
		return this.y;
	}

	public int getX() {
		return this.x;
	}
}
