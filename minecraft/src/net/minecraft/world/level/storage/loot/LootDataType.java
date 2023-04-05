package net.minecraft.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootDataType<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(
		Deserializers.createConditionSerializer().create(),
		createSingleOrMultipleDeserialiser(LootItemCondition.class, LootDataManager::createComposite),
		"predicates",
		createSimpleValidator()
	);
	public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(
		Deserializers.createFunctionSerializer().create(),
		createSingleOrMultipleDeserialiser(LootItemFunction.class, LootDataManager::createComposite),
		"item_modifiers",
		createSimpleValidator()
	);
	public static final LootDataType<LootTable> TABLE = new LootDataType<>(
		Deserializers.createLootTableSerializer().create(), createSingleDeserialiser(LootTable.class), "loot_tables", createLootTableValidator()
	);
	private final Gson parser;
	private final BiFunction<ResourceLocation, JsonElement, Optional<T>> topDeserializer;
	private final String directory;
	private final LootDataType.Validator<T> validator;

	private LootDataType(
		Gson gson, BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> biFunction, String string, LootDataType.Validator<T> validator
	) {
		this.parser = gson;
		this.directory = string;
		this.validator = validator;
		this.topDeserializer = (BiFunction<ResourceLocation, JsonElement, Optional<T>>)biFunction.apply(gson, string);
	}

	public Gson parser() {
		return this.parser;
	}

	public String directory() {
		return this.directory;
	}

	public void runValidation(ValidationContext validationContext, LootDataId<T> lootDataId, T object) {
		this.validator.run(validationContext, lootDataId, object);
	}

	public Optional<T> deserialize(ResourceLocation resourceLocation, JsonElement jsonElement) {
		return (Optional<T>)this.topDeserializer.apply(resourceLocation, jsonElement);
	}

	public static Stream<LootDataType<?>> values() {
		return Stream.of(PREDICATE, MODIFIER, TABLE);
	}

	private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleDeserialiser(Class<T> class_) {
		return (gson, string) -> (resourceLocation, jsonElement) -> {
				try {
					return Optional.of(gson.fromJson(jsonElement, class_));
				} catch (Exception var6) {
					LOGGER.error("Couldn't parse element {}:{}", string, resourceLocation, var6);
					return Optional.empty();
				}
			};
	}

	private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleOrMultipleDeserialiser(
		Class<T> class_, Function<T[], T> function
	) {
		Class<T[]> class2 = class_.arrayType();
		return (gson, string) -> (resourceLocation, jsonElement) -> {
				try {
					if (jsonElement.isJsonArray()) {
						T[] objects = (T[])((Object[])gson.fromJson(jsonElement, class2));
						return Optional.of(function.apply(objects));
					} else {
						return Optional.of(gson.fromJson(jsonElement, class_));
					}
				} catch (Exception var8) {
					LOGGER.error("Couldn't parse element {}:{}", string, resourceLocation, var8);
					return Optional.empty();
				}
			};
	}

	private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
		return (validationContext, lootDataId, lootContextUser) -> lootContextUser.validate(
				validationContext.enterElement("{" + lootDataId.type().directory + ":" + lootDataId.location() + "}", lootDataId)
			);
	}

	private static LootDataType.Validator<LootTable> createLootTableValidator() {
		return (validationContext, lootDataId, lootTable) -> lootTable.validate(
				validationContext.setParams(lootTable.getParamSet()).enterElement("{" + lootDataId.type().directory + ":" + lootDataId.location() + "}", lootDataId)
			);
	}

	@FunctionalInterface
	public interface Validator<T> {
		void run(ValidationContext validationContext, LootDataId<T> lootDataId, T object);
	}
}
