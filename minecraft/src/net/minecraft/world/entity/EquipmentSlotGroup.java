package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum EquipmentSlotGroup implements StringRepresentable {
	ANY(0, "any", equipmentSlot -> true),
	MAINHAND(1, "mainhand", EquipmentSlot.MAINHAND),
	OFFHAND(2, "offhand", EquipmentSlot.OFFHAND),
	HAND(3, "hand", equipmentSlot -> equipmentSlot.getType() == EquipmentSlot.Type.HAND),
	FEET(4, "feet", EquipmentSlot.FEET),
	LEGS(5, "legs", EquipmentSlot.LEGS),
	CHEST(6, "chest", EquipmentSlot.CHEST),
	HEAD(7, "head", EquipmentSlot.HEAD),
	ARMOR(8, "armor", EquipmentSlot::isArmor);

	public static final IntFunction<EquipmentSlotGroup> BY_ID = ByIdMap.continuous(
		equipmentSlotGroup -> equipmentSlotGroup.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
	);
	public static final Codec<EquipmentSlotGroup> CODEC = StringRepresentable.fromEnum(EquipmentSlotGroup::values);
	public static final StreamCodec<ByteBuf, EquipmentSlotGroup> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, equipmentSlotGroup -> equipmentSlotGroup.id);
	private final int id;
	private final String key;
	private final Predicate<EquipmentSlot> predicate;

	private EquipmentSlotGroup(int j, String string2, Predicate<EquipmentSlot> predicate) {
		this.id = j;
		this.key = string2;
		this.predicate = predicate;
	}

	private EquipmentSlotGroup(int j, String string2, EquipmentSlot equipmentSlot) {
		this(j, string2, equipmentSlot2 -> equipmentSlot2 == equipmentSlot);
	}

	@Override
	public String getSerializedName() {
		return this.key;
	}

	public boolean test(EquipmentSlot equipmentSlot) {
		return this.predicate.test(equipmentSlot);
	}
}
