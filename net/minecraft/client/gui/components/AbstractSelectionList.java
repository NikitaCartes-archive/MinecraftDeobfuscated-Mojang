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
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends Entry<E>>
extends AbstractContainerEventHandler
implements Widget {
    public static final ResourceLocation WHITE_TEXTURE_LOCATION = new ResourceLocation("textures/misc/white.png");
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
    private double scrollAmount;
    private boolean renderSelection = true;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    private E selected;
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;

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

    public void setRenderSelection(boolean bl) {
        this.renderSelection = bl;
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

    public void setRenderBackground(boolean bl) {
        this.renderBackground = bl;
    }

    public void setRenderTopAndBottom(boolean bl) {
        this.renderTopAndBottom = bl;
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
        int q;
        int p;
        int o;
        this.renderBackground(poseStack);
        int k = this.getScrollbarPosition();
        int l = k + 6;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        if (this.renderBackground) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            float g = 32.0f;
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.x0, this.y1, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.x1, this.y1, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.x1, this.y0, 0.0).uv((float)this.x1 / 32.0f, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
            bufferBuilder.vertex(this.x0, this.y0, 0.0).uv((float)this.x0 / 32.0f, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0f).color(32, 32, 32, 255).endVertex();
            tesselator.end();
        }
        int m = this.getRowLeft();
        int n = this.y0 + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(poseStack, m, n, tesselator);
        }
        this.renderList(poseStack, m, n, i, j, f);
        if (this.renderTopAndBottom) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float h = 32.0f;
            o = -100;
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.x0, this.y0, -100.0).uv(0.0f, (float)this.y0 / 32.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0 + this.width, this.y0, -100.0).uv((float)this.width / 32.0f, (float)this.y0 / 32.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0 + this.width, 0.0, -100.0).uv((float)this.width / 32.0f, 0.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0, 0.0, -100.0).uv(0.0f, 0.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0, this.height, -100.0).uv(0.0f, (float)this.height / 32.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0 + this.width, this.height, -100.0).uv((float)this.width / 32.0f, (float)this.height / 32.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0 + this.width, this.y1, -100.0).uv((float)this.width / 32.0f, (float)this.y1 / 32.0f).color(64, 64, 64, 255).endVertex();
            bufferBuilder.vertex(this.x0, this.y1, -100.0).uv(0.0f, (float)this.y1 / 32.0f).color(64, 64, 64, 255).endVertex();
            tesselator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE_LOCATION);
            p = 4;
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(this.x0, this.y0 + 4, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.x1, this.y0 + 4, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.x1, this.y0, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.x0, this.y0, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.x0, this.y1, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.x1, this.y1, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(this.x1, this.y1 - 4, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 0).endVertex();
            bufferBuilder.vertex(this.x0, this.y1 - 4, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 0).endVertex();
            tesselator.end();
        }
        if ((q = this.getMaxScroll()) > 0) {
            RenderSystem.disableTexture();
            o = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            o = Mth.clamp(o, 32, this.y1 - this.y0 - 8);
            p = (int)this.getScrollAmount() * (this.y1 - this.y0 - o) / q + this.y0;
            if (p < this.y0) {
                p = this.y0;
            }
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(k, this.y1, 0.0).uv(0.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(l, this.y1, 0.0).uv(1.0f, 1.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(l, this.y0, 0.0).uv(1.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(k, this.y0, 0.0).uv(0.0f, 0.0f).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(k, p + o, 0.0).uv(0.0f, 1.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(l, p + o, 0.0).uv(1.0f, 1.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(l, p, 0.0).uv(1.0f, 0.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(k, p, 0.0).uv(0.0f, 0.0f).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(k, p + o - 1, 0.0).uv(0.0f, 1.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(l - 1, p + o - 1, 0.0).uv(1.0f, 1.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(l - 1, p, 0.0).uv(1.0f, 0.0f).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(k, p, 0.0).uv(0.0f, 0.0f).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }
        this.renderDecorations(poseStack, i, j);
        RenderSystem.enableTexture();
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
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double d) {
        this.scrollAmount = Mth.clamp(d, 0.0, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
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
            this.moveSelection(SelectionDirection.DOWN);
            return true;
        }
        if (i == 265) {
            this.moveSelection(SelectionDirection.UP);
            return true;
        }
        return false;
    }

    protected void moveSelection(SelectionDirection selectionDirection) {
        this.moveSelection(selectionDirection, entry -> true);
    }

    protected void refreshSelection() {
        E entry = this.getSelected();
        if (entry != null) {
            this.setSelected(entry);
            this.ensureVisible(entry);
        }
    }

    protected void moveSelection(SelectionDirection selectionDirection, Predicate<E> predicate) {
        int i;
        int n = i = selectionDirection == SelectionDirection.UP ? -1 : 1;
        if (!this.children().isEmpty()) {
            int k;
            int j = this.children().indexOf(this.getSelected());
            while (j != (k = Mth.clamp(j + i, 0, this.getItemCount() - 1))) {
                Entry entry = (Entry)this.children().get(k);
                if (predicate.test(entry)) {
                    this.setSelected(entry);
                    this.ensureVisible(entry);
                    break;
                }
                j = k;
            }
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
                RenderSystem.setShader(GameRenderer::getPositionShader);
                float g = this.isFocused() ? 1.0f : 0.5f;
                RenderSystem.setShaderColor(g, g, g, 1.0f);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferBuilder.vertex(t, q + r + 2, 0.0).endVertex();
                bufferBuilder.vertex(u, q + r + 2, 0.0).endVertex();
                bufferBuilder.vertex(u, q - 2, 0.0).endVertex();
                bufferBuilder.vertex(t, q - 2, 0.0).endVertex();
                tesselator.end();
                RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
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

    public int getRowLeft() {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
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

    private void bindEntryToSelf(Entry<E> entry) {
        ((Entry)entry).list = this;
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
            AbstractSelectionList.this.bindEntryToSelf(entry);
            return entry2;
        }

        @Override
        public void add(int i, E entry) {
            this.delegate.add(i, entry);
            AbstractSelectionList.this.bindEntryToSelf(entry);
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
        private AbstractSelectionList<E> list;

        public abstract void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        @Override
        public boolean isMouseOver(double d, double e) {
            return Objects.equals(this.list.getEntryAtPosition(d, e), this);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SelectionDirection {
        UP,
        DOWN;

    }
}

