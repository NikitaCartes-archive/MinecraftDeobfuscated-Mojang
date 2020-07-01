package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
	private ClientboundRecipePacket.State state;
	private List<ResourceLocation> recipes;
	private List<ResourceLocation> toHighlight;
	private RecipeBookSettings bookSettings;

	public ClientboundRecipePacket() {
	}

	public ClientboundRecipePacket(
		ClientboundRecipePacket.State state, Collection<ResourceLocation> collection, Collection<ResourceLocation> collection2, RecipeBookSettings recipeBookSettings
	) {
		this.state = state;
		this.recipes = ImmutableList.copyOf(collection);
		this.toHighlight = ImmutableList.copyOf(collection2);
		this.bookSettings = recipeBookSettings;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddOrRemoveRecipes(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.state = friendlyByteBuf.readEnum(ClientboundRecipePacket.State.class);
		this.bookSettings = RecipeBookSettings.read(friendlyByteBuf);
		int i = friendlyByteBuf.readVarInt();
		this.recipes = Lists.<ResourceLocation>newArrayList();

		for (int j = 0; j < i; j++) {
			this.recipes.add(friendlyByteBuf.readResourceLocation());
		}

		if (this.state == ClientboundRecipePacket.State.INIT) {
			i = friendlyByteBuf.readVarInt();
			this.toHighlight = Lists.<ResourceLocation>newArrayList();

			for (int j = 0; j < i; j++) {
				this.toHighlight.add(friendlyByteBuf.readResourceLocation());
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.state);
		this.bookSettings.write(friendlyByteBuf);
		friendlyByteBuf.writeVarInt(this.recipes.size());

		for (ResourceLocation resourceLocation : this.recipes) {
			friendlyByteBuf.writeResourceLocation(resourceLocation);
		}

		if (this.state == ClientboundRecipePacket.State.INIT) {
			friendlyByteBuf.writeVarInt(this.toHighlight.size());

			for (ResourceLocation resourceLocation : this.toHighlight) {
				friendlyByteBuf.writeResourceLocation(resourceLocation);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public List<ResourceLocation> getRecipes() {
		return this.recipes;
	}

	@Environment(EnvType.CLIENT)
	public List<ResourceLocation> getHighlights() {
		return this.toHighlight;
	}

	@Environment(EnvType.CLIENT)
	public RecipeBookSettings getBookSettings() {
		return this.bookSettings;
	}

	@Environment(EnvType.CLIENT)
	public ClientboundRecipePacket.State getState() {
		return this.state;
	}

	public static enum State {
		INIT,
		ADD,
		REMOVE;
	}
}
