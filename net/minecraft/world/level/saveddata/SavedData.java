/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public abstract class SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean dirty;

    public abstract CompoundTag save(CompoundTag var1);

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean bl) {
        this.dirty = bl;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void save(File file) {
        if (!this.isDirty()) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag()));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            NbtIo.writeCompressed(compoundTag, file);
        } catch (IOException iOException) {
            LOGGER.error("Could not save data {}", (Object)this, (Object)iOException);
        }
        this.setDirty(false);
    }
}

