package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
	private final LootParams params;
	private final RandomSource random;
	private final HolderGetter.Provider lootDataResolver;
	private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.<LootContext.VisitedEntry<?>>newLinkedHashSet();

	LootContext(LootParams lootParams, RandomSource randomSource, HolderGetter.Provider provider) {
		this.params = lootParams;
		this.random = randomSource;
		this.lootDataResolver = provider;
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

	public HolderGetter.Provider getResolver() {
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

		public LootContext.Builder withOptionalRandomSource(RandomSource randomSource) {
			this.random = randomSource;
			return this;
		}

		public ServerLevel getLevel() {
			return this.params.getLevel();
		}

		public LootContext create(Optional<ResourceLocation> optional) {
			ServerLevel serverLevel = this.getLevel();
			MinecraftServer minecraftServer = serverLevel.getServer();
			RandomSource randomSource = (RandomSource)Optional.ofNullable(this.random)
				.or(() -> optional.map(serverLevel::getRandomSequence))
				.orElseGet(serverLevel::getRandom);
			return new LootContext(this.params, randomSource, minecraftServer.reloadableRegistries().lookup());
		}
	}

	public static enum EntityTarget implements StringRepresentable {
		THIS("this", LootContextParams.THIS_ENTITY),
		ATTACKER("attacker", LootContextParams.ATTACKING_ENTITY),
		DIRECT_ATTACKER("direct_attacker", LootContextParams.DIRECT_ATTACKING_ENTITY),
		ATTACKING_PLAYER("attacking_player", LootContextParams.LAST_DAMAGE_PLAYER);

		public static final StringRepresentable.EnumCodec<LootContext.EntityTarget> CODEC = StringRepresentable.fromEnum(LootContext.EntityTarget::values);
		private final String name;
		private final LootContextParam<? extends Entity> param;

		private EntityTarget(final String string2, final LootContextParam<? extends Entity> lootContextParam) {
			this.name = string2;
			this.param = lootContextParam;
		}

		public LootContextParam<? extends Entity> getParam() {
			return this.param;
		}

		public static LootContext.EntityTarget getByName(String string) {
			LootContext.EntityTarget entityTarget = (LootContext.EntityTarget)CODEC.byName(string);
			if (entityTarget != null) {
				return entityTarget;
			} else {
				throw new IllegalArgumentException("Invalid entity target " + string);
			}
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static record VisitedEntry<T>(LootDataType<T> type, T value) {
	}
}
