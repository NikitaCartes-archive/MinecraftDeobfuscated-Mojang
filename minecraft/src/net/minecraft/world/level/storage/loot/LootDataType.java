package net.minecraft.world.level.storage.loot;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataType<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(LootItemConditions.CODEC, "predicates", createSimpleValidator());
	public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(LootItemFunctions.CODEC, "item_modifiers", createSimpleValidator());
	public static final LootDataType<LootTable> TABLE = new LootDataType<>(LootTable.CODEC, "loot_tables", createLootTableValidator());
	private final Codec<T> codec;
	private final String directory;
	private final LootDataType.Validator<T> validator;

	private LootDataType(Codec<T> codec, String string, LootDataType.Validator<T> validator) {
		this.codec = codec;
		this.directory = string;
		this.validator = validator;
	}

	public String directory() {
		return this.directory;
	}

	public void runValidation(ValidationContext validationContext, LootDataId<T> lootDataId, T object) {
		this.validator.run(validationContext, lootDataId, object);
	}

	public <V> Optional<T> deserialize(ResourceLocation resourceLocation, DynamicOps<V> dynamicOps, V object) {
		DataResult<T> dataResult = this.codec.parse(dynamicOps, object);
		dataResult.error().ifPresent(partialResult -> LOGGER.error("Couldn't parse element {}:{} - {}", this.directory, resourceLocation, partialResult.message()));
		return dataResult.result();
	}

	public static Stream<LootDataType<?>> values() {
		return Stream.of(PREDICATE, MODIFIER, TABLE);
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
