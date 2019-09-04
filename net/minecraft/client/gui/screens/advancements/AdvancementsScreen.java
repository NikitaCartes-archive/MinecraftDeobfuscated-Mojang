/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementsScreen
extends Screen
implements ClientAdvancements.Listener {
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    private final ClientAdvancements advancements;
    private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
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
            this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
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
                if (!advancementTab.isMouseOver(j, k, d, e)) continue;
                this.advancements.setSelectedTab(advancementTab.getAdvancement(), true);
                break;
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
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        int k = (this.width - 252) / 2;
        int l = (this.height - 140) / 2;
        this.renderBackground();
        this.renderInside(i, j, k, l);
        this.renderWindow(k, l);
        this.renderTooltips(i, j, k, l);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (i != 0) {
            this.isScrolling = false;
            return false;
        }
        if (!this.isScrolling) {
            this.isScrolling = true;
        } else if (this.selectedTab != null) {
            this.selectedTab.scroll(f, g);
        }
        return true;
    }

    private void renderInside(int i, int j, int k, int l) {
        AdvancementTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            AdvancementsScreen.fill(k + 9, l + 18, k + 9 + 234, l + 18 + 113, -16777216);
            String string = I18n.get("advancements.empty", new Object[0]);
            int m = this.font.width(string);
            this.font.draw(string, k + 9 + 117 - m / 2, l + 18 + 56 - this.font.lineHeight / 2, -1);
            this.font.draw(":(", k + 9 + 117 - this.font.width(":(") / 2, l + 18 + 113 - this.font.lineHeight, -1);
            return;
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(k + 9, l + 18, -400.0f);
        RenderSystem.enableDepthTest();
        advancementTab.drawContents();
        RenderSystem.popMatrix();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
    }

    public void renderWindow(int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        Lighting.turnOff();
        this.minecraft.getTextureManager().bind(WINDOW_LOCATION);
        this.blit(i, j, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            this.minecraft.getTextureManager().bind(TABS_LOCATION);
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawTab(i, j, advancementTab == this.selectedTab);
            }
            RenderSystem.enableRescaleNormal();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Lighting.turnOnGui();
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawIcon(i, j, this.itemRenderer);
            }
            RenderSystem.disableBlend();
        }
        this.font.draw(I18n.get("gui.advancements", new Object[0]), i + 8, j + 6, 0x404040);
    }

    private void renderTooltips(int i, int j, int k, int l) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.selectedTab != null) {
            RenderSystem.pushMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.translatef(k + 9, l + 18, 400.0f);
            this.selectedTab.drawTooltips(i - k - 9, j - l - 18, k, l);
            RenderSystem.disableDepthTest();
            RenderSystem.popMatrix();
        }
        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (!advancementTab.isMouseOver(k, l, i, j)) continue;
                this.renderTooltip(advancementTab.getTitle(), i, j);
            }
        }
    }

    @Override
    public void onAddAdvancementRoot(Advancement advancement) {
        AdvancementTab advancementTab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), advancement);
        if (advancementTab == null) {
            return;
        }
        this.tabs.put(advancement, advancementTab);
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
        this.selectedTab = this.tabs.get(advancement);
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
        return this.tabs.get(advancement);
    }
}

