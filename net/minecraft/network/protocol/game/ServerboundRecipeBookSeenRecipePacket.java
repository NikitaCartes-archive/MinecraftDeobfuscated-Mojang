/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket
implements Packet<ServerGamePacketListener> {
    private final ResourceLocation recipe;

    public ServerboundRecipeBookSeenRecipePacket(Recipe<?> recipe) {
        this.recipe = recipe.getId();
    }

    public ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf friendlyByteBuf) {
        this.recipe = friendlyByteBuf.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(this.recipe);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleRecipeBookSeenRecipePacket(this);
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }
}

