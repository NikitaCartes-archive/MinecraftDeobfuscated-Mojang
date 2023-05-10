package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
	private final LootParams params;
	private final RandomSource random;
	private final LootDataResolver lootDataResolver;
	private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.<LootContext.VisitedEntry<?>>newLinkedHashSet();

	LootContext(LootParams lootParams, RandomSource randomSource, LootDataResolver lootDataResolver) {
		this.params = lootParams;
		this.random = randomSource;
		this.lootDataResolver = lootDataResolver;
	}

	public boolean hasParam(LootContextParam<?> lootContextParam) {
		return this.params.hasParam(lootContextParam);
	}

	public <T> T getParam(LootContextParam<T> lootContextParam) {
		return this.params.getParameter(lootContextParam);
	}

	public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
		this.params.addDynamicDrops(resourceLocation, consumer);
	}

	@Nullable
	public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
		return this.params.getParamOrNull(lootContextParam);
	}

	public boolean hasVisitedElement(LootContext.VisitedEntry<?> visitedEntry) {
		return this.visitedElements.contains(visitedEntry);
	}

	public boolean pushVisitedElement(LootContext.VisitedEntry<?> visitedEntry) {
		return this.visitedElements.add(visitedEntry);
	}

	public void popVisitedElement(LootContext.VisitedEntry<?> visitedEntry) {
		this.visitedElements.remove(visitedEntry);
	}

	public LootDataResolver getResolver() {
		return this.lootDataResolver;
	}

	public RandomSource getRandom() {
		return this.random;
	}

	public float getLuck() {
		return this.params.getLuck();
	}

	public ServerLevel getLevel() {
		return this.params.getLevel();
	}

	public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable lootTable) {
		return new LootContext.VisitedEntry<>(LootDataType.TABLE, lootTable);
	}

	public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition lootItemCondition) {
		return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, lootItemCondition);
	}

	public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction lootItemFunction) {
		return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, lootItemFunction);
	}

	public static class Builder {
		private final LootParams params;
		@Nullable
		private RandomSource random;

		public Builder(LootParams lootParams) {
			this.params = lootParams;
		}

		public LootContext.Builder withOptionalRandomSeed(long l) {
			if (l != 0L) {
				this.random = RandomSource.create(l);
			}

			return this;
		}

		public ServerLevel getLevel() {
			return this.params.getLevel();
		}

		public LootContext create(ResourceLocation resourceLocation) {
			ServerLevel serverLevel = this.getLevel();
			MinecraftServer minecraftServer = serverLevel.getServer();
			RandomSource randomSource;
			if (this.random != null) {
				randomSource = this.random;
			} else {
				randomSource = serverLevel.getRandomSequence(resourceLocation);
			}

			return new LootContext(this.params, randomSource, minecraftServer.getLootData());
		}
	}

	public static enum EntityTarget {
		THIS("this", LootContextParams.THIS_ENTITY),
		KILLER("killer", LootContextParams.KILLER_ENTITY),
		DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
		KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

		final String name;
		private final LootContextParam<? extends Entity> param;

		private EntityTarget(String string2, LootContextParam<? extends Entity> lootContextParam) {
			this.name = string2;
			this.param = lootContextParam;
		}

		public LootContextParam<? extends Entity> getParam() {
			return this.param;
		}

		public static LootContext.EntityTarget getByName(String string) {
			for (LootContext.EntityTarget entityTarget : values()) {
				if (entityTarget.name.equals(string)) {
					return entityTarget;
				}
			}

			throw new IllegalArgumentException("Invalid entity target " + string);
		}

		public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
			public void write(JsonWriter jsonWriter, LootContext.EntityTarget entityTarget) throws IOException {
				jsonWriter.value(entityTarget.name);
			}

			public LootContext.EntityTarget read(JsonReader jsonReader) throws IOException {
				return LootContext.EntityTarget.getByName(jsonReader.nextString());
			}
		}
	}

	public static record VisitedEntry<T>(LootDataType<T> type, T value) {
	}
}
