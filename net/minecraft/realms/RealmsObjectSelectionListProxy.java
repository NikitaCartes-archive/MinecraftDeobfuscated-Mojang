/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.realms.RealmsObjectSelectionList;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsObjectSelectionListProxy<E extends ObjectSelectionList.Entry<E>>
extends ObjectSelectionList<E> {
    private final RealmsObjectSelectionList realmsObjectSelectionList;

    public RealmsObjectSelectionListProxy(RealmsObjectSelectionList realmsObjectSelectionList, int i, int j, int k, int l, int m) {
        super(Minecraft.getInstance(), i, j, k, l, m);
        this.realmsObjectSelectionList = realmsObjectSelectionList;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear() {
        super.clearEntries();
    }

    @Override
    public boolean isFocused() {
        return this.realmsObjectSelectionList.isFocused();
    }

    protected void setSelectedItem(int i) {
        if (i == -1) {
            super.setSelected(null);
        } else if (super.getItemCount() != 0) {
            ObjectSelectionList.Entry entry = (ObjectSelectionList.Entry)super.getEntry(i);
            super.setSelected(entry);
        }
    }

    @Override
    public void setSelected(@Nullable E entry) {
        super.setSelected(entry);
        this.realmsObjectSelectionList.selectItem(super.children().indexOf(entry));
    }

    @Override
    public void renderBackground() {
        this.realmsObjectSelectionList.renderBackground();
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsObjectSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsObjectSelectionList.getScrollbarPosition();
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (this.realmsObjectSelectionList.mouseScrolled(d, e, f)) {
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public int getRowWidth() {
        return this.realmsObjectSelectionList.getRowWidth();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.realmsObjectSelectionList.mouseClicked(d, e, i)) {
            return true;
        }
        return RealmsObjectSelectionListProxy.super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return this.realmsObjectSelectionList.mouseReleased(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.realmsObjectSelectionList.mouseDragged(d, e, i, f, g)) {
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    protected final int addEntry(E entry) {
        return super.addEntry(entry);
    }

    @Override
    public E remove(int i) {
        return (E)((ObjectSelectionList.Entry)super.remove(i));
    }

    @Override
    public boolean removeEntry(E entry) {
        return super.removeEntry(entry);
    }

    @Override
    public void setScrollAmount(double d) {
        super.setScrollAmount(d);
    }

    public int y0() {
        return this.y0;
    }

    public int y1() {
        return this.y1;
    }

    public int headerHeight() {
        return this.headerHeight;
    }

    public int itemHeight() {
        return this.itemHeight;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        return this.realmsObjectSelectionList.keyPressed(i, j, k);
    }

    @Override
    public void replaceEntries(Collection<E> collection) {
        super.replaceEntries(collection);
    }

    @Override
    public int getRowTop(int i) {
        return super.getRowTop(i);
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }
}

