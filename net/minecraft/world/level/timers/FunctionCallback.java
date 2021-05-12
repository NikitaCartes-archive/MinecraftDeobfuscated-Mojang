/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class FunctionCallback
implements TimerCallback<MinecraftServer> {
    final ResourceLocation functionId;

    public FunctionCallback(ResourceLocation resourceLocation) {
        this.functionId = resourceLocation;
    }

    @Override
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
        ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
        serverFunctionManager.get(this.functionId).ifPresent(commandFunction -> serverFunctionManager.execute((CommandFunction)commandFunction, serverFunctionManager.getGameLoopSender()));
    }

    public static class Serializer
    extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
        public Serializer() {
            super(new ResourceLocation("function"), FunctionCallback.class);
        }

        @Override
        public void serialize(CompoundTag compoundTag, FunctionCallback functionCallback) {
            compoundTag.putString("Name", functionCallback.functionId.toString());
        }

        @Override
        public FunctionCallback deserialize(CompoundTag compoundTag) {
            ResourceLocation resourceLocation = new ResourceLocation(compoundTag.getString("Name"));
            return new FunctionCallback(resourceLocation);
        }

        @Override
        public /* synthetic */ TimerCallback deserialize(CompoundTag compoundTag) {
            return this.deserialize(compoundTag);
        }
    }
}

