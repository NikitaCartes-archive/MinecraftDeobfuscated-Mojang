/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;

@Environment(value=EnvType.CLIENT)
public class TransferableSelectionList
extends ObjectSelectionList<PackEntry> {
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("pack.incompatible");
    private static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("pack.incompatible.confirm.title");
    private final Component title;

    public TransferableSelectionList(Minecraft minecraft, int i, int j, Component component) {
        super(minecraft, i, j, 32, j - 55 + 4, 36);
        this.title = component;
        this.centerListVertically = false;
        minecraft.font.getClass();
        this.setRenderHeader(true, (int)(9.0f * 1.5f));
    }

    @Override
    protected void renderHeader(PoseStack poseStack, int i, int j, Tesselator tesselator) {
        MutableComponent component = new TextComponent("").append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft.font.draw(poseStack, component, (float)(i + this.width / 2 - this.minecraft.font.width(component) / 2), (float)Math.min(this.y0 + 3, j), 0xFFFFFF);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Environment(value=EnvType.CLIENT)
    public static class PackEntry
    extends ObjectSelectionList.Entry<PackEntry> {
        private TransferableSelectionList parent;
        protected final Minecraft minecraft;
        protected final Screen screen;
        private final PackSelectionModel.Entry pack;

        public PackEntry(Minecraft minecraft, TransferableSelectionList transferableSelectionList, Screen screen, PackSelectionModel.Entry entry) {
            this.minecraft = minecraft;
            this.screen = screen;
            this.pack = entry;
            this.parent = transferableSelectionList;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            int p;
            PackCompatibility packCompatibility = this.pack.getCompatibility();
            if (!packCompatibility.isCompatible()) {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                GuiComponent.fill(poseStack, k - 1, j - 1, k + l - 9, j + m + 1, -8978432);
            }
            this.pack.bindIcon(this.minecraft.getTextureManager());
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GuiComponent.blit(poseStack, k, j, 0.0f, 0.0f, 32, 32, 32, 32);
            Component component = this.pack.getTitle();
            FormattedText formattedText = this.pack.getExtendedDescription();
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || bl)) {
                this.minecraft.getTextureManager().bind(ICON_OVERLAY_LOCATION);
                GuiComponent.fill(poseStack, k, j, k + 32, j + 32, -1601138544);
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                p = n - k;
                int q = o - j;
                if (!packCompatibility.isCompatible()) {
                    component = INCOMPATIBLE_TITLE;
                    formattedText = packCompatibility.getDescription();
                }
                if (this.pack.canSelect()) {
                    if (p < 32) {
                        GuiComponent.blit(poseStack, k, j, 0.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(poseStack, k, j, 0.0f, 0.0f, 32, 32, 256, 256);
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (p < 16) {
                            GuiComponent.blit(poseStack, k, j, 32.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(poseStack, k, j, 32.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                    if (this.pack.canMoveUp()) {
                        if (p < 32 && p > 16 && q < 16) {
                            GuiComponent.blit(poseStack, k, j, 96.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(poseStack, k, j, 96.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                    if (this.pack.canMoveDown()) {
                        if (p < 32 && p > 16 && q > 16) {
                            GuiComponent.blit(poseStack, k, j, 64.0f, 32.0f, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(poseStack, k, j, 64.0f, 0.0f, 32, 32, 256, 256);
                        }
                    }
                }
            }
            if ((p = this.minecraft.font.width(component)) > 157) {
                FormattedText formattedText2 = FormattedText.composite(this.minecraft.font.substrByWidth(component, 157 - this.minecraft.font.width("...")), FormattedText.of("..."));
                this.minecraft.font.drawShadow(poseStack, formattedText2, (float)(k + 32 + 2), (float)(j + 1), 0xFFFFFF);
            } else {
                this.minecraft.font.drawShadow(poseStack, component, (float)(k + 32 + 2), (float)(j + 1), 0xFFFFFF);
            }
            this.minecraft.font.drawShadow(poseStack, component, (float)(k + 32 + 2), (float)(j + 1), 0xFFFFFF);
            List<FormattedText> list = this.minecraft.font.split(formattedText, 157);
            for (int r = 0; r < 2 && r < list.size(); ++r) {
                this.minecraft.font.drawShadow(poseStack, list.get(r), (float)(k + 32 + 2), (float)(j + 12 + 10 * r), 0x808080);
            }
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            double f = d - (double)this.parent.getRowLeft();
            double g = e - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && f <= 32.0) {
                if (this.pack.canSelect()) {
                    PackCompatibility packCompatibility = this.pack.getCompatibility();
                    if (packCompatibility.isCompatible()) {
                        this.pack.select();
                    } else {
                        Component component = packCompatibility.getConfirmation();
                        this.minecraft.setScreen(new ConfirmScreen(bl -> {
                            this.minecraft.setScreen(this.screen);
                            if (bl) {
                                this.pack.select();
                            }
                        }, INCOMPATIBLE_CONFIRM_TITLE, component));
                    }
                    return true;
                }
                if (f < 16.0 && this.pack.canUnselect()) {
                    this.pack.unselect();
                    return true;
                }
                if (f > 16.0 && g < 16.0 && this.pack.canMoveUp()) {
                    this.pack.moveUp();
                    return true;
                }
                if (f > 16.0 && g > 16.0 && this.pack.canMoveDown()) {
                    this.pack.moveDown();
                    return true;
                }
            }
            return false;
        }
    }
}

