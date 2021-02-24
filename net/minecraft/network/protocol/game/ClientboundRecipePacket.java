/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket
implements Packet<ClientGamePacketListener> {
    private final State state;
    private final List<ResourceLocation> recipes;
    private final List<ResourceLocation> toHighlight;
    private final RecipeBookSettings bookSettings;

    public ClientboundRecipePacket(State state, Collection<ResourceLocation> collection, Collection<ResourceLocation> collection2, RecipeBookSettings recipeBookSettings) {
        this.state = state;
        this.recipes = ImmutableList.copyOf(collection);
        this.toHighlight = ImmutableList.copyOf(collection2);
        this.bookSettings = recipeBookSettings;
    }

    public ClientboundRecipePacket(FriendlyByteBuf friendlyByteBuf) {
        this.state = friendlyByteBuf.readEnum(State.class);
        this.bookSettings = RecipeBookSettings.read(friendlyByteBuf);
        this.recipes = friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation);
        this.toHighlight = this.state == State.INIT ? friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation) : ImmutableList.of();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.state);
        this.bookSettings.write(friendlyByteBuf);
        friendlyByteBuf.writeCollection(this.recipes, FriendlyByteBuf::writeResourceLocation);
        if (this.state == State.INIT) {
            friendlyByteBuf.writeCollection(this.toHighlight, FriendlyByteBuf::writeResourceLocation);
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleAddOrRemoveRecipes(this);
    }

    @Environment(value=EnvType.CLIENT)
    public List<ResourceLocation> getRecipes() {
        return this.recipes;
    }

    @Environment(value=EnvType.CLIENT)
    public List<ResourceLocation> getHighlights() {
        return this.toHighlight;
    }

    @Environment(value=EnvType.CLIENT)
    public RecipeBookSettings getBookSettings() {
        return this.bookSettings;
    }

    @Environment(value=EnvType.CLIENT)
    public State getState() {
        return this.state;
    }

    public static enum State {
        INIT,
        ADD,
        REMOVE;

    }
}

