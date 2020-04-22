/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public abstract class ScrolledSelectionList
extends AbstractContainerEventHandler
implements Widget {
    protected final Minecraft minecraft;
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected final int itemHeight;
    protected boolean centerListVertically = true;
    protected int yDrag = -2;
    protected double yo;
    protected boolean visible = true;
    protected boolean renderSelection = true;
    protected boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;

    public ScrolledSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        this.minecraft = minecraft;
        this.width = i;
        this.height = j;
        this.y0 = k;
        this.y1 = l;
        this.itemHeight = m;
        this.x0 = 0;
        this.x1 = i;
    }

    public boolean isVisible() {
        return this.visible;
    }

    protected abstract int getItemCount();

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    protected boolean selectItem(int i, int j, double d, double e) {
        return true;
    }

    protected abstract boolean isSelectedItem(int var1);

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected void updateItemPosition(int i, int j, int k, float f) {
    }

    protected abstract void renderItem(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, float var8);

    protected void renderHeader(int i, int j, Tesselator tesselator) {
    }

    protected void clickedHeader(int i, int j) {
    }

    protected void renderDecorations(int i, int j) {
    }

    public int getItemAtPosition(double d, double e) {
        int i = this.x0 + this.width / 2 - this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2 + this.getRowWidth() / 2;
        int k = Mth.floor(e - (double)this.y0) - this.headerHeight + (int)this.yo - 4;
        int l = k / this.itemHeight;
        if (d < (double)this.getScrollbarPosition() && d >= (double)i && d <= (double)j && l >= 0 && k >= 0 && l < this.getItemCount()) {
            return l;
        }
        return -1;
    }

    protected void capYPosition() {
        this.yo = Mth.clamp(this.yo, 0.0, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public boolean isMouseInList(double d, double e) {
        return e >= (double)this.y0 && e <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!this.visible) {
            return;
        }
        this.renderBackground();
        int k = this.getScrollbarPosition();
        int l = k + 6;
        this.capYPosition();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float g = 32.0f;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, this.y1, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y1 + (int)this.yo) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y1 + (int)this.yo) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y0 + (int)this.yo) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y0 + (int)this.yo) / 32.0f).color(32, 32, 32, 255).endVertex();
        tesselator.end();
        int m = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
        int n = this.y0 + 4 - (int)this.yo;
        if (this.renderHeader) {
            this.renderHeader(m, n, tesselator);
        }
        this.renderList(poseStack, m, n, i, j, f);
        RenderSystem.disableDepthTest();
        this.renderHoleBackground(0, this.y0, 255, 255);
        this.renderHoleBackground(this.y1, this.height, 255, 255);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        int o = 4;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, this.y0 + 4, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x1, this.y0 + 4, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 255).endVertex();
        tesselator.end();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, this.y1, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1 - 4, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x0, this.y1 - 4, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 0).endVertex();
        tesselator.end();
        int p = this.getMaxScroll();
        if (p > 0) {
            int q = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            int r = (int)this.yo * (this.y1 - this.y0 - (q = Mth.clamp(q, 32, this.y1 - this.y0 - 8))) / p + this.y0;
            if (r < this.y0) {
                r = this.y0;
            }
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(k, this.y1, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(l, this.y1, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(l, this.y0, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(k, this.y0, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            tesselator.end();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(k, r + q, 0.0).uv(0.0f, 1.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(l, r + q, 0.0).uv(1.0f, 1.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(l, r, 0.0).uv(1.0f, 0.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(k, r, 0.0).uv(0.0f, 0.0f).color(128, 128, 128, 255).endVertex();
            tesselator.end();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(k, r + q - 1, 0.0).uv(0.0f, 1.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(l - 1, r + q - 1, 0.0).uv(1.0f, 1.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(l - 1, r, 0.0).uv(1.0f, 0.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(k, r, 0.0).uv(0.0f, 0.0f).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }
        this.renderDecorations(i, j);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected void updateScrollingState(double d, double e, int i) {
        this.scrolling = i == 0 && d >= (double)this.getScrollbarPosition() && d < (double)(this.getScrollbarPosition() + 6);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.updateScrollingState(d, e, i);
        if (!this.isVisible() || !this.isMouseInList(d, e)) {
            return false;
        }
        int j = this.getItemAtPosition(d, e);
        if (j == -1 && i == 0) {
            this.clickedHeader((int)(d - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(e - (double)this.y0) + (int)this.yo - 4);
            return true;
        }
        if (j != -1 && this.selectItem(j, i, d, e)) {
            if (this.children().size() > j) {
                this.setFocused(this.children().get(j));
            }
            this.setDragging(true);
            return true;
        }
        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(d, e, i);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (super.mouseDragged(d, e, i, f, g)) {
            return true;
        }
        if (!this.isVisible() || i != 0 || !this.scrolling) {
            return false;
        }
        if (e < (double)this.y0) {
            this.yo = 0.0;
        } else if (e > (double)this.y1) {
            this.yo = this.getMaxScroll();
        } else {
            double h = this.getMaxScroll();
            if (h < 1.0) {
                h = 1.0;
            }
            int j = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            double k = h / (double)(this.y1 - this.y0 - (j = Mth.clamp(j, 32, this.y1 - this.y0 - 8)));
            if (k < 1.0) {
                k = 1.0;
            }
            this.yo += g * k;
            this.capYPosition();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (!this.isVisible()) {
            return false;
        }
        this.yo -= f * (double)this.itemHeight / 2.0;
        return true;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (!this.isVisible()) {
            return false;
        }
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 264) {
            this.moveSelection(1);
            return true;
        }
        if (i == 265) {
            this.moveSelection(-1);
            return true;
        }
        return false;
    }

    protected void moveSelection(int i) {
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (!this.isVisible()) {
            return false;
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return this.isMouseInList(d, e);
    }

    public int getRowWidth() {
        return 220;
    }

    protected void renderList(PoseStack poseStack, int i, int j, int k, int l, float f) {
        int m = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        for (int n = 0; n < m; ++n) {
            int o = j + n * this.itemHeight + this.headerHeight;
            int p = this.itemHeight - 4;
            if (o > this.y1 || o + p < this.y0) {
                this.updateItemPosition(n, i, o, f);
            }
            if (this.renderSelection && this.isSelectedItem(n)) {
                int q = this.x0 + this.width / 2 - this.getRowWidth() / 2;
                int r = this.x0 + this.width / 2 + this.getRowWidth() / 2;
                RenderSystem.disableTexture();
                float g = this.isFocused() ? 1.0f : 0.5f;
                RenderSystem.color4f(g, g, g, 1.0f);
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
                bufferBuilder.vertex(q, o + p + 2, 0.0).endVertex();
                bufferBuilder.vertex(r, o + p + 2, 0.0).endVertex();
                bufferBuilder.vertex(r, o - 2, 0.0).endVertex();
                bufferBuilder.vertex(q, o - 2, 0.0).endVertex();
                tesselator.end();
                RenderSystem.color4f(0.0f, 0.0f, 0.0f, 1.0f);
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
                bufferBuilder.vertex(q + 1, o + p + 1, 0.0).endVertex();
                bufferBuilder.vertex(r - 1, o + p + 1, 0.0).endVertex();
                bufferBuilder.vertex(r - 1, o - 1, 0.0).endVertex();
                bufferBuilder.vertex(q + 1, o - 1, 0.0).endVertex();
                tesselator.end();
                RenderSystem.enableTexture();
            }
            this.renderItem(poseStack, n, i, o, p, k, l, f);
        }
    }

    protected boolean isFocused() {
        return false;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    protected void renderHoleBackground(int i, int j, int k, int l) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float f = 32.0f;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, j, 0.0).uv(0.0f, (float)j / 32.0f).color(64, 64, 64, l).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, j, 0.0).uv((float)this.width / 32.0f, (float)j / 32.0f).color(64, 64, 64, l).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, i, 0.0).uv((float)this.width / 32.0f, (float)i / 32.0f).color(64, 64, 64, k).endVertex();
        bufferBuilder.vertex(this.x0, i, 0.0).uv(0.0f, (float)i / 32.0f).color(64, 64, 64, k).endVertex();
        tesselator.end();
    }
}

