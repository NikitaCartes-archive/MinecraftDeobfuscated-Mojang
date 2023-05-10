package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class LootParams {
	private final ServerLevel level;
	private final Map<LootContextParam<?>, Object> params;
	private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops;
	private final float luck;

	public LootParams(ServerLevel serverLevel, Map<LootContextParam<?>, Object> map, Map<ResourceLocation, LootParams.DynamicDrop> map2, float f) {
		this.level = serverLevel;
		this.params = map;
		this.dynamicDrops = map2;
		this.luck = f;
	}

	public ServerLevel getLevel() {
		return this.level;
	}

	public boolean hasParam(LootContextParam<?> lootContextParam) {
		return this.params.containsKey(lootContextParam);
	}

	public <T> T getParameter(LootContextParam<T> lootContextParam) {
		T object = (T)this.params.get(lootContextParam);
		if (object == null) {
			throw new NoSuchElementException(lootContextParam.getName().toString());
		} else {
			return object;
		}
	}

	@Nullable
	public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
		return (T)this.params.get(lootContextParam);
	}

	@Nullable
	public <T> T getParamOrNull(LootContextParam<T> lootContextParam) {
		return (T)this.params.get(lootContextParam);
	}

	public void addDynamicDrops(ResourceLocation resourceLocation, Consumer<ItemStack> consumer) {
		LootParams.DynamicDrop dynamicDrop = (LootParams.DynamicDrop)this.dynamicDrops.get(resourceLocation);
		if (dynamicDrop != null) {
			dynamicDrop.add(consumer);
		}
	}

	public float getLuck() {
		return this.luck;
	}

	public static class Builder {
		private final ServerLevel level;
		private final Map<LootContextParam<?>, Object> params = Maps.<LootContextParam<?>, Object>newIdentityHashMap();
		private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops = Maps.<ResourceLocation, LootParams.DynamicDrop>newHashMap();
		private float luck;

		public Builder(ServerLevel serverLevel) {
			this.level = serverLevel;
		}

		public ServerLevel getLevel() {
			return this.level;
		}

		public <T> LootParams.Builder withParameter(LootContextParam<T> lootContextParam, T object) {
			this.params.put(lootContextParam, object);
			return this;
		}

		public <T> LootParams.Builder withOptionalParameter(LootContextParam<T> lootContextParam, @Nullable T object) {
			if (object == null) {
				this.params.remove(lootContextParam);
			} else {
				this.params.put(lootContextParam, object);
			}

			return this;
		}

		public <T> T getParameter(LootContextParam<T> lootContextParam) {
			T object = (T)this.params.get(lootContextParam);
			if (object == null) {
				throw new NoSuchElementException(lootContextParam.getName().toString());
			} else {
				return object;
			}
		}

		@Nullable
		public <T> T getOptionalParameter(LootContextParam<T> lootContextParam) {
			return (T)this.params.get(lootContextParam);
		}

		public LootParams.Builder withDynamicDrop(ResourceLocation resourceLocation, LootParams.DynamicDrop dynamicDrop) {
			LootParams.DynamicDrop dynamicDrop2 = (LootParams.DynamicDrop)this.dynamicDrops.put(resourceLocation, dynamicDrop);
			if (dynamicDrop2 != null) {
				throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
			} else {
				return this;
			}
		}

		public LootParams.Builder withLuck(float f) {
			this.luck = f;
			return this;
		}

		public LootParams create(LootContextParamSet lootContextParamSet) {
			Set<LootContextParam<?>> set = Sets.<LootContextParam<?>>difference(this.params.keySet(), lootContextParamSet.getAllowed());
			if (!set.isEmpty()) {
				throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
			} else {
				Set<LootContextParam<?>> set2 = Sets.<LootContextParam<?>>difference(lootContextParamSet.getRequired(), this.params.keySet());
				if (!set2.isEmpty()) {
					throw new IllegalArgumentException("Missing required parameters: " + set2);
				} else {
					return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
				}
			}
		}
	}

	@FunctionalInterface
	public interface DynamicDrop {
		void add(Consumer<ItemStack> consumer);
	}
}
