/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public enum VoronoiZoom implements AreaTransformer1
{
    INSTANCE;


    @Override
    public int applyPixel(BigContext<?> bigContext, Area area, int i, int j) {
        int k = i - 2;
        int l = j - 2;
        int m = k >> 2;
        int n = l >> 2;
        int o = m << 2;
        int p = n << 2;
        bigContext.initRandom(o, p);
        double d = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        double e = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        bigContext.initRandom(o + 4, p);
        double f = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        double g = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        bigContext.initRandom(o, p + 4);
        double h = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        double q = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        bigContext.initRandom(o + 4, p + 4);
        double r = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        double s = ((double)bigContext.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        int t = k & 3;
        int u = l & 3;
        double v = ((double)u - e) * ((double)u - e) + ((double)t - d) * ((double)t - d);
        double w = ((double)u - g) * ((double)u - g) + ((double)t - f) * ((double)t - f);
        double x = ((double)u - q) * ((double)u - q) + ((double)t - h) * ((double)t - h);
        double y = ((double)u - s) * ((double)u - s) + ((double)t - r) * ((double)t - r);
        if (v < w && v < x && v < y) {
            return area.get(this.getParentX(o), this.getParentY(p));
        }
        if (w < v && w < x && w < y) {
            return area.get(this.getParentX(o + 4), this.getParentY(p)) & 0xFF;
        }
        if (x < v && x < w && x < y) {
            return area.get(this.getParentX(o), this.getParentY(p + 4));
        }
        return area.get(this.getParentX(o + 4), this.getParentY(p + 4)) & 0xFF;
    }

    @Override
    public int getParentX(int i) {
        return i >> 2;
    }

    @Override
    public int getParentY(int i) {
        return i >> 2;
    }
}

