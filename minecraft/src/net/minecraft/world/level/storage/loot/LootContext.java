package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
	private final Random random;
	private final float luck;
	private final ServerLevel level;
	private final Function<ResourceLocation, LootTable> lootTables;
	private final Set<LootTable> visitedTables = Sets.<LootTable>newLinkedHashSet();
	private final Function<ResourceLocation, LootItemCondition> conditions;
	private final Set<LootItemCondition> visitedConditions = Sets.<LootItemCondition>newLinkedHashSet();
	private final Map<LootContextParam<?>, Object> params;
	private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops;

	private LootContext(
		Random random,
		float f,
		ServerLevel serverLevel,
		Function<ResourceLocation, LootTable> function,
		Function<ResourceLocation, LootItemCondition> function2,
		Map<LootContextParam<?>, Object> map,
		Map<ResourceLocation, LootContext.DynamicDrop> map2
	) {
		this.random = random;
		this.luck = f;
		this.level = serverLevel;
		this.lootTables = function;
		this.conditions = function2;
		this.params = ImmutableMap.copyOf(map);
		this.dynamicDrops = ImmutableMap.copyOf(map2);
	}

	public boolean hasParam(LootContextParam<?> lootContextParam) {
		return this.params.containsKey(lootContextParam);
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

	public boolean addVisitedTable(LootTable lootTable) {
		return this.visitedTables.add(lootTable);
	}

	public void removeVisitedTable(LootTable lootTable) {
		this.visitedTables.remove(lootTable);
	}

	public boolean addVisitedCondition(LootItemCondition lootItemCondition) {
		return this.visitedConditions.add(lootItemCondition);
	}

	public void removeVisitedCondition(LootItemCondition lootItemCondition) {
		this.visitedConditions.remove(lootItemCondition);
	}

	public LootTable getLootTable(ResourceLocation resourceLocation) {
		return (LootTable)this.lootTables.apply(resourceLocation);
	}

	public LootItemCondition getCondition(ResourceLocation resourceLocation) {
		return (LootItemCondition)this.conditions.apply(resourceLocation);
	}

	public Random getRandom() {
		return this.random;
	}

	public float getLuck() {
		return this.luck;
	}

	public ServerLevel getLevel() {
		return this.level;
	}

	public static class Builder {
		private final ServerLevel level;
		private final Map<LootContextParam<?>, Object> params = Maps.<LootContextParam<?>, Object>newIdentityHashMap();
		private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops = Maps.<ResourceLocation, LootContext.DynamicDrop>newHashMap();
		private Random random;
		private float luck;

		public Builder(ServerLevel serverLevel) {
			this.level = serverLevel;
		}

		public LootContext.Builder withRandom(Random random) {
			this.random = random;
			return this;
		}

		public LootContext.Builder withOptionalRandomSeed(long l) {
			if (l != 0L) {
				this.random = new Random(l);
			}

			return this;
		}

		public LootContext.Builder withOptionalRandomSeed(long l, Random random) {
			if (l == 0L) {
				this.random = random;
			} else {
				this.random = new Random(l);
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
					Random random = this.random;
					if (random == null) {
						random = new Random();
					}

					MinecraftServer minecraftServer = this.level.getServer();
					return new LootContext(
						random, this.luck, this.level, minecraftServer.getLootTables()::get, minecraftServer.getPredicateManager()::get, this.params, this.dynamicDrops
					);
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

		private final String name;
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
}
