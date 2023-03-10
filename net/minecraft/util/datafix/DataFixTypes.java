/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
    LEVEL(References.LEVEL),
    PLAYER(References.PLAYER),
    CHUNK(References.CHUNK),
    HOTBAR(References.HOTBAR),
    OPTIONS(References.OPTIONS),
    STRUCTURE(References.STRUCTURE),
    STATS(References.STATS),
    SAVED_DATA(References.SAVED_DATA),
    ADVANCEMENTS(References.ADVANCEMENTS),
    POI_CHUNK(References.POI_CHUNK),
    WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(References.ENTITY_CHUNK);

    public static final Set<DSL.TypeReference> TYPES_FOR_LEVEL_LIST;
    private final DSL.TypeReference type;

    private DataFixTypes(DSL.TypeReference typeReference) {
        this.type = typeReference;
    }

    private static int currentVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int i, int j) {
        return dataFixer.update(this.type, dynamic, i, j);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer dataFixer, Dynamic<T> dynamic, int i) {
        return this.update(dataFixer, dynamic, i, DataFixTypes.currentVersion());
    }

    public CompoundTag update(DataFixer dataFixer, CompoundTag compoundTag, int i, int j) {
        return this.update(dataFixer, new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag), i, j).getValue();
    }

    public CompoundTag updateToCurrentVersion(DataFixer dataFixer, CompoundTag compoundTag, int i) {
        return this.update(dataFixer, compoundTag, i, DataFixTypes.currentVersion());
    }

    static {
        TYPES_FOR_LEVEL_LIST = Set.of(DataFixTypes.LEVEL.type);
    }
}

