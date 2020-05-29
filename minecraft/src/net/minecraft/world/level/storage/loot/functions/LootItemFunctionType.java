package net.minecraft.world.level.storage.loot.functions;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;

public class LootItemFunctionType extends SerializerType<LootItemFunction> {
	public LootItemFunctionType(Serializer<? extends LootItemFunction> serializer) {
		super(serializer);
	}
}
