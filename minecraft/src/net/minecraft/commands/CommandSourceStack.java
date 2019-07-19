package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack implements SharedSuggestionProvider {
	public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.player"));
	public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.entity"));
	private final CommandSource source;
	private final Vec3 worldPosition;
	private final ServerLevel level;
	private final int permissionLevel;
	private final String textName;
	private final Component displayName;
	private final MinecraftServer server;
	private final boolean silent;
	@Nullable
	private final Entity entity;
	private final ResultConsumer<CommandSourceStack> consumer;
	private final EntityAnchorArgument.Anchor anchor;
	private final Vec2 rotation;

	public CommandSourceStack(
		CommandSource commandSource,
		Vec3 vec3,
		Vec2 vec2,
		ServerLevel serverLevel,
		int i,
		String string,
		Component component,
		MinecraftServer minecraftServer,
		@Nullable Entity entity
	) {
		this(commandSource, vec3, vec2, serverLevel, i, string, component, minecraftServer, entity, false, (commandContext, bl, ix) -> {
		}, EntityAnchorArgument.Anchor.FEET);
	}

	protected CommandSourceStack(
		CommandSource commandSource,
		Vec3 vec3,
		Vec2 vec2,
		ServerLevel serverLevel,
		int i,
		String string,
		Component component,
		MinecraftServer minecraftServer,
		@Nullable Entity entity,
		boolean bl,
		ResultConsumer<CommandSourceStack> resultConsumer,
		EntityAnchorArgument.Anchor anchor
	) {
		this.source = commandSource;
		this.worldPosition = vec3;
		this.level = serverLevel;
		this.silent = bl;
		this.entity = entity;
		this.permissionLevel = i;
		this.textName = string;
		this.displayName = component;
		this.server = minecraftServer;
		this.consumer = resultConsumer;
		this.anchor = anchor;
		this.rotation = vec2;
	}

	public CommandSourceStack withEntity(Entity entity) {
		return this.entity == entity
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				this.permissionLevel,
				entity.getName().getString(),
				entity.getDisplayName(),
				this.server,
				entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withPosition(Vec3 vec3) {
		return this.worldPosition.equals(vec3)
			? this
			: new CommandSourceStack(
				this.source,
				vec3,
				this.rotation,
				this.level,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withRotation(Vec2 vec2) {
		return this.rotation.equals(vec2)
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				vec2,
				this.level,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> resultConsumer) {
		return this.consumer.equals(resultConsumer)
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				resultConsumer,
				this.anchor
			);
	}

	public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> resultConsumer, BinaryOperator<ResultConsumer<CommandSourceStack>> binaryOperator) {
		ResultConsumer<CommandSourceStack> resultConsumer2 = (ResultConsumer<CommandSourceStack>)binaryOperator.apply(this.consumer, resultConsumer);
		return this.withCallback(resultConsumer2);
	}

	public CommandSourceStack withSuppressedOutput() {
		return this.silent
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				true,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withPermission(int i) {
		return i == this.permissionLevel
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				i,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withMaximumPermission(int i) {
		return i <= this.permissionLevel
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				i,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack withAnchor(EntityAnchorArgument.Anchor anchor) {
		return anchor == this.anchor
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				this.level,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				anchor
			);
	}

	public CommandSourceStack withLevel(ServerLevel serverLevel) {
		return serverLevel == this.level
			? this
			: new CommandSourceStack(
				this.source,
				this.worldPosition,
				this.rotation,
				serverLevel,
				this.permissionLevel,
				this.textName,
				this.displayName,
				this.server,
				this.entity,
				this.silent,
				this.consumer,
				this.anchor
			);
	}

	public CommandSourceStack facing(Entity entity, EntityAnchorArgument.Anchor anchor) throws CommandSyntaxException {
		return this.facing(anchor.apply(entity));
	}

	public CommandSourceStack facing(Vec3 vec3) throws CommandSyntaxException {
		Vec3 vec32 = this.anchor.apply(this);
		double d = vec3.x - vec32.x;
		double e = vec3.y - vec32.y;
		double f = vec3.z - vec32.z;
		double g = (double)Mth.sqrt(d * d + f * f);
		float h = Mth.wrapDegrees((float)(-(Mth.atan2(e, g) * 180.0F / (float)Math.PI)));
		float i = Mth.wrapDegrees((float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F);
		return this.withRotation(new Vec2(h, i));
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public String getTextName() {
		return this.textName;
	}

	@Override
	public boolean hasPermission(int i) {
		return this.permissionLevel >= i;
	}

	public Vec3 getPosition() {
		return this.worldPosition;
	}

	public ServerLevel getLevel() {
		return this.level;
	}

	@Nullable
	public Entity getEntity() {
		return this.entity;
	}

	public Entity getEntityOrException() throws CommandSyntaxException {
		if (this.entity == null) {
			throw ERROR_NOT_ENTITY.create();
		} else {
			return this.entity;
		}
	}

	public ServerPlayer getPlayerOrException() throws CommandSyntaxException {
		if (!(this.entity instanceof ServerPlayer)) {
			throw ERROR_NOT_PLAYER.create();
		} else {
			return (ServerPlayer)this.entity;
		}
	}

	public Vec2 getRotation() {
		return this.rotation;
	}

	public MinecraftServer getServer() {
		return this.server;
	}

	public EntityAnchorArgument.Anchor getAnchor() {
		return this.anchor;
	}

	public void sendSuccess(Component component, boolean bl) {
		if (this.source.acceptsSuccess() && !this.silent) {
			this.source.sendMessage(component);
		}

		if (bl && this.source.shouldInformAdmins() && !this.silent) {
			this.broadcastToAdmins(component);
		}
	}

	private void broadcastToAdmins(Component component) {
		Component component2 = new TranslatableComponent("chat.type.admin", this.getDisplayName(), component)
			.withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC});
		if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
			for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
				if (serverPlayer != this.source && this.server.getPlayerList().isOp(serverPlayer.getGameProfile())) {
					serverPlayer.sendMessage(component2);
				}
			}
		}

		if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
			this.server.sendMessage(component2);
		}
	}

	public void sendFailure(Component component) {
		if (this.source.acceptsFailure() && !this.silent) {
			this.source.sendMessage(new TextComponent("").append(component).withStyle(ChatFormatting.RED));
		}
	}

	public void onCommandComplete(CommandContext<CommandSourceStack> commandContext, boolean bl, int i) {
		if (this.consumer != null) {
			this.consumer.onCommandComplete(commandContext, bl, i);
		}
	}

	@Override
	public Collection<String> getOnlinePlayerNames() {
		return Lists.<String>newArrayList(this.server.getPlayerNames());
	}

	@Override
	public Collection<String> getAllTeams() {
		return this.server.getScoreboard().getTeamNames();
	}

	@Override
	public Collection<ResourceLocation> getAvailableSoundEvents() {
		return Registry.SOUND_EVENT.keySet();
	}

	@Override
	public Stream<ResourceLocation> getRecipeNames() {
		return this.server.getRecipeManager().getRecipeIds();
	}

	@Override
	public CompletableFuture<Suggestions> customSuggestion(CommandContext<SharedSuggestionProvider> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return null;
	}
}
