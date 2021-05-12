package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionCallback implements TimerCallback<MinecraftServer> {
	final ResourceLocation functionId;

	public FunctionCallback(ResourceLocation resourceLocation) {
		this.functionId = resourceLocation;
	}

	public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
		ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
		serverFunctionManager.get(this.functionId)
			.ifPresent(commandFunction -> serverFunctionManager.execute(commandFunction, serverFunctionManager.getGameLoopSender()));
	}

	public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
		public Serializer() {
			super(new ResourceLocation("function"), FunctionCallback.class);
		}

		public void serialize(CompoundTag compoundTag, FunctionCallback functionCallback) {
			compoundTag.putString("Name", functionCallback.functionId.toString());
		}

		public FunctionCallback deserialize(CompoundTag compoundTag) {
			ResourceLocation resourceLocation = new ResourceLocation(compoundTag.getString("Name"));
			return new FunctionCallback(resourceLocation);
		}
	}
}
