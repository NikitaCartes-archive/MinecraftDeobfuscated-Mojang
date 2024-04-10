package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
	public static final Codec<Map<EquipmentSlot, Float>> DROP_CHANCES_CODEC = Codec.either(Codec.FLOAT, Codec.unboundedMap(EquipmentSlot.CODEC, Codec.FLOAT))
		.xmap(either -> either.map(EquipmentTable::createForAllSlots, Function.identity()), map -> {
			boolean bl = map.values().stream().distinct().count() == 1L;
			boolean bl2 = map.keySet().containsAll(Arrays.asList(EquipmentSlot.values()));
			return bl && bl2 ? Either.left((Float)map.values().stream().findFirst().orElse(0.0F)) : Either.right(map);
		});
	public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(EquipmentTable::lootTable),
					DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)
				)
				.apply(instance, EquipmentTable::new)
	);

	private static Map<EquipmentSlot, Float> createForAllSlots(float f) {
		return createForAllSlots(List.of(EquipmentSlot.values()), f);
	}

	private static Map<EquipmentSlot, Float> createForAllSlots(List<EquipmentSlot> list, float f) {
		Map<EquipmentSlot, Float> map = Maps.<EquipmentSlot, Float>newHashMap();

		for (EquipmentSlot equipmentSlot : list) {
			map.put(equipmentSlot, f);
		}

		return map;
	}
}
