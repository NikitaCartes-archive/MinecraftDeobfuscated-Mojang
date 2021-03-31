package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
	private final ClientboundRecipePacket.State state;
	private final List<ResourceLocation> recipes;
	private final List<ResourceLocation> toHighlight;
	private final RecipeBookSettings bookSettings;

	public ClientboundRecipePacket(
		ClientboundRecipePacket.State state, Collection<ResourceLocation> collection, Collection<ResourceLocation> collection2, RecipeBookSettings recipeBookSettings
	) {
		this.state = state;
		this.recipes = ImmutableList.copyOf(collection);
		this.toHighlight = ImmutableList.copyOf(collection2);
		this.bookSettings = recipeBookSettings;
	}

	public ClientboundRecipePacket(FriendlyByteBuf friendlyByteBuf) {
		this.state = friendlyByteBuf.readEnum(ClientboundRecipePacket.State.class);
		this.bookSettings = RecipeBookSettings.read(friendlyByteBuf);
		this.recipes = friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation);
		if (this.state == ClientboundRecipePacket.State.INIT) {
			this.toHighlight = friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation);
		} else {
			this.toHighlight = ImmutableList.of();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.state);
		this.bookSettings.write(friendlyByteBuf);
		friendlyByteBuf.writeCollection(this.recipes, FriendlyByteBuf::writeResourceLocation);
		if (this.state == ClientboundRecipePacket.State.INIT) {
			friendlyByteBuf.writeCollection(this.toHighlight, FriendlyByteBuf::writeResourceLocation);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddOrRemoveRecipes(this);
	}

	public List<ResourceLocation> getRecipes() {
		return this.recipes;
	}

	public List<ResourceLocation> getHighlights() {
		return this.toHighlight;
	}

	public RecipeBookSettings getBookSettings() {
		return this.bookSettings;
	}

	public ClientboundRecipePacket.State getState() {
		return this.state;
	}

	public static enum State {
		INIT,
		ADD,
		REMOVE;
	}
}
