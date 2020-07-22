package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
	private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
	private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
	private static final Component VERY_SAD_LABEL = new TranslatableComponent("advancements.sad_label");
	private static final Component NO_ADVANCEMENTS_LABEL = new TranslatableComponent("advancements.empty");
	private static final Component TITLE = new TranslatableComponent("gui.advancements");
	private final ClientAdvancements advancements;
	private final Map<Advancement, AdvancementTab> tabs = Maps.<Advancement, AdvancementTab>newLinkedHashMap();
	private AdvancementTab selectedTab;
	private boolean isScrolling;

	public AdvancementsScreen(ClientAdvancements clientAdvancements) {
		super(NarratorChatListener.NO_TITLE);
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		int k = (this.width - 252) / 2;
		int l = (this.height - 140) / 2;
		this.renderBackground(poseStack);
		this.renderInside(poseStack, i, j, k, l);
		this.renderWindow(poseStack, k, l);
		this.renderTooltips(poseStack, i, j, k, l);
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

	private void renderInside(PoseStack poseStack, int i, int j, int k, int l) {
		AdvancementTab advancementTab = this.selectedTab;
		if (advancementTab == null) {
			fill(poseStack, k + 9, l + 18, k + 9 + 234, l + 18 + 113, -16777216);
			int m = k + 9 + 117;
			drawCenteredString(poseStack, this.font, NO_ADVANCEMENTS_LABEL, m, l + 18 + 56 - 9 / 2, -1);
			drawCenteredString(poseStack, this.font, VERY_SAD_LABEL, m, l + 18 + 113 - 9, -1);
		} else {
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)(k + 9), (float)(l + 18), 0.0F);
			advancementTab.drawContents(poseStack);
			RenderSystem.popMatrix();
			RenderSystem.depthFunc(515);
			RenderSystem.disableDepthTest();
		}
	}

	public void renderWindow(PoseStack poseStack, int i, int j) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		this.minecraft.getTextureManager().bind(WINDOW_LOCATION);
		this.blit(poseStack, i, j, 0, 0, 252, 140);
		if (this.tabs.size() > 1) {
			this.minecraft.getTextureManager().bind(TABS_LOCATION);

			for (AdvancementTab advancementTab : this.tabs.values()) {
				advancementTab.drawTab(poseStack, i, j, advancementTab == this.selectedTab);
			}

			RenderSystem.enableRescaleNormal();
			RenderSystem.defaultBlendFunc();

			for (AdvancementTab advancementTab : this.tabs.values()) {
				advancementTab.drawIcon(i, j, this.itemRenderer);
			}

			RenderSystem.disableBlend();
		}

		this.font.draw(poseStack, TITLE, (float)(i + 8), (float)(j + 6), 4210752);
	}

	private void renderTooltips(PoseStack poseStack, int i, int j, int k, int l) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.selectedTab != null) {
			RenderSystem.pushMatrix();
			RenderSystem.enableDepthTest();
			RenderSystem.translatef((float)(k + 9), (float)(l + 18), 400.0F);
			this.selectedTab.drawTooltips(poseStack, i - k - 9, j - l - 18, k, l);
			RenderSystem.disableDepthTest();
			RenderSystem.popMatrix();
		}

		if (this.tabs.size() > 1) {
			for (AdvancementTab advancementTab : this.tabs.values()) {
				if (advancementTab.isMouseOver(k, l, (double)i, (double)j)) {
					this.renderTooltip(poseStack, advancementTab.getTitle(), i, j);
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
