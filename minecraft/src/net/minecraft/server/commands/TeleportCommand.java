package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
	private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.teleport.invalidPosition")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("teleport")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
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
																	new TeleportCommand.LookAt(EntityArgument.getEntity(commandContext, "facingEntity"), EntityAnchorArgument.Anchor.FEET)
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
																			new TeleportCommand.LookAt(
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
															new TeleportCommand.LookAt(Vec3Argument.getVec3(commandContext, "facingLocation"))
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
				.then(
					Commands.argument("location", Vec3Argument.vec3())
						.executes(
							commandContext -> teleportToPos(
									commandContext.getSource(),
									Collections.singleton(commandContext.getSource().getEntityOrException()),
									commandContext.getSource().getLevel(),
									Vec3Argument.getCoordinates(commandContext, "location"),
									WorldCoordinates.current(),
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
		);
		commandDispatcher.register(Commands.literal("tp").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).redirect(literalCommandNode));
	}

	private static int teleportToEntity(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
		for (Entity entity2 : collection) {
			performTeleport(
				commandSourceStack,
				entity2,
				(ServerLevel)entity.level,
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class),
				entity.yRot,
				entity.xRot,
				null
			);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.teleport.success.entity.single", ((Entity)collection.iterator().next()).getDisplayName(), entity.getDisplayName()),
				true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName()), true);
		}

		return collection.size();
	}

	private static int teleportToPos(
		CommandSourceStack commandSourceStack,
		Collection<? extends Entity> collection,
		ServerLevel serverLevel,
		Coordinates coordinates,
		@Nullable Coordinates coordinates2,
		@Nullable TeleportCommand.LookAt lookAt
	) throws CommandSyntaxException {
		Vec3 vec3 = coordinates.getPosition(commandSourceStack);
		Vec2 vec2 = coordinates2 == null ? null : coordinates2.getRotation(commandSourceStack);
		Set<ClientboundPlayerPositionPacket.RelativeArgument> set = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);
		if (coordinates.isXRelative()) {
			set.add(ClientboundPlayerPositionPacket.RelativeArgument.X);
		}

		if (coordinates.isYRelative()) {
			set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y);
		}

		if (coordinates.isZRelative()) {
			set.add(ClientboundPlayerPositionPacket.RelativeArgument.Z);
		}

		if (coordinates2 == null) {
			set.add(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT);
			set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT);
		} else {
			if (coordinates2.isXRelative()) {
				set.add(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT);
			}

			if (coordinates2.isYRelative()) {
				set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT);
			}
		}

		for (Entity entity : collection) {
			if (coordinates2 == null) {
				performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, entity.yRot, entity.xRot, lookAt);
			} else {
				performTeleport(commandSourceStack, entity, serverLevel, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, lookAt);
			}
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.teleport.success.location.single", ((Entity)collection.iterator().next()).getDisplayName(), vec3.x, vec3.y, vec3.z),
				true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.teleport.success.location.multiple", collection.size(), vec3.x, vec3.y, vec3.z), true);
		}

		return collection.size();
	}

	public static void performTeleport(
		CommandSourceStack commandSourceStack,
		Entity entity,
		ServerLevel serverLevel,
		double d,
		double e,
		double f,
		Set<ClientboundPlayerPositionPacket.RelativeArgument> set,
		float g,
		float h,
		@Nullable TeleportCommand.LookAt lookAt
	) throws CommandSyntaxException {
		BlockPos blockPos = new BlockPos(d, e, f);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw INVALID_POSITION.create();
		} else {
			if (entity instanceof ServerPlayer) {
				ChunkPos chunkPos = new ChunkPos(new BlockPos(d, e, f));
				serverLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, entity.getId());
				entity.stopRiding();
				if (((ServerPlayer)entity).isSleeping()) {
					((ServerPlayer)entity).stopSleepInBed(true, true);
				}

				if (serverLevel == entity.level) {
					((ServerPlayer)entity).connection.teleport(d, e, f, g, h, set);
				} else {
					((ServerPlayer)entity).teleportTo(serverLevel, d, e, f, g, h);
				}

				entity.setYHeadRot(g);
			} else {
				float i = Mth.wrapDegrees(g);
				float j = Mth.wrapDegrees(h);
				j = Mth.clamp(j, -90.0F, 90.0F);
				if (serverLevel == entity.level) {
					entity.moveTo(d, e, f, i, j);
					entity.setYHeadRot(i);
				} else {
					entity.unRide();
					entity.dimension = serverLevel.dimension.getType();
					Entity entity2 = entity;
					entity = entity.getType().create(serverLevel);
					if (entity == null) {
						return;
					}

					entity.restoreFrom(entity2);
					entity.moveTo(d, e, f, i, j);
					entity.setYHeadRot(i);
					serverLevel.addFromAnotherDimension(entity);
					entity2.removed = true;
				}
			}

			if (lookAt != null) {
				lookAt.perform(commandSourceStack, entity);
			}

			if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).isFallFlying()) {
				entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
				entity.setOnGround(true);
			}
		}
	}

	static class LookAt {
		private final Vec3 position;
		private final Entity entity;
		private final EntityAnchorArgument.Anchor anchor;

		public LookAt(Entity entity, EntityAnchorArgument.Anchor anchor) {
			this.entity = entity;
			this.anchor = anchor;
			this.position = anchor.apply(entity);
		}

		public LookAt(Vec3 vec3) {
			this.entity = null;
			this.position = vec3;
			this.anchor = null;
		}

		public void perform(CommandSourceStack commandSourceStack, Entity entity) {
			if (this.entity != null) {
				if (entity instanceof ServerPlayer) {
					((ServerPlayer)entity).lookAt(commandSourceStack.getAnchor(), this.entity, this.anchor);
				} else {
					entity.lookAt(commandSourceStack.getAnchor(), this.position);
				}
			} else {
				entity.lookAt(commandSourceStack.getAnchor(), this.position);
			}
		}
	}
}
