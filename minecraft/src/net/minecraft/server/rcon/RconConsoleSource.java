package net.minecraft.server.rcon;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class RconConsoleSource implements CommandSource {
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
		ServerLevel serverLevel = this.server.getLevel(DimensionType.OVERWORLD);
		return new CommandSourceStack(
			this, new Vec3(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "Recon", new TextComponent("Rcon"), this.server, null
		);
	}

	@Override
	public void sendMessage(Component component) {
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
