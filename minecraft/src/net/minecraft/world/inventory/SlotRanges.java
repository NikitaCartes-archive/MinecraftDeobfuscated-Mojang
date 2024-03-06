package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public class SlotRanges {
	private static final List<SlotRange> SLOTS = Util.make(new ArrayList(), arrayList -> {
		addSlotRange(arrayList, "container.", 0, 54);
		addSlotRange(arrayList, "hotbar.", 0, 9);
		addSlotRange(arrayList, "inventory.", 9, 27);
		addSlotRange(arrayList, "enderchest.", 200, 27);
		addSlotRange(arrayList, "villager.", 300, 8);
		addSlotRange(arrayList, "horse.", 500, 15);
		int i = EquipmentSlot.MAINHAND.getIndex(98);
		int j = EquipmentSlot.OFFHAND.getIndex(98);
		addSingleSlot(arrayList, "weapon", i);
		addSingleSlot(arrayList, "weapon.mainhand", i);
		addSingleSlot(arrayList, "weapon.offhand", j);
		addSlotRange(arrayList, "weapon.*", i, j);
		i = EquipmentSlot.HEAD.getIndex(100);
		j = EquipmentSlot.CHEST.getIndex(100);
		int k = EquipmentSlot.LEGS.getIndex(100);
		int l = EquipmentSlot.FEET.getIndex(100);
		int m = EquipmentSlot.BODY.getIndex(105);
		addSingleSlot(arrayList, "armor.head", i);
		addSingleSlot(arrayList, "armor.chest", j);
		addSingleSlot(arrayList, "armor.legs", k);
		addSingleSlot(arrayList, "armor.feet", l);
		addSingleSlot(arrayList, "armor.body", m);
		addSlotRange(arrayList, "armor.*", i, j, k, l, m);
		addSingleSlot(arrayList, "horse.saddle", 400);
		addSingleSlot(arrayList, "horse.chest", 499);
		addSingleSlot(arrayList, "player.cursor", 499);
		addSlotRange(arrayList, "player.crafting.", 500, 4);
	});
	public static final Codec<SlotRange> CODEC = StringRepresentable.fromValues(() -> (SlotRange[])SLOTS.toArray(new SlotRange[0]));
	private static final Function<String, SlotRange> NAME_LOOKUP = StringRepresentable.createNameLookup(
		(SlotRange[])SLOTS.toArray(new SlotRange[0]), string -> string
	);

	private static SlotRange create(String string, int i) {
		return SlotRange.of(string, IntLists.singleton(i));
	}

	private static SlotRange create(String string, IntList intList) {
		return SlotRange.of(string, IntLists.unmodifiable(intList));
	}

	private static SlotRange create(String string, int... is) {
		return SlotRange.of(string, IntList.of(is));
	}

	private static void addSingleSlot(List<SlotRange> list, String string, int i) {
		list.add(create(string, i));
	}

	private static void addSlotRange(List<SlotRange> list, String string, int i, int j) {
		IntList intList = new IntArrayList(j);

		for (int k = 0; k < j; k++) {
			int l = i + k;
			list.add(create(string + k, l));
			intList.add(l);
		}

		list.add(create(string + "*", intList));
	}

	private static void addSlotRange(List<SlotRange> list, String string, int... is) {
		list.add(create(string, is));
	}

	@Nullable
	public static SlotRange nameToIds(String string) {
		return (SlotRange)NAME_LOOKUP.apply(string);
	}

	public static Stream<String> allNames() {
		return SLOTS.stream().map(StringRepresentable::getSerializedName);
	}

	public static Stream<String> singleSlotNames() {
		return SLOTS.stream().filter(slotRange -> slotRange.size() == 1).map(StringRepresentable::getSerializedName);
	}
}
