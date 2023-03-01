/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Stitcher<T extends Entry> {
    private static final Comparator<Holder<?>> HOLDER_COMPARATOR = Comparator.comparing(holder -> -holder.height).thenComparing(holder -> -holder.width).thenComparing(holder -> holder.entry.name());
    private final int mipLevel;
    private final List<Holder<T>> texturesToBeStitched = new ArrayList<Holder<T>>();
    private final List<Region<T>> storage = new ArrayList<Region<T>>();
    private int storageX;
    private int storageY;
    private final int maxWidth;
    private final int maxHeight;

    public Stitcher(int i, int j, int k) {
        this.mipLevel = k;
        this.maxWidth = i;
        this.maxHeight = j;
    }

    public int getWidth() {
        return this.storageX;
    }

    public int getHeight() {
        return this.storageY;
    }

    public void registerSprite(T entry) {
        Holder<T> holder = new Holder<T>(entry, this.mipLevel);
        this.texturesToBeStitched.add(holder);
    }

    public void stitch() {
        ArrayList<Holder<T>> list = new ArrayList<Holder<T>>(this.texturesToBeStitched);
        list.sort(HOLDER_COMPARATOR);
        for (Holder holder2 : list) {
            if (this.addToStorage(holder2)) continue;
            throw new StitcherException((Entry)holder2.entry, list.stream().map(holder -> holder.entry).collect(ImmutableList.toImmutableList()));
        }
    }

    public void gatherSprites(SpriteLoader<T> spriteLoader) {
        for (Region<T> region : this.storage) {
            region.walk(spriteLoader);
        }
    }

    static int smallestFittingMinTexel(int i, int j) {
        return (i >> j) + ((i & (1 << j) - 1) == 0 ? 0 : 1) << j;
    }

    private boolean addToStorage(Holder<T> holder) {
        for (Region<T> region : this.storage) {
            if (!region.add(holder)) continue;
            return true;
        }
        return this.expand(holder);
    }

    private boolean expand(Holder<T> holder) {
        Region<T> region;
        boolean bl5;
        boolean bl4;
        boolean bl2;
        int i = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        int j = Mth.smallestEncompassingPowerOfTwo(this.storageY);
        int k = Mth.smallestEncompassingPowerOfTwo(this.storageX + holder.width);
        int l = Mth.smallestEncompassingPowerOfTwo(this.storageY + holder.height);
        boolean bl = k <= this.maxWidth;
        boolean bl3 = bl2 = l <= this.maxHeight;
        if (!bl && !bl2) {
            return false;
        }
        boolean bl32 = bl && i != k;
        boolean bl6 = bl4 = bl2 && j != l;
        if (bl32 ^ bl4) {
            bl5 = bl32;
        } else {
            boolean bl7 = bl5 = bl && i <= j;
        }
        if (bl5) {
            if (this.storageY == 0) {
                this.storageY = l;
            }
            region = new Region(this.storageX, 0, k - this.storageX, this.storageY);
            this.storageX = k;
        } else {
            region = new Region<T>(0, this.storageY, this.storageX, l - this.storageY);
            this.storageY = l;
        }
        region.add(holder);
        this.storage.add(region);
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    record Holder<T extends Entry>(T entry, int width, int height) {
        public Holder(T entry, int i) {
            this(entry, Stitcher.smallestFittingMinTexel(entry.width(), i), Stitcher.smallestFittingMinTexel(entry.height(), i));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Entry {
        public int width();

        public int height();

        public ResourceLocation name();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Region<T extends Entry> {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        @Nullable
        private List<Region<T>> subSlots;
        @Nullable
        private Holder<T> holder;

        public Region(int i, int j, int k, int l) {
            this.originX = i;
            this.originY = j;
            this.width = k;
            this.height = l;
        }

        public int getX() {
            return this.originX;
        }

        public int getY() {
            return this.originY;
        }

        public boolean add(Holder<T> holder) {
            if (this.holder != null) {
                return false;
            }
            int i = holder.width;
            int j = holder.height;
            if (i > this.width || j > this.height) {
                return false;
            }
            if (i == this.width && j == this.height) {
                this.holder = holder;
                return true;
            }
            if (this.subSlots == null) {
                this.subSlots = new ArrayList<Region<T>>(1);
                this.subSlots.add(new Region<T>(this.originX, this.originY, i, j));
                int k = this.width - i;
                int l = this.height - j;
                if (l > 0 && k > 0) {
                    int n;
                    int m = Math.max(this.height, k);
                    if (m >= (n = Math.max(this.width, l))) {
                        this.subSlots.add(new Region<T>(this.originX, this.originY + j, i, l));
                        this.subSlots.add(new Region<T>(this.originX + i, this.originY, k, this.height));
                    } else {
                        this.subSlots.add(new Region<T>(this.originX + i, this.originY, k, j));
                        this.subSlots.add(new Region<T>(this.originX, this.originY + j, this.width, l));
                    }
                } else if (k == 0) {
                    this.subSlots.add(new Region<T>(this.originX, this.originY + j, i, l));
                } else if (l == 0) {
                    this.subSlots.add(new Region<T>(this.originX + i, this.originY, k, j));
                }
            }
            for (Region<T> region : this.subSlots) {
                if (!region.add(holder)) continue;
                return true;
            }
            return false;
        }

        public void walk(SpriteLoader<T> spriteLoader) {
            if (this.holder != null) {
                spriteLoader.load(this.holder.entry, this.getX(), this.getY());
            } else if (this.subSlots != null) {
                for (Region region : this.subSlots) {
                    region.walk(spriteLoader);
                }
            }
        }

        public String toString() {
            return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + "}";
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface SpriteLoader<T extends Entry> {
        public void load(T var1, int var2, int var3);
    }
}

