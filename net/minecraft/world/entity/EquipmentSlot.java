/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

public enum EquipmentSlot {
    MAINHAND(Type.HAND, 0, 0, "mainhand"),
    OFFHAND(Type.HAND, 1, 5, "offhand"),
    FEET(Type.ARMOR, 0, 1, "feet"),
    LEGS(Type.ARMOR, 1, 2, "legs"),
    CHEST(Type.ARMOR, 2, 3, "chest"),
    HEAD(Type.ARMOR, 3, 4, "head");

    private final Type type;
    private final int index;
    private final int filterFlag;
    private final String name;

    private EquipmentSlot(Type type, int j, int k, String string2) {
        this.type = type;
        this.index = j;
        this.filterFlag = k;
        this.name = string2;
    }

    public Type getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public int getFilterFlag() {
        return this.filterFlag;
    }

    public String getName() {
        return this.name;
    }

    public static EquipmentSlot byName(String string) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (!equipmentSlot.getName().equals(string)) continue;
            return equipmentSlot;
        }
        throw new IllegalArgumentException("Invalid slot '" + string + "'");
    }

    public static EquipmentSlot byTypeAndIndex(Type type, int i) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != type || equipmentSlot.getIndex() != i) continue;
            return equipmentSlot;
        }
        throw new IllegalArgumentException("Invalid slot '" + (Object)((Object)type) + "': " + i);
    }

    public static enum Type {
        HAND,
        ARMOR;

    }
}

