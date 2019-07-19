/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.saveddata;

import net.minecraft.world.level.saveddata.SavedData;

public class SaveDataDirtyRunnable
implements Runnable {
    private final SavedData savedData;

    public SaveDataDirtyRunnable(SavedData savedData) {
        this.savedData = savedData;
    }

    @Override
    public void run() {
        this.savedData.setDirty();
    }
}

