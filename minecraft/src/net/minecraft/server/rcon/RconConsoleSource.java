package net.minecraft.server.rcon;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class RconConsoleSource implements CommandSource {
	private static final String RCON = "Rcon";
	private static final Component RCON_COMPONENT = Component.literal("Rcon");
	private final StringBuffer buffer = new StringBuffer();
	private final MinecraftServer server;

	public RconConsoleSource(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
	}

	public void prepareForCommand() {
		this.buffer.setLength(0);
	}

	public String getCommandResponse() {
		return this.buffer.toString();
	}

	public CommandSourceStack createCommandSourceStack() {
		ServerLevel serverLevel = this.server.overworld();
		return new CommandSourceStack(
			this, Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "Rcon", RCON_COMPONENT, this.server, null
		);
	}

	@Override
	public void sendSystemMessage(Component component) {
		this.buffer.append(component.getString());
	}

	@Override
	public boolean acceptsSuccess() {
		return true;
	}

	@Override
	public boolean acceptsFailure() {
		return true;
	}

	@Override
	public boolean shouldInformAdmins() {
		return this.server.shouldRconBroadcast();
	}
}
