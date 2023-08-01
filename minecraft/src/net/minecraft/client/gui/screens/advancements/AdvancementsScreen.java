package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
	private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
	public static final int WINDOW_WIDTH = 252;
	public static final int WINDOW_HEIGHT = 140;
	private static final int WINDOW_INSIDE_X = 9;
	private static final int WINDOW_INSIDE_Y = 18;
	public static final int WINDOW_INSIDE_WIDTH = 234;
	public static final int WINDOW_INSIDE_HEIGHT = 113;
	private static final int WINDOW_TITLE_X = 8;
	private static final int WINDOW_TITLE_Y = 6;
	public static final int BACKGROUND_TILE_WIDTH = 16;
	public static final int BACKGROUND_TILE_HEIGHT = 16;
	public static final int BACKGROUND_TILE_COUNT_X = 14;
	public static final int BACKGROUND_TILE_COUNT_Y = 7;
	private static final double SCROLL_SPEED = 16.0;
	private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
	private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
	private static final Component TITLE = Component.translatable("gui.advancements");
	private final ClientAdvancements advancements;
	private final Map<Advancement, AdvancementTab> tabs = Maps.<Advancement, AdvancementTab>newLinkedHashMap();
	@Nullable
	private AdvancementTab selectedTab;
	private boolean isScrolling;

	public AdvancementsScreen(ClientAdvancements clientAdvancements) {
		super(GameNarrator.NO_TITLE);
		this.advancements = clientAdvancements;
	}

	@Override
	protected void init() {
		this.tabs.clear();
		this.selectedTab = null;
		this.advancements.setListener(this);
		if (this.selectedTab == null && !this.tabs.isEmpty()) {
			this.advancements.setSelectedTab(((AdvancementTab)this.tabs.values().iterator().next()).getAdvancement(), true);
		} else {
			this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
		}
	}

	@Override
	public void removed() {
		this.advancements.setListener(null);
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.send(ServerboundSeenAdvancementsPacket.closedScreen());
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i == 0) {
			int j = (this.width - 252) / 2;
			int k = (this.height - 140) / 2;

			for (AdvancementTab advancementTab : this.tabs.values()) {
				if (advancementTab.isMouseOver(j, k, d, e)) {
					this.advancements.setSelectedTab(advancementTab.getAdvancement(), true);
					break;
				}
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.minecraft.options.keyAdvancements.matches(i, j)) {
			this.minecraft.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = (this.width - 252) / 2;
		int l = (this.height - 140) / 2;
		this.renderBackground(guiGraphics, i, j, f);
		this.renderInside(guiGraphics, i, j, k, l);
		this.renderWindow(guiGraphics, k, l);
		this.renderTooltips(guiGraphics, i, j, k, l);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (i != 0) {
			this.isScrolling = false;
			return false;
		} else {
			if (!this.isScrolling) {
				this.isScrolling = true;
			} else if (this.selectedTab != null) {
				this.selectedTab.scroll(f, g);
			}

			return true;
		}
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		if (this.selectedTab != null) {
			this.selectedTab.scroll(f * 16.0, g * 16.0);
			return true;
		} else {
			return false;
		}
	}

	private void renderInside(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		AdvancementTab advancementTab = this.selectedTab;
		if (advancementTab == null) {
			guiGraphics.fill(k + 9, l + 18, k + 9 + 234, l + 18 + 113, -16777216);
			int m = k + 9 + 117;
			guiGraphics.drawCenteredString(this.font, NO_ADVANCEMENTS_LABEL, m, l + 18 + 56 - 9 / 2, -1);
			guiGraphics.drawCenteredString(this.font, VERY_SAD_LABEL, m, l + 18 + 113 - 9, -1);
		} else {
			advancementTab.drawContents(guiGraphics, k + 9, l + 18);
		}
	}

	public void renderWindow(GuiGraphics guiGraphics, int i, int j) {
		RenderSystem.enableBlend();
		guiGraphics.blit(WINDOW_LOCATION, i, j, 0, 0, 252, 140);
		if (this.tabs.size() > 1) {
			for (AdvancementTab advancementTab : this.tabs.values()) {
				advancementTab.drawTab(guiGraphics, i, j, advancementTab == this.selectedTab);
			}

			for (AdvancementTab advancementTab : this.tabs.values()) {
				advancementTab.drawIcon(guiGraphics, i, j);
			}
		}

		guiGraphics.drawString(this.font, TITLE, i + 8, j + 6, 4210752, false);
	}

	private void renderTooltips(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		if (this.selectedTab != null) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate((float)(k + 9), (float)(l + 18), 400.0F);
			RenderSystem.enableDepthTest();
			this.selectedTab.drawTooltips(guiGraphics, i - k - 9, j - l - 18, k, l);
			RenderSystem.disableDepthTest();
			guiGraphics.pose().popPose();
		}

		if (this.tabs.size() > 1) {
			for (AdvancementTab advancementTab : this.tabs.values()) {
				if (advancementTab.isMouseOver(k, l, (double)i, (double)j)) {
					guiGraphics.renderTooltip(this.font, advancementTab.getTitle(), i, j);
				}
			}
		}
	}

	@Override
	public void onAddAdvancementRoot(Advancement advancement) {
		AdvancementTab advancementTab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), advancement);
		if (advancementTab != null) {
			this.tabs.put(advancement, advancementTab);
		}
	}

	@Override
	public void onRemoveAdvancementRoot(Advancement advancement) {
	}

	@Override
	public void onAddAdvancementTask(Advancement advancement) {
		AdvancementTab advancementTab = this.getTab(advancement);
		if (advancementTab != null) {
			advancementTab.addAdvancement(advancement);
		}
	}

	@Override
	public void onRemoveAdvancementTask(Advancement advancement) {
	}

	@Override
	public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress) {
		AdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
		if (advancementWidget != null) {
			advancementWidget.setProgress(advancementProgress);
		}
	}

	@Override
	public void onSelectedTabChanged(@Nullable Advancement advancement) {
		this.selectedTab = (AdvancementTab)this.tabs.get(advancement);
	}

	@Override
	public void onAdvancementsCleared() {
		this.tabs.clear();
		this.selectedTab = null;
	}

	@Nullable
	public AdvancementWidget getAdvancementWidget(Advancement advancement) {
		AdvancementTab advancementTab = this.getTab(advancement);
		return advancementTab == null ? null : advancementTab.getWidget(advancement);
	}

	@Nullable
	private AdvancementTab getTab(Advancement advancement) {
		while (advancement.getParent() != null) {
			advancement = advancement.getParent();
		}

		return (AdvancementTab)this.tabs.get(advancement);
	}
}
