/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer
{
    INSTANCE;


    @Override
    public int apply(Context context, int i, int j, int k, int l, int m) {
        int[] is = new int[1];
        if (this.checkEdge(is, m) || this.checkEdgeStrict(is, i, j, k, l, m, 38, 37) || this.checkEdgeStrict(is, i, j, k, l, m, 39, 37) || this.checkEdgeStrict(is, i, j, k, l, m, 32, 5)) {
            return is[0];
        }
        if (m == 2 && (i == 12 || j == 12 || l == 12 || k == 12)) {
            return 34;
        }
        if (m == 6) {
            if (i == 2 || j == 2 || l == 2 || k == 2 || i == 30 || j == 30 || l == 30 || k == 30 || i == 12 || j == 12 || l == 12 || k == 12) {
                return 1;
            }
            if (i == 21 || k == 21 || j == 21 || l == 21 || i == 168 || k == 168 || j == 168 || l == 168) {
                return 23;
            }
        }
        return m;
    }

    private boolean checkEdge(int[] is, int i) {
        if (!Layers.isSame(i, 3)) {
            return false;
        }
        is[0] = i;
        return true;
    }

    private boolean checkEdgeStrict(int[] is, int i, int j, int k, int l, int m, int n, int o) {
        if (m != n) {
            return false;
        }
        is[0] = Layers.isSame(i, n) && Layers.isSame(j, n) && Layers.isSame(l, n) && Layers.isSame(k, n) ? m : o;
        return true;
    }
}

