/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.resourcepacks.lists;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.resourcepacks.ResourcePackSelectScreen;
import net.minecraft.client.gui.screens.resourcepacks.lists.SelectedResourcePackList;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;

@Environment(value=EnvType.CLIENT)
public abstract class ResourcePackList
extends ObjectSelectionList<ResourcePackEntry> {
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("resourcePack.incompatible", new Object[0]);
    private static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("resourcePack.incompatible.confirm.title", new Object[0]);
    protected final Minecraft minecraft;
    private final Component title;

    public ResourcePackList(Minecraft minecraft, int i, int j, Component component) {
        super(minecraft, i, j, 32, j - 55 + 4, 36);
        this.minecraft = minecraft;
        this.centerListVertically = false;
        minecraft.font.getClass();
        this.setRenderHeader(true, (int)(9.0f * 1.5f));
        this.title = component;
    }

    @Override
    protected void renderHeader(int i, int j, Tesselator tesselator) {
        Component component = new TextComponent("").append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft.font.draw(component.getColoredString(), i + this.width / 2 - this.minecraft.font.width(component.getColoredString()) / 2, Math.min(this.y0 + 3, j), 0xFFFFFF);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public void addResourcePackEntry(ResourcePackEntry resourcePackEntry) {
        this.addEntry(resourcePackEntry);
        resourcePackEntry.parent = this;
    }

    @Environment(value=EnvType.CLIENT)
    public static class ResourcePackEntry
    extends ObjectSelectionList.Entry<ResourcePackEntry> {
        private ResourcePackList parent;
        protected final Minecraft minecraft;
        protected final ResourcePackSelectScreen screen;
        private final UnopenedResourcePack resourcePack;

        public ResourcePackEntry(ResourcePackList resourcePackList, ResourcePackSelectScreen resourcePackSelectScreen, UnopenedResourcePack unopenedResourcePack) {
            this.screen = resourcePackSelectScreen;
            this.minecraft = Minecraft.getInstance();
            this.resourcePack = unopenedResourcePack;
            this.parent = resourcePackList;
        }

        public void addToList(SelectedResourcePackList selectedResourcePackList) {
            this.getResourcePack().getDefaultPosition().insert(selectedResourcePackList.children(), this, ResourcePackEntry::getResourcePack, true);
            this.parent = selectedResourcePackList;
        }

        protected void bindToIcon() {
            this.resourcePack.bindIcon(this.minecraft.getTextureManager());
        }

        protected PackCompatibility getCompatibility() {
            return this.resourcePack.getCompatibility();
        }

        protected String getDescription() {
            return this.resourcePack.getDescription().getColoredString();
        }

        protected String getName() {
            return this.resourcePack.getTitle().getColoredString();
        }

        public UnopenedResourcePack getResourcePack() {
            return this.resourcePack;
        }

        @Override
        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            int p;
            PackCompatibility packCompatibility = this.getCompatibility();
            if (!packCompatibility.isCompatible()) {
                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                GuiComponent.fill(k - 1, j - 1, k + l - 9, j + m + 1, -8978432);
            }
            this.bindToIcon();
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GuiComponent.blit(k, j, 0.0f, 0.0f, 32, 32, 32, 32);
            String string = this.getName();
            String string2 = this.getDescription();
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || bl)) {
                this.minecraft.getTextureManager().bind(ICON_OVERLAY_LOCATION);
                GuiComponent.fill(k, j, k + 32, j + 32, -1601138544);
                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                p = n - k;
                int q = o - j;
                if (!packCompatibility.isCompatible()) {
                    string = INCOMPATIBLE_TITLE.getColoredString();
                    string2 = packCompatibility.getDescription().getColoredString();
                }
                if (this.canMoveRight()) {
                    if (p < 32) {
                        GuiComponent.blit(k, j, 0.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(k, j, 0.0f, 0.0f, 32, 32, 256, 256);
                    }
                } else {
                    if (this.canMoveLeft()) {
                        if (p < 16) {
                            GuiComponent.blit(k, j, 32.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(k, j, 32.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                    if (this.canMoveUp()) {
                        if (p < 32 && p > 16 && q < 16) {
                            GuiComponent.blit(k, j, 96.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(k, j, 96.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                    if (this.canMoveDown()) {
                        if (p < 32 && p > 16 && q > 16) {
                            GuiComponent.blit(k, j, 64.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(k, j, 64.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                }
            }
            if ((p = this.minecraft.font.width(string)) > 157) {
                string = this.minecraft.font.substrByWidth(string, 157 - this.minecraft.font.width("...")) + "...";
            }
            this.minecraft.font.drawShadow(string, k + 32 + 2, j + 1, 0xFFFFFF);
            List<String> list = this.minecraft.font.split(string2, 157);
            for (int r = 0; r < 2 && r < list.size(); ++r) {
                this.minecraft.font.drawShadow(list.get(r), k + 32 + 2, j + 12 + 10 * r, 0x808080);
            }
        }

        protected boolean showHoverOverlay() {
            return !this.resourcePack.isFixedPosition() || !this.resourcePack.isRequired();
        }

        protected boolean canMoveRight() {
            return !this.screen.isSelected(this);
        }

        protected boolean canMoveLeft() {
            return this.screen.isSelected(this) && !this.resourcePack.isRequired();
        }

        protected boolean canMoveUp() {
            List list = this.parent.children();
            int i = list.indexOf(this);
            return i > 0 && !((ResourcePackEntry)list.get((int)(i - 1))).resourcePack.isFixedPosition();
        }

        protected boolean canMoveDown() {
            List list = this.parent.children();
            int i = list.indexOf(this);
            return i >= 0 && i < list.size() - 1 && !((ResourcePackEntry)list.get((int)(i + 1))).resourcePack.isFixedPosition();
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            double f = d - (double)this.parent.getRowLeft();
            double g = e - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && f <= 32.0) {
                if (this.canMoveRight()) {
                    this.getScreen().setChanged();
                    PackCompatibility packCompatibility = this.getCompatibility();
                    if (packCompatibility.isCompatible()) {
                        this.getScreen().select(this);
                    } else {
                        Component component = packCompatibility.getConfirmation();
                        this.minecraft.setScreen(new ConfirmScreen(bl -> {
                            this.minecraft.setScreen(this.getScreen());
                            if (bl) {
                                this.getScreen().select(this);
                            }
                        }, INCOMPATIBLE_CONFIRM_TITLE, component));
                    }
                    return true;
                }
                if (f < 16.0 && this.canMoveLeft()) {
                    this.getScreen().deselect(this);
                    return true;
                }
                if (f > 16.0 && g < 16.0 && this.canMoveUp()) {
                    List<ResourcePackEntry> list = this.parent.children();
                    int j = list.indexOf(this);
                    list.remove(this);
                    list.add(j - 1, this);
                    this.getScreen().setChanged();
                    return true;
                }
                if (f > 16.0 && g > 16.0 && this.canMoveDown()) {
                    List<ResourcePackEntry> list = this.parent.children();
                    int j = list.indexOf(this);
                    list.remove(this);
                    list.add(j + 1, this);
                    this.getScreen().setChanged();
                    return true;
                }
            }
            return false;
        }

        public ResourcePackSelectScreen getScreen() {
            return this.screen;
        }
    }
}

