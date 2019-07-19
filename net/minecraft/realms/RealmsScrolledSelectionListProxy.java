/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraft.realms.RealmsScrolledSelectionList;

@Environment(value=EnvType.CLIENT)
public class RealmsScrolledSelectionListProxy
extends ScrolledSelectionList {
    private final RealmsScrolledSelectionList realmsScrolledSelectionList;

    public RealmsScrolledSelectionListProxy(RealmsScrolledSelectionList realmsScrolledSelectionList, int i, int j, int k, int l, int m) {
        super(Minecraft.getInstance(), i, j, k, l, m);
        this.realmsScrolledSelectionList = realmsScrolledSelectionList;
    }

    @Override
    public int getItemCount() {
        return this.realmsScrolledSelectionList.getItemCount();
    }

    @Override
    public boolean selectItem(int i, int j, double d, double e) {
        return this.realmsScrolledSelectionList.selectItem(i, j, d, e);
    }

    @Override
    public boolean isSelectedItem(int i) {
        return this.realmsScrolledSelectionList.isSelectedItem(i);
    }

    @Override
    public void renderBackground() {
        this.realmsScrolledSelectionList.renderBackground();
    }

    @Override
    public void renderItem(int i, int j, int k, int l, int m, int n, float f) {
        this.realmsScrolledSelectionList.renderItem(i, j, k, l, m, n);
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsScrolledSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsScrolledSelectionList.getScrollbarPosition();
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (this.realmsScrolledSelectionList.mouseScrolled(d, e, f)) {
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.realmsScrolledSelectionList.mouseClicked(d, e, i)) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return this.realmsScrolledSelectionList.mouseReleased(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return this.realmsScrolledSelectionList.mouseDragged(d, e, i, f, g);
    }
}

