package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookUpdatePacket implements Packet<ServerGamePacketListener> {
	private ServerboundRecipeBookUpdatePacket.Purpose purpose;
	private ResourceLocation recipe;
	private boolean guiOpen;
	private boolean filteringCraftable;
	private boolean furnaceGuiOpen;
	private boolean furnaceFilteringCraftable;
	private boolean blastFurnaceGuiOpen;
	private boolean blastFurnaceFilteringCraftable;
	private boolean smokerGuiOpen;
	private boolean smokerFilteringCraftable;

	public ServerboundRecipeBookUpdatePacket() {
	}

	public ServerboundRecipeBookUpdatePacket(Recipe<?> recipe) {
		this.purpose = ServerboundRecipeBookUpdatePacket.Purpose.SHOWN;
		this.recipe = recipe.getId();
	}

	@Environment(EnvType.CLIENT)
	public ServerboundRecipeBookUpdatePacket(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
		this.purpose = ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS;
		this.guiOpen = bl;
		this.filteringCraftable = bl2;
		this.furnaceGuiOpen = bl3;
		this.furnaceFilteringCraftable = bl4;
		this.blastFurnaceGuiOpen = bl5;
		this.blastFurnaceFilteringCraftable = bl6;
		this.smokerGuiOpen = bl5;
		this.smokerFilteringCraftable = bl6;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.purpose = friendlyByteBuf.readEnum(ServerboundRecipeBookUpdatePacket.Purpose.class);
		if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SHOWN) {
			this.recipe = friendlyByteBuf.readResourceLocation();
		} else if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS) {
			this.guiOpen = friendlyByteBuf.readBoolean();
			this.filteringCraftable = friendlyByteBuf.readBoolean();
			this.furnaceGuiOpen = friendlyByteBuf.readBoolean();
			this.furnaceFilteringCraftable = friendlyByteBuf.readBoolean();
			this.blastFurnaceGuiOpen = friendlyByteBuf.readBoolean();
			this.blastFurnaceFilteringCraftable = friendlyByteBuf.readBoolean();
			this.smokerGuiOpen = friendlyByteBuf.readBoolean();
			this.smokerFilteringCraftable = friendlyByteBuf.readBoolean();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.purpose);
		if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SHOWN) {
			friendlyByteBuf.writeResourceLocation(this.recipe);
		} else if (this.purpose == ServerboundRecipeBookUpdatePacket.Purpose.SETTINGS) {
			friendlyByteBuf.writeBoolean(this.guiOpen);
			friendlyByteBuf.writeBoolean(this.filteringCraftable);
			friendlyByteBuf.writeBoolean(this.furnaceGuiOpen);
			friendlyByteBuf.writeBoolean(this.furnaceFilteringCraftable);
			friendlyByteBuf.writeBoolean(this.blastFurnaceGuiOpen);
			friendlyByteBuf.writeBoolean(this.blastFurnaceFilteringCraftable);
			friendlyByteBuf.writeBoolean(this.smokerGuiOpen);
			friendlyByteBuf.writeBoolean(this.smokerFilteringCraftable);
		}
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleRecipeBookUpdatePacket(this);
	}

	public ServerboundRecipeBookUpdatePacket.Purpose getPurpose() {
		return this.purpose;
	}

	public ResourceLocation getRecipe() {
		return this.recipe;
	}

	public boolean isGuiOpen() {
		return this.guiOpen;
	}

	public boolean isFilteringCraftable() {
		return this.filteringCraftable;
	}

	public boolean isFurnaceGuiOpen() {
		return this.furnaceGuiOpen;
	}

	public boolean isFurnaceFilteringCraftable() {
		return this.furnaceFilteringCraftable;
	}

	public boolean isBlastFurnaceGuiOpen() {
		return this.blastFurnaceGuiOpen;
	}

	public boolean isBlastFurnaceFilteringCraftable() {
		return this.blastFurnaceFilteringCraftable;
	}

	public boolean isSmokerGuiOpen() {
		return this.smokerGuiOpen;
	}

	public boolean isSmokerFilteringCraftable() {
		return this.smokerFilteringCraftable;
	}

	public static enum Purpose {
		SHOWN,
		SETTINGS;
	}
}
