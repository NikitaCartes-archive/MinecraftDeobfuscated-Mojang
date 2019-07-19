/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import net.minecraft.world.inventory.ContainerData;

public abstract class DataSlot {
    private int prevValue;

    public static DataSlot forContainer(final ContainerData containerData, final int i) {
        return new DataSlot(){

            @Override
            public int get() {
                return containerData.get(i);
            }

            @Override
            public void set(int i2) {
                containerData.set(i, i2);
            }
        };
    }

    public static DataSlot shared(final int[] is, final int i) {
        return new DataSlot(){

            @Override
            public int get() {
                return is[i];
            }

            @Override
            public void set(int i2) {
                is[i] = i2;
            }
        };
    }

    public static DataSlot standalone() {
        return new DataSlot(){
            private int value;

            @Override
            public int get() {
                return this.value;
            }

            @Override
            public void set(int i) {
                this.value = i;
            }
        };
    }

    public abstract int get();

    public abstract void set(int var1);

    public boolean checkAndClearUpdateFlag() {
        int i = this.get();
        boolean bl = i != this.prevValue;
        this.prevValue = i;
        return bl;
    }
}

