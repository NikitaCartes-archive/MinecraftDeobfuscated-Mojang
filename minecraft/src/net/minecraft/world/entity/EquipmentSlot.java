package net.minecraft.world.entity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

public enum EquipmentSlot implements StringRepresentable {
	MAINHAND(EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
	OFFHAND(EquipmentSlot.Type.HAND, 1, 5, "offhand"),
	FEET(EquipmentSlot.Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
	LEGS(EquipmentSlot.Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
	CHEST(EquipmentSlot.Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
	HEAD(EquipmentSlot.Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
	BODY(EquipmentSlot.Type.ANIMAL_ARMOR, 0, 1, 6, "body");

	public static final int NO_COUNT_LIMIT = 0;
	public static final StringRepresentable.EnumCodec<EquipmentSlot> CODEC = StringRepresentable.fromEnum(EquipmentSlot::values);
	private final EquipmentSlot.Type type;
	private final int index;
	private final int countLimit;
	private final int filterFlag;
	private final String name;

	private EquipmentSlot(final EquipmentSlot.Type type, final int j, final int k, final int l, final String string2) {
		this.type = type;
		this.index = j;
		this.countLimit = k;
		this.filterFlag = l;
		this.name = string2;
	}

	private EquipmentSlot(final EquipmentSlot.Type type, final int j, final int k, final String string2) {
		this(type, j, 0, k, string2);
	}

	public EquipmentSlot.Type getType() {
		return this.type;
	}

	public int getIndex() {
		return this.index;
	}

	public int getIndex(int i) {
		return i + this.index;
	}

	public ItemStack limit(ItemStack itemStack) {
		return this.countLimit > 0 ? itemStack.split(this.countLimit) : itemStack;
	}

	public int getFilterFlag() {
		return this.filterFlag;
	}

	public String getName() {
		return this.name;
	}

	public boolean isArmor() {
		return this.type == EquipmentSlot.Type.HUMANOID_ARMOR || this.type == EquipmentSlot.Type.ANIMAL_ARMOR;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static EquipmentSlot byName(String string) {
		EquipmentSlot equipmentSlot = (EquipmentSlot)CODEC.byName(string);
		if (equipmentSlot != null) {
			return equipmentSlot;
		} else {
			throw new IllegalArgumentException("Invalid slot '" + string + "'");
		}
	}

	public static enum Type {
		HAND,
		HUMANOID_ARMOR,
		ANIMAL_ARMOR;
	}
}
