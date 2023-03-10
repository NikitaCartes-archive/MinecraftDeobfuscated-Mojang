/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;

@Environment(value=EnvType.CLIENT)
public class TransferableSelectionList
extends ObjectSelectionList<PackEntry> {
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
    static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
    private final Component title;
    final PackSelectionScreen screen;

    public TransferableSelectionList(Minecraft minecraft, PackSelectionScreen packSelectionScreen, int i, int j, Component component) {
        super(minecraft, i, j, 32, j - 55 + 4, 36);
        this.screen = packSelectionScreen;
        this.title = component;
        this.centerListVertically = false;
        Objects.requireNonNull(minecraft.font);
        this.setRenderHeader(true, (int)(9.0f * 1.5f));
    }

    @Override
    protected void renderHeader(PoseStack poseStack, int i, int j) {
        MutableComponent component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
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

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.getSelected() != null) {
            switch (i) {
                case 32: 
                case 257: {
                    ((PackEntry)this.getSelected()).keyboardSelection();
                    return true;
                }
            }
            if (Screen.hasShiftDown()) {
                switch (i) {
                    case 265: {
                        ((PackEntry)this.getSelected()).keyboardMoveUp();
                        return true;
                    }
                    case 264: {
                        ((PackEntry)this.getSelected()).keyboardMoveDown();
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    public static class PackEntry
    extends ObjectSelectionList.Entry<PackEntry> {
        private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
        private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
        private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
        private static final int ICON_OVERLAY_X_MOVE_UP = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
        private static final int MAX_NAME_WIDTH_PIXELS = 157;
        private static final String TOO_LONG_NAME_SUFFIX = "...";
        private final TransferableSelectionList parent;
        protected final Minecraft minecraft;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;

        public PackEntry(Minecraft minecraft, TransferableSelectionList transferableSelectionList, PackSelectionModel.Entry entry) {
            this.minecraft = minecraft;
            this.pack = entry;
            this.parent = transferableSelectionList;
            this.nameDisplayCache = PackEntry.cacheName(minecraft, entry.getTitle());
            this.descriptionDisplayCache = PackEntry.cacheDescription(minecraft, entry.getExtendedDescription());
            this.incompatibleNameDisplayCache = PackEntry.cacheName(minecraft, INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = PackEntry.cacheDescription(minecraft, entry.getCompatibility().getDescription());
        }

        private static FormattedCharSequence cacheName(Minecraft minecraft, Component component) {
            int i = minecraft.font.width(component);
            if (i > 157) {
                FormattedText formattedText = FormattedText.composite(minecraft.font.substrByWidth(component, 157 - minecraft.font.width(TOO_LONG_NAME_SUFFIX)), FormattedText.of(TOO_LONG_NAME_SUFFIX));
                return Language.getInstance().getVisualOrder(formattedText);
            }
            return component.getVisualOrderText();
        }

        private static MultiLineLabel cacheDescription(Minecraft minecraft, Component component) {
            return MultiLineLabel.create(minecraft.font, (FormattedText)component, 157, 2);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.pack.getTitle());
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            PackCompatibility packCompatibility = this.pack.getCompatibility();
            if (!packCompatibility.isCompatible()) {
                GuiComponent.fill(poseStack, k - 1, j - 1, k + l - 9, j + m + 1, -8978432);
            }
            RenderSystem.setShaderTexture(0, this.pack.getIconTexture());
            GuiComponent.blit(poseStack, k, j, 0.0f, 0.0f, 32, 32, 32, 32);
            FormattedCharSequence formattedCharSequence = this.nameDisplayCache;
            MultiLineLabel multiLineLabel = this.descriptionDisplayCache;
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get().booleanValue() || bl || this.parent.getSelected() == this && this.parent.isFocused())) {
                RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
                GuiComponent.fill(poseStack, k, j, k + 32, j + 32, -1601138544);
                int p = n - k;
                int q = o - j;
                if (!this.pack.getCompatibility().isCompatible()) {
                    formattedCharSequence = this.incompatibleNameDisplayCache;
                    multiLineLabel = this.incompatibleDescriptionDisplayCache;
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
            this.minecraft.font.drawShadow(poseStack, formattedCharSequence, (float)(k + 32 + 2), (float)(j + 1), 0xFFFFFF);
            multiLineLabel.renderLeftAligned(poseStack, k + 32 + 2, j + 12, 10, 0x808080);
        }

        public String getPackId() {
            return this.pack.getId();
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        public void keyboardSelection() {
            if (this.pack.canSelect() && this.handlePackSelection()) {
                this.parent.screen.updateFocus(this.parent);
            } else if (this.pack.canUnselect()) {
                this.pack.unselect();
                this.parent.screen.updateFocus(this.parent);
            }
        }

        void keyboardMoveUp() {
            if (this.pack.canMoveUp()) {
                this.pack.moveUp();
            }
        }

        void keyboardMoveDown() {
            if (this.pack.canMoveDown()) {
                this.pack.moveDown();
            }
        }

        private boolean handlePackSelection() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.select();
                return true;
            }
            Component component = this.pack.getCompatibility().getConfirmation();
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                this.minecraft.setScreen(this.parent.screen);
                if (bl) {
                    this.pack.select();
                }
            }, INCOMPATIBLE_CONFIRM_TITLE, component));
            return false;
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i != 0) {
                return false;
            }
            double f = d - (double)this.parent.getRowLeft();
            double g = e - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && f <= 32.0) {
                this.parent.screen.clearSelected();
                if (this.pack.canSelect()) {
                    this.handlePackSelection();
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

