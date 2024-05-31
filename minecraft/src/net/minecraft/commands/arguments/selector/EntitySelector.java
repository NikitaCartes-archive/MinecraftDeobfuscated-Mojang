package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelector {
	public static final int INFINITE = Integer.MAX_VALUE;
	public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = (vec3, list) -> {
	};
	private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>() {
		public Entity tryCast(Entity entity) {
			return entity;
		}

		@Override
		public Class<? extends Entity> getBaseClass() {
			return Entity.class;
		}
	};
	private final int maxResults;
	private final boolean includesEntities;
	private final boolean worldLimited;
	private final List<Predicate<Entity>> contextFreePredicates;
	private final MinMaxBounds.Doubles range;
	private final Function<Vec3, Vec3> position;
	@Nullable
	private final AABB aabb;
	private final BiConsumer<Vec3, List<? extends Entity>> order;
	private final boolean currentEntity;
	@Nullable
	private final String playerName;
	@Nullable
	private final UUID entityUUID;
	private final EntityTypeTest<Entity, ?> type;
	private final boolean usesSelector;

	public EntitySelector(
		int i,
		boolean bl,
		boolean bl2,
		List<Predicate<Entity>> list,
		MinMaxBounds.Doubles doubles,
		Function<Vec3, Vec3> function,
		@Nullable AABB aABB,
		BiConsumer<Vec3, List<? extends Entity>> biConsumer,
		boolean bl3,
		@Nullable String string,
		@Nullable UUID uUID,
		@Nullable EntityType<?> entityType,
		boolean bl4
	) {
		this.maxResults = i;
		this.includesEntities = bl;
		this.worldLimited = bl2;
		this.contextFreePredicates = list;
		this.range = doubles;
		this.position = function;
		this.aabb = aABB;
		this.order = biConsumer;
		this.currentEntity = bl3;
		this.playerName = string;
		this.entityUUID = uUID;
		this.type = (EntityTypeTest<Entity, ?>)(entityType == null ? ANY_TYPE : entityType);
		this.usesSelector = bl4;
	}

	public int getMaxResults() {
		return this.maxResults;
	}

	public boolean includesEntities() {
		return this.includesEntities;
	}

	public boolean isSelfSelector() {
		return this.currentEntity;
	}

	public boolean isWorldLimited() {
		return this.worldLimited;
	}

	public boolean usesSelector() {
		return this.usesSelector;
	}

	private void checkPermissions(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		if (this.usesSelector && !commandSourceStack.hasPermission(2)) {
			throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
		}
	}

	public Entity findSingleEntity(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		this.checkPermissions(commandSourceStack);
		List<? extends Entity> list = this.findEntities(commandSourceStack);
		if (list.isEmpty()) {
			throw EntityArgument.NO_ENTITIES_FOUND.create();
		} else if (list.size() > 1) {
			throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
		} else {
			return (Entity)list.get(0);
		}
	}

	public List<? extends Entity> findEntities(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		this.checkPermissions(commandSourceStack);
		if (!this.includesEntities) {
			return this.findPlayers(commandSourceStack);
		} else if (this.playerName != null) {
			ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayerByName(this.playerName);
			return serverPlayer == null ? List.of() : List.of(serverPlayer);
		} else if (this.entityUUID != null) {
			for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
				Entity entity = serverLevel.getEntity(this.entityUUID);
				if (entity != null) {
					if (entity.getType().isEnabled(commandSourceStack.enabledFeatures())) {
						return List.of(entity);
					}
					break;
				}
			}

			return List.of();
		} else {
			Vec3 vec3 = (Vec3)this.position.apply(commandSourceStack.getPosition());
			AABB aABB = this.getAbsoluteAabb(vec3);
			if (this.currentEntity) {
				Predicate<Entity> predicate = this.getPredicate(vec3, aABB, null);
				return commandSourceStack.getEntity() != null && predicate.test(commandSourceStack.getEntity()) ? List.of(commandSourceStack.getEntity()) : List.of();
			} else {
				Predicate<Entity> predicate = this.getPredicate(vec3, aABB, commandSourceStack.enabledFeatures());
				List<Entity> list = new ObjectArrayList<>();
				if (this.isWorldLimited()) {
					this.addEntities(list, commandSourceStack.getLevel(), aABB, predicate);
				} else {
					for (ServerLevel serverLevel2 : commandSourceStack.getServer().getAllLevels()) {
						this.addEntities(list, serverLevel2, aABB, predicate);
					}
				}

				return this.sortAndLimit(vec3, list);
			}
		}
	}

	private void addEntities(List<Entity> list, ServerLevel serverLevel, @Nullable AABB aABB, Predicate<Entity> predicate) {
		int i = this.getResultLimit();
		if (list.size() < i) {
			if (aABB != null) {
				serverLevel.getEntities(this.type, aABB, predicate, list, i);
			} else {
				serverLevel.getEntities(this.type, predicate, list, i);
			}
		}
	}

	private int getResultLimit() {
		return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
	}

	public ServerPlayer findSinglePlayer(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		this.checkPermissions(commandSourceStack);
		List<ServerPlayer> list = this.findPlayers(commandSourceStack);
		if (list.size() != 1) {
			throw EntityArgument.NO_PLAYERS_FOUND.create();
		} else {
			return (ServerPlayer)list.get(0);
		}
	}

	public List<ServerPlayer> findPlayers(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		this.checkPermissions(commandSourceStack);
		if (this.playerName != null) {
			ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayerByName(this.playerName);
			return serverPlayer == null ? List.of() : List.of(serverPlayer);
		} else if (this.entityUUID != null) {
			ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayer(this.entityUUID);
			return serverPlayer == null ? List.of() : List.of(serverPlayer);
		} else {
			Vec3 vec3 = (Vec3)this.position.apply(commandSourceStack.getPosition());
			AABB aABB = this.getAbsoluteAabb(vec3);
			Predicate<Entity> predicate = this.getPredicate(vec3, aABB, null);
			if (this.currentEntity) {
				if (commandSourceStack.getEntity() instanceof ServerPlayer serverPlayer2 && predicate.test(serverPlayer2)) {
					return List.of(serverPlayer2);
				}

				return List.of();
			} else {
				int i = this.getResultLimit();
				List<ServerPlayer> list;
				if (this.isWorldLimited()) {
					list = commandSourceStack.getLevel().getPlayers(predicate, i);
				} else {
					list = new ObjectArrayList<>();

					for (ServerPlayer serverPlayer3 : commandSourceStack.getServer().getPlayerList().getPlayers()) {
						if (predicate.test(serverPlayer3)) {
							list.add(serverPlayer3);
							if (list.size() >= i) {
								return list;
							}
						}
					}
				}

				return this.sortAndLimit(vec3, list);
			}
		}
	}

	@Nullable
	private AABB getAbsoluteAabb(Vec3 vec3) {
		return this.aabb != null ? this.aabb.move(vec3) : null;
	}

	private Predicate<Entity> getPredicate(Vec3 vec3, @Nullable AABB aABB, @Nullable FeatureFlagSet featureFlagSet) {
		boolean bl = featureFlagSet != null;
		boolean bl2 = aABB != null;
		boolean bl3 = !this.range.isAny();
		int i = (bl ? 1 : 0) + (bl2 ? 1 : 0) + (bl3 ? 1 : 0);
		List<Predicate<Entity>> list;
		if (i == 0) {
			list = this.contextFreePredicates;
		} else {
			List<Predicate<Entity>> list2 = new ObjectArrayList<>(this.contextFreePredicates.size() + i);
			list2.addAll(this.contextFreePredicates);
			if (bl) {
				list2.add((Predicate)entity -> entity.getType().isEnabled(featureFlagSet));
			}

			if (bl2) {
				list2.add((Predicate)entity -> aABB.intersects(entity.getBoundingBox()));
			}

			if (bl3) {
				list2.add((Predicate)entity -> this.range.matchesSqr(entity.distanceToSqr(vec3)));
			}

			list = list2;
		}

		return Util.allOf(list);
	}

	private <T extends Entity> List<T> sortAndLimit(Vec3 vec3, List<T> list) {
		if (list.size() > 1) {
			this.order.accept(vec3, list);
		}

		return list.subList(0, Math.min(this.maxResults, list.size()));
	}

	public static Component joinNames(List<? extends Entity> list) {
		return ComponentUtils.formatList(list, Entity::getDisplayName);
	}
}
