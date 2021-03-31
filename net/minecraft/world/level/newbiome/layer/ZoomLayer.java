/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public enum ZoomLayer implements AreaTransformer1
{
    NORMAL,
    FUZZY{

        @Override
        protected int modeOrRandom(BigContext<?> bigContext, int i, int j, int k, int l) {
            return bigContext.random(i, j, k, l);
        }
    };

    private static final int ZOOM_BITS = 1;
    private static final int ZOOM_MASK = 1;

    @Override
    public int getParentX(int i) {
        return i >> 1;
    }

    @Override
    public int getParentY(int i) {
        return i >> 1;
    }

    @Override
    public int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
        int k = area.get(this.getParentX(i), this.getParentY(j));
        bigContext.initRandom(i >> 1 << 1, j >> 1 << 1);
        int l = i & 1;
        int m = j & 1;
        if (l == 0 && m == 0) {
            return k;
        }
        int n = area.get(this.getParentX(i), this.getParentY(j + 1));
        int o = bigContext.random(k, n);
        if (l == 0 && m == 1) {
            return o;
        }
        int p = area.get(this.getParentX(i + 1), this.getParentY(j));
        int q = bigContext.random(k, p);
        if (l == 1 && m == 0) {
            return q;
        }
        int r = area.get(this.getParentX(i + 1), this.getParentY(j + 1));
        return this.modeOrRandom(bigContext, k, p, n, r);
    }

    protected int modeOrRandom(BigContext<?> bigContext, int i, int j, int k, int l) {
        if (j == k && k == l) {
            return j;
        }
        if (i == j && i == k) {
            return i;
        }
        if (i == j && i == l) {
            return i;
        }
        if (i == k && i == l) {
            return i;
        }
        if (i == j && k != l) {
            return i;
        }
        if (i == k && j != l) {
            return i;
        }
        if (i == l && j != k) {
            return i;
        }
        if (j == k && i != l) {
            return j;
        }
        if (j == l && i != k) {
            return j;
        }
        if (k == l && i != j) {
            return k;
        }
        return bigContext.random(i, j, k, l);
    }
}

