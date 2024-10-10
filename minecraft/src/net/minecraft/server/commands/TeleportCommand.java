package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
	private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("teleport")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("location", Vec3Argument.vec3())
						.executes(
							commandContext -> teleportToPos(
									commandContext.getSource(),
									Collections.singleton(commandContext.getSource().getEntityOrException()),
									commandContext.getSource().getLevel(),
									Vec3Argument.getCoordinates(commandContext, "location"),
									null,
									null
								)
						)
				)
				.then(
					Commands.argument("destination", EntityArgument.entity())
						.executes(
							commandContext -> teleportToEntity(
									commandContext.getSource(),
									Collections.singleton(commandContext.getSource().getEntityOrException()),
									EntityArgument.getEntity(commandContext, "destination")
								)
						)
				)
				.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
							Commands.argument("location", Vec3Argument.vec3())
								.executes(
									commandContext -> teleportToPos(
											commandContext.getSource(),
											EntityArgument.getEntities(commandContext, "targets"),
											commandContext.getSource().getLevel(),
											Vec3Argument.getCoordinates(commandContext, "location"),
											null,
											null
										)
								)
								.then(
									Commands.argument("rotation", RotationArgument.rotation())
										.executes(
											commandContext -> teleportToPos(
													commandContext.getSource(),
													EntityArgument.getEntities(commandContext, "targets"),
													commandContext.getSource().getLevel(),
													Vec3Argument.getCoordinates(commandContext, "location"),
													RotationArgument.getRotation(commandContext, "rotation"),
													null
												)
										)
								)
								.then(
									Commands.literal("facing")
										.then(
											Commands.literal("entity")
												.then(
													Commands.argument("facingEntity", EntityArgument.entity())
														.executes(
															commandContext -> teleportToPos(
																	commandContext.getSource(),
																	EntityArgument.getEntities(commandContext, "targets"),
																	commandContext.getSource().getLevel(),
																	Vec3Argument.getCoordinates(commandContext, "location"),
																	null,
																	new LookAt.LookAtEntity(EntityArgument.getEntity(commandContext, "facingEntity"), EntityAnchorArgument.Anchor.FEET)
																)
														)
														.then(
															Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
																.executes(
																	commandContext -> teleportToPos(
																			commandContext.getSource(),
																			EntityArgument.getEntities(commandContext, "targets"),
																			commandContext.getSource().getLevel(),
																			Vec3Argument.getCoordinates(commandContext, "location"),
																			null,
																			new LookAt.LookAtEntity(
																				EntityArgument.getEntity(commandContext, "facingEntity"), EntityAnchorArgument.getAnchor(commandContext, "facingAnchor")
																			)
																		)
																)
														)
												)
										)
										.then(
											Commands.argument("facingLocation", Vec3Argument.vec3())
												.executes(
													commandContext -> teleportToPos(
															commandContext.getSource(),
															EntityArgument.getEntities(commandContext, "targets"),
															commandContext.getSource().getLevel(),
															Vec3Argument.getCoordinates(commandContext, "location"),
															null,
															new LookAt.LookAtPosition(Vec3Argument.getVec3(commandContext, "facingLocation"))
														)
												)
										)
								)
						)
						.then(
							Commands.argument("destination", EntityArgument.entity())
								.executes(
									commandContext -> teleportToEntity(
											commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), EntityArgument.getEntity(commandContext, "destination")
										)
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("tp").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).redirect(literalCommandNode));
	}

	private static int teleportToEntity(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
		for (Entity entity2 : collection) {
			performTeleport(
				commandSourceStack,
				entity2,
				(ServerLevel)entity.level(),
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				EnumSet.noneOf(Relative.class),
				entity.getYRot(),
				entity.getXRot(),
				null
			);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.teleport.success.entity.single", ((Entity)collection.iterator().next()).getDisplayName(), entity.getDisplayName()),
				true
			);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName()), true);
		}

		return collection.size();
	}

	private static int teleportToPos(
		CommandSourceStack commandSourceStack,
		Collection<? extends Entity> collection,
		ServerLevel serverLevel,
		Coordinates coordinates,
		@Nullable Coordinates coordinates2,
		@Nullable LookAt lookAt
	) throws CommandSyntaxException {
		Vec3 vec3 = coordinates.getPosition(commandSourceStack);
		Vec2 vec2 = coordinates2 == null ? null : coordinates2.getRotation(commandSourceStack);

		for (Entity entity : collection) {
			Set<Relative> set = getRelatives(coordinates, coordinates2, entity.level().dimension() == serverLevel.dimension());
			if (vec2 == null) {
				performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, entity.getYRot(), entity.getXRot(), lookAt);
			} else {
				performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, lookAt);
			}
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.teleport.success.location.single",
						((Entity)collection.iterator().next()).getDisplayName(),
						formatDouble(vec3.x),
						formatDouble(vec3.y),
						formatDouble(vec3.z)
					),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.teleport.success.location.multiple", collection.size(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)
					),
				true
			);
		}

		return collection.size();
	}

	private static Set<Relative> getRelatives(Coordinates coordinates, @Nullable Coordinates coordinates2, boolean bl) {
		Set<Relative> set = EnumSet.noneOf(Relative.class);
		if (coordinates.isXRelative()) {
			set.add(Relative.DELTA_X);
			if (bl) {
				set.add(Relative.X);
			}
		}

		if (coordinates.isYRelative()) {
			set.add(Relative.DELTA_Y);
			if (bl) {
				set.add(Relative.Y);
			}
		}

		if (coordinates.isZRelative()) {
			set.add(Relative.DELTA_Z);
			if (bl) {
				set.add(Relative.Z);
			}
		}

		if (coordinates2 == null || coordinates2.isXRelative()) {
			set.add(Relative.X_ROT);
		}

		if (coordinates2 == null || coordinates2.isYRelative()) {
			set.add(Relative.Y_ROT);
		}

		return set;
	}

	private static String formatDouble(double d) {
		return String.format(Locale.ROOT, "%f", d);
	}

	private static void performTeleport(
		CommandSourceStack commandSourceStack,
		Entity entity,
		ServerLevel serverLevel,
		double d,
		double e,
		double f,
		Set<Relative> set,
		float g,
		float h,
		@Nullable LookAt lookAt
	) throws CommandSyntaxException {
		BlockPos blockPos = BlockPos.containing(d, e, f);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw INVALID_POSITION.create();
		} else {
			double i = set.contains(Relative.X) ? d - entity.getX() : d;
			double j = set.contains(Relative.Y) ? e - entity.getY() : e;
			double k = set.contains(Relative.Z) ? f - entity.getZ() : f;
			float l = set.contains(Relative.Y_ROT) ? g - entity.getYRot() : g;
			float m = set.contains(Relative.X_ROT) ? h - entity.getXRot() : h;
			float n = Mth.wrapDegrees(l);
			float o = Mth.wrapDegrees(m);
			if (entity.teleportTo(serverLevel, i, j, k, set, n, o, true)) {
				if (lookAt != null) {
					lookAt.perform(commandSourceStack, entity);
				}

				if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isFallFlying()) {
					entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
					entity.setOnGround(true);
				}

				if (entity instanceof PathfinderMob pathfinderMob) {
					pathfinderMob.getNavigation().stop();
				}
			}
		}
	}
}
