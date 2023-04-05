package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
	private final RandomSource random;
	private final float luck;
	private final ServerLevel level;
	private final LootDataResolver lootDataResolver;
	private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.<LootContext.VisitedEntry<?>>newLinkedHashSet();
	private final Map<LootContextParam<?>, Object> params;
	private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops;

	LootContext(
		RandomSource randomSource,
		float f,
		ServerLevel serverLevel,
		LootDataResolver lootDataResolver,
		Map<LootContextParam<?>, Object> map,
		Map<ResourceLocation, LootContext.DynamicDrop> map2
	) {
		this.random = randomSource;
		this.luck = f;
		this.level = serverLevel;
		this.lootDataResolver = lootDataResolver;
		this.params = ImmutableMap.copyOf(map);
		this.dynamicDrops = ImmutableMap.copyOf(map2);
	}

	public boolean hasParam(LootContextParam<?> lootContextParam) {
		return this.params.containsKey(lootContextParam);
	}

	public <T> T getParam(LootContextParam<T> lootContextParam) {
		T object = (T)this.params.get(lootContextParam);
		if (object == null) {
			throw new NoSuchElementException(lootContextParam.getName().toString());
		} else {
			return object;
		}
	}

	public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
		LootContext.DynamicDrop dynamicDrop = (LootContext.DynamicDrop)this.dynamicDrops.get(resourceLocation);
		if (dynamicDrop != null) {
			dynamicDrop.add(this, consumer);
		}
	}

	@Nullable
	public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
		return (T)this.params.get(lootContextParam);
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
		return this.luck;
	}

	public ServerLevel getLevel() {
		return this.level;
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
		private final ServerLevel level;
		private final Map<LootContextParam<?>, Object> params = Maps.<LootContextParam<?>, Object>newIdentityHashMap();
		private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops = Maps.<ResourceLocation, LootContext.DynamicDrop>newHashMap();
		@Nullable
		private RandomSource random;
		private float luck;

		public Builder(ServerLevel serverLevel) {
			this.level = serverLevel;
		}

		public LootContext.Builder withRandom(RandomSource randomSource) {
			this.random = randomSource;
			return this;
		}

		public LootContext.Builder withOptionalRandomSeed(long l) {
			if (l != 0L) {
				this.random = RandomSource.create(l);
			}

			return this;
		}

		public LootContext.Builder withOptionalRandomSeed(long l, RandomSource randomSource) {
			if (l == 0L) {
				this.random = randomSource;
			} else {
				this.random = RandomSource.create(l);
			}

			return this;
		}

		public LootContext.Builder withLuck(float f) {
			this.luck = f;
			return this;
		}

		public <T> LootContext.Builder withParameter(LootContextParam<T> lootContextParam, T object) {
			this.params.put(lootContextParam, object);
			return this;
		}

		public <T> LootContext.Builder withOptionalParameter(LootContextParam<T> lootContextParam, @Nullable T object) {
			if (object == null) {
				this.params.remove(lootContextParam);
			} else {
				this.params.put(lootContextParam, object);
			}

			return this;
		}

		public LootContext.Builder withDynamicDrop(ResourceLocation resourceLocation, LootContext.DynamicDrop dynamicDrop) {
			LootContext.DynamicDrop dynamicDrop2 = (LootContext.DynamicDrop)this.dynamicDrops.put(resourceLocation, dynamicDrop);
			if (dynamicDrop2 != null) {
				throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
			} else {
				return this;
			}
		}

		public ServerLevel getLevel() {
			return this.level;
		}

		public <T> T getParameter(LootContextParam<T> lootContextParam) {
			T object = (T)this.params.get(lootContextParam);
			if (object == null) {
				throw new IllegalArgumentException("No parameter " + lootContextParam);
			} else {
				return object;
			}
		}

		@Nullable
		public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
			return (T)this.params.get(lootContextParam);
		}

		public LootContext create(LootContextParamSet lootContextParamSet) {
			Set<LootContextParam<?>> set = Sets.<LootContextParam<?>>difference(this.params.keySet(), lootContextParamSet.getAllowed());
			if (!set.isEmpty()) {
				throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
			} else {
				Set<LootContextParam<?>> set2 = Sets.<LootContextParam<?>>difference(lootContextParamSet.getRequired(), this.params.keySet());
				if (!set2.isEmpty()) {
					throw new IllegalArgumentException("Missing required parameters: " + set2);
				} else {
					RandomSource randomSource = this.random;
					if (randomSource == null) {
						randomSource = RandomSource.create();
					}

					MinecraftServer minecraftServer = this.level.getServer();
					return new LootContext(randomSource, this.luck, this.level, minecraftServer.getLootData(), this.params, this.dynamicDrops);
				}
			}
		}
	}

	@FunctionalInterface
	public interface DynamicDrop {
		void add(LootContext lootContext, Consumer<ItemStack> consumer);
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
