/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends Entry<E>>
extends AbstractContainerEventHandler
implements Renderable,
NarratableEntry {
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
    @Nullable
    private E selected;
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;
    @Nullable
    private E hovered;

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

    public E getFirstElement() {
        return (E)((Entry)this.children.get(0));
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
        this.selected = null;
    }

    protected void replaceEntries(Collection<E> collection) {
        this.clearEntries();
        this.children.addAll(collection);
    }

    protected E getEntry(int i) {
        return (E)((Entry)this.children().get(i));
    }

    protected int addEntry(E entry) {
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
        double d = (double)this.getMaxScroll() - this.getScrollAmount();
        this.children.add(0, entry);
        this.setScrollAmount((double)this.getMaxScroll() - d);
    }

    protected boolean removeEntryFromTop(E entry) {
        double d = (double)this.getMaxScroll() - this.getScrollAmount();
        boolean bl = this.removeEntry(entry);
        this.setScrollAmount((double)this.getMaxScroll() - d);
        return bl;
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

    protected void renderHeader(PoseStack poseStack, int i, int j) {
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
        int m;
        this.renderBackground(poseStack);
        int k = this.getScrollbarPosition();
        int l = k + 6;
        this.hovered = this.isMouseOver(i, j) ? this.getEntryAtPosition(i, j) : null;
        Object v0 = this.hovered;
        if (this.renderBackground) {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f);
            m = 32;
            AbstractSelectionList.blit(poseStack, this.x0, this.y0, this.x1, this.y1 + (int)this.getScrollAmount(), this.x1 - this.x0, this.y1 - this.y0, 32, 32);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        m = this.getRowLeft();
        int n = this.y0 + 4 - (int)this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(poseStack, m, n);
        }
        this.renderList(poseStack, i, j, f);
        if (this.renderTopAndBottom) {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            o = 32;
            p = -100;
            RenderSystem.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);
            AbstractSelectionList.blit(poseStack, this.x0, 0, -100, 0.0f, 0.0f, this.width, this.y0, 32, 32);
            AbstractSelectionList.blit(poseStack, this.x0, this.y1, -100, 0.0f, this.y1, this.width, this.height - this.y1, 32, 32);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            q = 4;
            this.fillGradient(poseStack, this.x0, this.y0, this.x1, this.y0 + 4, -16777216, 0);
            this.fillGradient(poseStack, this.x0, this.y1 - 4, this.x1, this.y1, 0, -16777216);
        }
        if ((o = this.getMaxScroll()) > 0) {
            p = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            p = Mth.clamp(p, 32, this.y1 - this.y0 - 8);
            q = (int)this.getScrollAmount() * (this.y1 - this.y0 - p) / o + this.y0;
            if (q < this.y0) {
                q = this.y0;
            }
            AbstractSelectionList.fill(poseStack, k, this.y0, l, this.y1, -16777216);
            AbstractSelectionList.fill(poseStack, k, q, l, q + p, -8355712);
            AbstractSelectionList.fill(poseStack, k, q, l - 1, q + p - 1, -4144960);
        }
        this.renderDecorations(poseStack, i, j);
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

    public int getScrollBottom() {
        return (int)this.getScrollAmount() - this.height - this.headerHeight;
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
                GuiEventListener entry2 = this.getFocused();
                if (entry2 != entry && entry2 instanceof ContainerEventHandler) {
                    ContainerEventHandler containerEventHandler = (ContainerEventHandler)entry2;
                    containerEventHandler.setFocused(null);
                }
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
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        super.setFocused(guiEventListener);
        int i = this.children.indexOf(guiEventListener);
        if (i >= 0) {
            Entry entry = (Entry)this.children.get(i);
            this.setSelected(entry);
            if (this.minecraft.getLastInputType().isKeyboard()) {
                this.ensureVisible(entry);
            }
        }
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection) {
        return (E)this.nextEntry(screenDirection, entry -> true);
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate) {
        return this.nextEntry(screenDirection, predicate, this.getSelected());
    }

    @Nullable
    protected E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate, @Nullable E entry) {
        int i;
        switch (screenDirection) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case RIGHT: 
            case LEFT: {
                int n = 0;
                break;
            }
            case UP: {
                int n = -1;
                break;
            }
            case DOWN: {
                int n = i = 1;
            }
        }
        if (!this.children().isEmpty() && i != 0) {
            int j = entry == null ? (i > 0 ? 0 : this.children().size() - 1) : this.children().indexOf(entry) + i;
            for (int k = j; k >= 0 && k < this.children.size(); k += i) {
                Entry entry2 = (Entry)this.children().get(k);
                if (!predicate.test(entry2)) continue;
                return (E)entry2;
            }
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return e >= (double)this.y0 && e <= (double)this.y1 && d >= (double)this.x0 && d <= (double)this.x1;
    }

    protected void renderList(PoseStack poseStack, int i, int j, float f) {
        int k = this.getRowLeft();
        int l = this.getRowWidth();
        int m = this.itemHeight - 4;
        int n = this.getItemCount();
        for (int o = 0; o < n; ++o) {
            int p = this.getRowTop(o);
            int q = this.getRowBottom(o);
            if (q < this.y0 || p > this.y1) continue;
            this.renderItem(poseStack, i, j, f, o, k, p, l, m);
        }
    }

    protected void renderItem(PoseStack poseStack, int i, int j, float f, int k, int l, int m, int n, int o) {
        E entry = this.getEntry(k);
        if (this.renderSelection && this.isSelectedItem(k)) {
            int p = this.isFocused() ? -1 : -8355712;
            this.renderSelection(poseStack, m, n, o, p, -16777216);
        }
        ((Entry)entry).render(poseStack, k, m, l, n, o, i, j, Objects.equals(this.hovered, entry), f);
    }

    protected void renderSelection(PoseStack poseStack, int i, int j, int k, int l, int m) {
        int n = this.x0 + (this.width - j) / 2;
        int o = this.x0 + (this.width + j) / 2;
        AbstractSelectionList.fill(poseStack, n, i - 2, o, i + k + 2, l);
        AbstractSelectionList.fill(poseStack, n + 1, i - 1, o - 1, i + k + 1, m);
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

    protected int getRowBottom(int i) {
        return this.getRowTop(i) + this.itemHeight;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.hovered != null) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Nullable
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

    @Nullable
    protected E getHovered() {
        return this.hovered;
    }

    void bindEntryToSelf(Entry<E> entry) {
        entry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E entry) {
        int i;
        List<E> list = this.children();
        if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
            narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", i + 1, list.size()));
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
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

        TrackedList() {
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
    protected static abstract class Entry<E extends Entry<E>>
    implements GuiEventListener {
        @Deprecated
        AbstractSelectionList<E> list;

        protected Entry() {
        }

        @Override
        public void setFocused(boolean bl) {
        }

        @Override
        public boolean isFocused() {
            return this.list.getFocused() == this;
        }

        public abstract void render(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, float var10);

        @Override
        public boolean isMouseOver(double d, double e) {
            return Objects.equals(this.list.getEntryAtPosition(d, e), this);
        }
    }
}

