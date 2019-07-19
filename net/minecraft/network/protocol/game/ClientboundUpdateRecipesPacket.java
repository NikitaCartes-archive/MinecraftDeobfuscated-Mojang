/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundUpdateRecipesPacket
implements Packet<ClientGamePacketListener> {
    private List<Recipe<?>> recipes;

    public ClientboundUpdateRecipesPacket() {
    }

    public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> collection) {
        this.recipes = Lists.newArrayList(collection);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateRecipes(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.recipes = Lists.newArrayList();
        int i = friendlyByteBuf.readVarInt();
        for (int j = 0; j < i; ++j) {
            this.recipes.add(ClientboundUpdateRecipesPacket.fromNetwork(friendlyByteBuf));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.recipes.size());
        for (Recipe<?> recipe : this.recipes) {
            ClientboundUpdateRecipesPacket.toNetwork(recipe, friendlyByteBuf);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public static Recipe<?> fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return Registry.RECIPE_SERIALIZER.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourceLocation)).fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    public static <T extends Recipe<?>> void toNetwork(T recipe, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(recipe.getSerializer()));
        friendlyByteBuf.writeResourceLocation(recipe.getId());
        recipe.getSerializer().toNetwork(friendlyByteBuf, recipe);
    }
}

