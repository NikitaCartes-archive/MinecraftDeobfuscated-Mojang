package net.minecraft.advancements.critereon;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record PlayerPredicate(
	MinMaxBounds.Ints level,
	Optional<GameType> gameType,
	List<PlayerPredicate.StatMatcher<?>> stats,
	Object2BooleanMap<ResourceLocation> recipes,
	Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements,
	Optional<EntityPredicate> lookingAt
) implements EntitySubPredicate {
	public static final int LOOKING_AT_RANGE = 100;
	public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "level", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level),
					GameType.CODEC.optionalFieldOf("gamemode").forGetter(PlayerPredicate::gameType),
					ExtraCodecs.strictOptionalField(PlayerPredicate.StatMatcher.CODEC.listOf(), "stats", List.of()).forGetter(PlayerPredicate::stats),
					ExtraCodecs.strictOptionalField(ExtraCodecs.object2BooleanMap(ResourceLocation.CODEC), "recipes", Object2BooleanMaps.emptyMap())
						.forGetter(PlayerPredicate::recipes),
					ExtraCodecs.strictOptionalField(Codec.unboundedMap(ResourceLocation.CODEC, PlayerPredicate.AdvancementPredicate.CODEC), "advancements", Map.of())
						.forGetter(PlayerPredicate::advancements),
					ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "looking_at").forGetter(PlayerPredicate::lookingAt)
				)
				.apply(instance, PlayerPredicate::new)
	);

	@Override
	public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
		if (!(entity instanceof ServerPlayer serverPlayer)) {
			return false;
		} else if (!this.level.matches(serverPlayer.experienceLevel)) {
			return false;
		} else if (this.gameType.isPresent() && this.gameType.get() != serverPlayer.gameMode.getGameModeForPlayer()) {
			return false;
		} else {
			StatsCounter statsCounter = serverPlayer.getStats();

			for (PlayerPredicate.StatMatcher<?> statMatcher : this.stats) {
				if (!statMatcher.matches(statsCounter)) {
					return false;
				}
			}

			RecipeBook recipeBook = serverPlayer.getRecipeBook();

			for (Entry<ResourceLocation> entry : this.recipes.object2BooleanEntrySet()) {
				if (recipeBook.contains((ResourceLocation)entry.getKey()) != entry.getBooleanValue()) {
					return false;
				}
			}

			if (!this.advancements.isEmpty()) {
				PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
				ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();

				for (java.util.Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry2 : this.advancements.entrySet()) {
					Advancement advancement = serverAdvancementManager.getAdvancement((ResourceLocation)entry2.getKey());
					if (advancement == null || !((PlayerPredicate.AdvancementPredicate)entry2.getValue()).test(playerAdvancements.getOrStartProgress(advancement))) {
						return false;
					}
				}
			}

			if (this.lookingAt.isPresent()) {
				Vec3 vec32 = serverPlayer.getEyePosition();
				Vec3 vec33 = serverPlayer.getViewVector(1.0F);
				Vec3 vec34 = vec32.add(vec33.x * 100.0, vec33.y * 100.0, vec33.z * 100.0);
				EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
					serverPlayer.level(), serverPlayer, vec32, vec34, new AABB(vec32, vec34).inflate(1.0), entityx -> !entityx.isSpectator(), 0.0F
				);
				if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
					return false;
				}

				Entity entity2 = entityHitResult.getEntity();
				if (!((EntityPredicate)this.lookingAt.get()).matches(serverPlayer, entity2) || !serverPlayer.hasLineOfSight(entity2)) {
					return false;
				}
			}

			return true;
		}
	}

	@Override
	public EntitySubPredicate.Type type() {
		return EntitySubPredicate.Types.PLAYER;
	}

	static record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements PlayerPredicate.AdvancementPredicate {
		public static final Codec<PlayerPredicate.AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING)
			.xmap(PlayerPredicate.AdvancementCriterionsPredicate::new, PlayerPredicate.AdvancementCriterionsPredicate::criterions);

		public boolean test(AdvancementProgress advancementProgress) {
			for (Entry<String> entry : this.criterions.object2BooleanEntrySet()) {
				CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
				if (criterionProgress == null || criterionProgress.isDone() != entry.getBooleanValue()) {
					return false;
				}
			}

			return true;
		}
	}

	static record AdvancementDonePredicate(boolean state) implements PlayerPredicate.AdvancementPredicate {
		public static final Codec<PlayerPredicate.AdvancementDonePredicate> CODEC = Codec.BOOL
			.xmap(PlayerPredicate.AdvancementDonePredicate::new, PlayerPredicate.AdvancementDonePredicate::state);

		public boolean test(AdvancementProgress advancementProgress) {
			return advancementProgress.isDone() == this.state;
		}
	}

	interface AdvancementPredicate extends Predicate<AdvancementProgress> {
		Codec<PlayerPredicate.AdvancementPredicate> CODEC = Codec.either(
				PlayerPredicate.AdvancementDonePredicate.CODEC, PlayerPredicate.AdvancementCriterionsPredicate.CODEC
			)
			.xmap(
				either -> either.map(advancementDonePredicate -> advancementDonePredicate, advancementCriterionsPredicate -> advancementCriterionsPredicate),
				advancementPredicate -> {
					if (advancementPredicate instanceof PlayerPredicate.AdvancementDonePredicate advancementDonePredicate) {
						return Either.left(advancementDonePredicate);
					} else if (advancementPredicate instanceof PlayerPredicate.AdvancementCriterionsPredicate advancementCriterionsPredicate) {
						return Either.right(advancementCriterionsPredicate);
					} else {
						throw new UnsupportedOperationException();
					}
				}
			);
	}

	public static class Builder {
		private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
		private Optional<GameType> gameType = Optional.empty();
		private final ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> stats = ImmutableList.builder();
		private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
		private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.<ResourceLocation, PlayerPredicate.AdvancementPredicate>newHashMap();
		private Optional<EntityPredicate> lookingAt = Optional.empty();

		public static PlayerPredicate.Builder player() {
			return new PlayerPredicate.Builder();
		}

		public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints ints) {
			this.level = ints;
			return this;
		}

		public <T> PlayerPredicate.Builder addStat(StatType<T> statType, Holder.Reference<T> reference, MinMaxBounds.Ints ints) {
			this.stats.add(new PlayerPredicate.StatMatcher<>(statType, reference, ints));
			return this;
		}

		public PlayerPredicate.Builder addRecipe(ResourceLocation resourceLocation, boolean bl) {
			this.recipes.put(resourceLocation, bl);
			return this;
		}

		public PlayerPredicate.Builder setGameType(GameType gameType) {
			this.gameType = Optional.of(gameType);
			return this;
		}

		public PlayerPredicate.Builder setLookingAt(Optional<EntityPredicate> optional) {
			this.lookingAt = optional;
			return this;
		}

		public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation resourceLocation, boolean bl) {
			this.advancements.put(resourceLocation, new PlayerPredicate.AdvancementDonePredicate(bl));
			return this;
		}

		public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation resourceLocation, Map<String, Boolean> map) {
			this.advancements.put(resourceLocation, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(map)));
			return this;
		}

		public PlayerPredicate build() {
			return new PlayerPredicate(this.level, this.gameType, this.stats.build(), this.recipes, this.advancements, this.lookingAt);
		}
	}

	static record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
		public static final Codec<PlayerPredicate.StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE
			.byNameCodec()
			.dispatch(PlayerPredicate.StatMatcher::type, PlayerPredicate.StatMatcher::createTypedCodec);

		public StatMatcher(StatType<T> statType, Holder<T> holder, MinMaxBounds.Ints ints) {
			this(statType, holder, ints, Suppliers.memoize(() -> statType.get(holder.value())));
		}

		private static <T> Codec<PlayerPredicate.StatMatcher<T>> createTypedCodec(StatType<T> statType) {
			return RecordCodecBuilder.create(
				instance -> instance.group(
							statType.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(PlayerPredicate.StatMatcher::value),
							ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "value", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate.StatMatcher::range)
						)
						.apply(instance, (holder, ints) -> new PlayerPredicate.StatMatcher<>(statType, holder, ints))
			);
		}

		public boolean matches(StatsCounter statsCounter) {
			return this.range.matches(statsCounter.getValue((Stat<?>)this.stat.get()));
		}
	}
}
