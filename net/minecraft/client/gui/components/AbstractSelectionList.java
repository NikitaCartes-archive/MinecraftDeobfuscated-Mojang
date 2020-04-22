/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends Entry<E>>
extends AbstractContainerEventHandler
implements Widget {
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new TrackedList();
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected boolean centerListVertically = true;
    protected int yDrag = -2;
    private double scrollAmount;
    protected boolean renderSelection = true;
    protected boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    private E selected;

    public AbstractSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        this.minecraft = minecraft;
        this.width = i;
        this.height = j;
        this.y0 = k;
        this.y1 = l;
        this.itemHeight = m;
        this.x0 = 0;
        this.x1 = i;
    }

    protected void setRenderHeader(boolean bl, int i) {
        this.renderHeader = bl;
        this.headerHeight = i;
        if (!bl) {
            this.headerHeight = 0;
        }
    }

    public int getRowWidth() {
        return 220;
    }

    @Nullable
    public E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E entry) {
        this.selected = entry;
    }

    @Nullable
    public E getFocused() {
        return (E)((Entry)super.getFocused());
    }

    public final List<E> children() {
        return this.children;
    }

    protected final void clearEntries() {
        this.children.clear();
    }

    protected void replaceEntries(Collection<E> collection) {
        this.children.clear();
        this.children.addAll(collection);
    }

    protected E getEntry(int i) {
        return (E)((Entry)this.children().get(i));
    }

    protected int addEntry(E entry) {
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected int getItemCount() {
        return this.children().size();
    }

    protected boolean isSelectedItem(int i) {
        return Objects.equals(this.getSelected(), this.children().get(i));
    }

    @Nullable
    protected final E getEntryAtPosition(double d, double e) {
        int i = this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2;
        int k = j - i;
        int l = j + i;
        int m = Mth.floor(e - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
        int n = m / this.itemHeight;
        if (d < (double)this.getScrollbarPosition() && d >= (double)k && d <= (double)l && n >= 0 && m >= 0 && n < this.getItemCount()) {
            return (E)((Entry)this.children().get(n));
        }
        return null;
    }

    public void updateSize(int i, int j, int k, int l) {
        this.width = i;
        this.height = j;
        this.y0 = k;
        this.y1 = l;
        this.x0 = 0;
        this.x1 = i;
    }

    public void setLeftPos(int i) {
        this.x0 = i;
        this.x1 = i + this.width;
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected void clickedHeader(int i, int j) {
    }

    protected void renderHeader(PoseStack poseStack, int i, int j, Tesselator tesselator) {
    }

    protected void renderBackground(PoseStack poseStack) {
    }

    protected void renderDecorations(PoseStack poseStack, int i, int j) {
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        int k = this.getScrollbarPosition();
        int l = k + 6;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float g = 32.0f;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, this.y1, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
        tesselator.end();
        int m = this.getRowLeft();
        int n = this.y0 + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(poseStack, m, n, tesselator);
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
            q = Mth.clamp(q, 32, this.y1 - this.y0 - 8);
            int r = (int)this.getScrollAmount() * (this.y1 - this.y0 - q) / p + this.y0;
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
        this.renderDecorations(poseStack, i, j);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected void centerScrollOn(E entry) {
        this.setScrollAmount(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2);
    }

    protected void ensureVisible(E entry) {
        int k;
        int i = this.getRowTop(this.children().indexOf(entry));
        int j = i - this.y0 - 4 - this.itemHeight;
        if (j < 0) {
            this.scroll(j);
        }
        if ((k = this.y1 - i - this.itemHeight - this.itemHeight) < 0) {
            this.scroll(-k);
        }
    }

    private void scroll(int i) {
        this.setScrollAmount(this.getScrollAmount() + (double)i);
        this.yDrag = -2;
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double d) {
        this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScroll());
    }

    private int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    protected void updateScrollingState(double d, double e, int i) {
        this.scrolling = i == 0 && d >= (double)this.getScrollbarPosition() && d < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.updateScrollingState(d, e, i);
        if (!this.isMouseOver(d, e)) {
            return false;
        }
        E entry = this.getEntryAtPosition(d, e);
        if (entry != null) {
            if (entry.mouseClicked(d, e, i)) {
                this.setFocused((GuiEventListener)entry);
                this.setDragging(true);
                return true;
            }
        } else if (i == 0) {
            this.clickedHeader((int)(d - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(e - (double)this.y0) + (int)this.getScrollAmount() - 4);
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
        if (i != 0 || !this.scrolling) {
            return false;
        }
        if (e < (double)this.y0) {
            this.setScrollAmount(0.0);
        } else if (e > (double)this.y1) {
            this.setScrollAmount(this.getMaxScroll());
        } else {
            double h = Math.max(1, this.getMaxScroll());
            int j = this.y1 - this.y0;
            int k = Mth.clamp((int)((float)(j * j) / (float)this.getMaxPosition()), 32, j - 8);
            double l = Math.max(1.0, h / (double)(j - k));
            this.setScrollAmount(this.getScrollAmount() + g * l);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        this.setScrollAmount(this.getScrollAmount() - f * (double)this.itemHeight / 2.0);
        return true;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
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
        if (!this.children().isEmpty()) {
            int j = this.children().indexOf(this.getSelected());
            int k = Mth.clamp(j + i, 0, this.getItemCount() - 1);
            Entry entry = (Entry)this.children().get(k);
            this.setSelected(entry);
            this.ensureVisible(entry);
        }
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return e >= (double)this.y0 && e <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
    }

    protected void renderList(PoseStack poseStack, int i, int j, int k, int l, float f) {
        int m = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        for (int n = 0; n < m; ++n) {
            int t;
            int o = this.getRowTop(n);
            int p = this.getRowBottom(n);
            if (p < this.y0 || o > this.y1) continue;
            int q = j + n * this.itemHeight + this.headerHeight;
            int r = this.itemHeight - 4;
            E entry = this.getEntry(n);
            int s = this.getRowWidth();
            if (this.renderSelection && this.isSelectedItem(n)) {
                t = this.x0 + this.width / 2 - s / 2;
                int u = this.x0 + this.width / 2 + s / 2;
                RenderSystem.disableTexture();
                float g = this.isFocused() ? 1.0f : 0.5f;
                RenderSystem.color4f(g, g, g, 1.0f);
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
                bufferBuilder.vertex(t, q + r + 2, 0.0).endVertex();
                bufferBuilder.vertex(u, q + r + 2, 0.0).endVertex();
                bufferBuilder.vertex(u, q - 2, 0.0).endVertex();
                bufferBuilder.vertex(t, q - 2, 0.0).endVertex();
                tesselator.end();
                RenderSystem.color4f(0.0f, 0.0f, 0.0f, 1.0f);
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION);
                bufferBuilder.vertex(t + 1, q + r + 1, 0.0).endVertex();
                bufferBuilder.vertex(u - 1, q + r + 1, 0.0).endVertex();
                bufferBuilder.vertex(u - 1, q - 1, 0.0).endVertex();
                bufferBuilder.vertex(t + 1, q - 1, 0.0).endVertex();
                tesselator.end();
                RenderSystem.enableTexture();
            }
            t = this.getRowLeft();
            ((Entry)entry).render(poseStack, n, o, t, s, r, k, l, this.isMouseOver(k, l) && Objects.equals(this.getEntryAtPosition(k, l), entry), f);
        }
    }

    protected int getRowLeft() {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    protected int getRowTop(int i) {
        return this.y0 + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
    }

    private int getRowBottom(int i) {
        return this.getRowTop(i) + this.itemHeight;
    }

    protected boolean isFocused() {
        return false;
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

    protected E remove(int i) {
        Entry entry = (Entry)this.children.get(i);
        if (this.removeEntry((Entry)this.children.get(i))) {
            return (E)entry;
        }
        return null;
    }

    protected boolean removeEntry(E entry) {
        boolean bl = this.children.remove(entry);
        if (bl && entry == this.getSelected()) {
            this.setSelected(null);
        }
        return bl;
    }

    @Override
    @Nullable
    public /* synthetic */ GuiEventListener getFocused() {
        return this.getFocused();
    }

    @Environment(value=EnvType.CLIENT)
    class TrackedList
    extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        private TrackedList() {
        }

        @Override
        public E get(int i) {
            return (Entry)this.delegate.get(i);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public E set(int i, E entry) {
            Entry entry2 = (Entry)this.delegate.set(i, entry);
            ((Entry)entry).list = AbstractSelectionList.this;
            return entry2;
        }

        @Override
        public void add(int i, E entry) {
            this.delegate.add(i, entry);
            ((Entry)entry).list = AbstractSelectionList.this;
        }

        @Override
        public E remove(int i) {
            return (Entry)this.delegate.remove(i);
        }

        @Override
        public /* synthetic */ Object remove(int i) {
            return this.remove(i);
        }

        @Override
        public /* synthetic */ void add(int i, Object object) {
            this.add(i, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object set(int i, Object object) {
            return this.set(i, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object get(int i) {
            return this.get(i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry<E extends Entry<E>>
    implements GuiEventListener {
        @Deprecated
        AbstractSelectionList<E> list;

        public abstract void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        @Override
        public boolean isMouseOver(double d, double e) {
            return Objects.equals(this.list.getEntryAtPosition(d, e), this);
        }
    }
}

