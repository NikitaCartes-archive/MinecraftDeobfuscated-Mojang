/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundPlaceGhostRecipePacket
implements Packet<ClientGamePacketListener> {
    private int containerId;
    private ResourceLocation recipe;

    public ClientboundPlaceGhostRecipePacket() {
    }

    public ClientboundPlaceGhostRecipePacket(int i, Recipe<?> recipe) {
        this.containerId = i;
        this.recipe = recipe.getId();
    }

    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    @Environment(value=EnvType.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.containerId = friendlyByteBuf.readByte();
        this.recipe = friendlyByteBuf.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeResourceLocation(this.recipe);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlaceRecipe(this);
    }
}

