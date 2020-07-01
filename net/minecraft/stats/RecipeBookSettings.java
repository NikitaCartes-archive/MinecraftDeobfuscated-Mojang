/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
    private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(RecipeBookType.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookType.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookType.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookType.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
    private final Map<RecipeBookType, TypeSettings> states;

    private RecipeBookSettings(Map<RecipeBookType, TypeSettings> map) {
        this.states = map;
    }

    public RecipeBookSettings() {
        this(Util.make(Maps.newEnumMap(RecipeBookType.class), enumMap -> {
            for (RecipeBookType recipeBookType : RecipeBookType.values()) {
                enumMap.put(recipeBookType, new TypeSettings(false, false));
            }
        }));
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isOpen(RecipeBookType recipeBookType) {
        return this.states.get((Object)recipeBookType).open;
    }

    public void setOpen(RecipeBookType recipeBookType, boolean bl) {
        this.states.get((Object)recipeBookType).open = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isFiltering(RecipeBookType recipeBookType) {
        return this.states.get((Object)recipeBookType).filtering;
    }

    public void setFiltering(RecipeBookType recipeBookType, boolean bl) {
        this.states.get((Object)recipeBookType).filtering = bl;
    }

    public static RecipeBookSettings read(FriendlyByteBuf friendlyByteBuf) {
        EnumMap<RecipeBookType, TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);
        for (RecipeBookType recipeBookType : RecipeBookType.values()) {
            boolean bl = friendlyByteBuf.readBoolean();
            boolean bl2 = friendlyByteBuf.readBoolean();
            map.put(recipeBookType, new TypeSettings(bl, bl2));
        }
        return new RecipeBookSettings(map);
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        for (RecipeBookType recipeBookType : RecipeBookType.values()) {
            TypeSettings typeSettings = this.states.get((Object)recipeBookType);
            if (typeSettings == null) {
                friendlyByteBuf.writeBoolean(false);
                friendlyByteBuf.writeBoolean(false);
                continue;
            }
            friendlyByteBuf.writeBoolean(typeSettings.open);
            friendlyByteBuf.writeBoolean(typeSettings.filtering);
        }
    }

    public static RecipeBookSettings read(CompoundTag compoundTag) {
        EnumMap<RecipeBookType, TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);
        TAG_FIELDS.forEach((recipeBookType, pair) -> {
            boolean bl = compoundTag.getBoolean((String)pair.getFirst());
            boolean bl2 = compoundTag.getBoolean((String)pair.getSecond());
            map.put((RecipeBookType)((Object)recipeBookType), new TypeSettings(bl, bl2));
        });
        return new RecipeBookSettings(map);
    }

    public void write(CompoundTag compoundTag) {
        TAG_FIELDS.forEach((recipeBookType, pair) -> {
            TypeSettings typeSettings = this.states.get(recipeBookType);
            compoundTag.putBoolean((String)pair.getFirst(), typeSettings.open);
            compoundTag.putBoolean((String)pair.getSecond(), typeSettings.filtering);
        });
    }

    public RecipeBookSettings copy() {
        EnumMap<RecipeBookType, TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);
        for (RecipeBookType recipeBookType : RecipeBookType.values()) {
            TypeSettings typeSettings = this.states.get((Object)recipeBookType);
            map.put(recipeBookType, typeSettings.copy());
        }
        return new RecipeBookSettings(map);
    }

    public void replaceFrom(RecipeBookSettings recipeBookSettings) {
        this.states.clear();
        for (RecipeBookType recipeBookType : RecipeBookType.values()) {
            TypeSettings typeSettings = recipeBookSettings.states.get((Object)recipeBookType);
            this.states.put(recipeBookType, typeSettings.copy());
        }
    }

    public boolean equals(Object object) {
        return this == object || object instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)object).states);
    }

    public int hashCode() {
        return this.states.hashCode();
    }

    static final class TypeSettings {
        private boolean open;
        private boolean filtering;

        public TypeSettings(boolean bl, boolean bl2) {
            this.open = bl;
            this.filtering = bl2;
        }

        public TypeSettings copy() {
            return new TypeSettings(this.open, this.filtering);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof TypeSettings) {
                TypeSettings typeSettings = (TypeSettings)object;
                return this.open == typeSettings.open && this.filtering == typeSettings.filtering;
            }
            return false;
        }

        public int hashCode() {
            int i = this.open ? 1 : 0;
            i = 31 * i + (this.filtering ? 1 : 0);
            return i;
        }

        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + ']';
        }
    }
}

