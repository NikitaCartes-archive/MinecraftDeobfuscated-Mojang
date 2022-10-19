/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.crafting;

import net.minecraft.util.StringRepresentable;

public enum CookingBookCategory implements StringRepresentable
{
    FOOD("food"),
    BLOCKS("blocks"),
    MISC("misc");

    public static final StringRepresentable.EnumCodec<CookingBookCategory> CODEC;
    private final String name;

    private CookingBookCategory(String string2) {
        this.name = string2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CookingBookCategory::values);
    }
}

