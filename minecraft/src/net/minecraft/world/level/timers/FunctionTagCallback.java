package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionTagCallback implements TimerCallback<MinecraftServer> {
	final ResourceLocation tagId;

	public FunctionTagCallback(ResourceLocation resourceLocation) {
		this.tagId = resourceLocation;
	}

	public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
		ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();

		for (CommandFunction<CommandSourceStack> commandFunction : serverFunctionManager.getTag(this.tagId)) {
			serverFunctionManager.execute(commandFunction, serverFunctionManager.getGameLoopSender());
		}
	}

	public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
		public Serializer() {
			super(ResourceLocation.withDefaultNamespace("function_tag"), FunctionTagCallback.class);
		}

		public void serialize(CompoundTag compoundTag, FunctionTagCallback functionTagCallback) {
			compoundTag.putString("Name", functionTagCallback.tagId.toString());
		}

		public FunctionTagCallback deserialize(CompoundTag compoundTag) {
			ResourceLocation resourceLocation = ResourceLocation.parse(compoundTag.getString("Name"));
			return new FunctionTagCallback(resourceLocation);
		}
	}
}
