/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.util.AbstractList;
import net.minecraft.nbt.Tag;

public abstract class CollectionTag<T extends Tag>
extends AbstractList<T>
implements Tag {
    @Override
    public abstract T set(int var1, T var2);

    @Override
    public abstract void add(int var1, T var2);

    @Override
    public abstract T remove(int var1);

    public abstract boolean setTag(int var1, Tag var2);

    public abstract boolean addTag(int var1, Tag var2);

    public abstract byte getElementType();

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (T)((Tag)object));
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (T)((Tag)object));
    }
}

