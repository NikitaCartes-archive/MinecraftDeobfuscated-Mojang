package net.minecraft.world.level.storage.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public record LootDataType<T>(ResourceKey<Registry<T>> registryKey, Codec<T> codec, String directory, LootDataType.Validator<T> validator) {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(
		Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, "predicates", createSimpleValidator()
	);
	public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(
		Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, "item_modifiers", createSimpleValidator()
	);
	public static final LootDataType<LootTable> TABLE = new LootDataType<>(
		Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, "loot_tables", createLootTableValidator()
	);

	public void runValidation(ValidationContext validationContext, ResourceKey<T> resourceKey, T object) {
		this.validator.run(validationContext, resourceKey, object);
	}

	public <V> Optional<T> deserialize(ResourceLocation resourceLocation, DynamicOps<V> dynamicOps, V object) {
		DataResult<T> dataResult = this.codec.parse(dynamicOps, object);
		dataResult.error().ifPresent(error -> LOGGER.error("Couldn't parse element {}:{} - {}", this.directory, resourceLocation, error.message()));
		return dataResult.result();
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
