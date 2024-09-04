package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record LootDataType<T>(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
	public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(
		Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, createSimpleValidator()
	);
	public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(
		Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, createSimpleValidator()
	);
	public static final LootDataType<LootTable> TABLE = new LootDataType<>(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, createLootTableValidator());

	public void runValidation(ValidationContext validationContext, ResourceKey<T> resourceKey, T object) {
		this.validator.run(validationContext, resourceKey, object);
	}

	public static Stream<LootDataType<?>> values() {
		return Stream.of(PREDICATE, MODIFIER, TABLE);
	}

	private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
		return (validationContext, resourceKey, lootContextUser) -> lootContextUser.validate(
				validationContext.enterElement("{" + resourceKey.registry() + "/" + resourceKey.location() + "}", resourceKey)
			);
	}

	private static LootDataType.Validator<LootTable> createLootTableValidator() {
		return (validationContext, resourceKey, lootTable) -> lootTable.validate(
				validationContext.setParams(lootTable.getParamSet()).enterElement("{" + resourceKey.registry() + "/" + resourceKey.location() + "}", resourceKey)
			);
	}

	@FunctionalInterface
	public interface Validator<T> {
		void run(ValidationContext validationContext, ResourceKey<T> resourceKey, T object);
	}
}
