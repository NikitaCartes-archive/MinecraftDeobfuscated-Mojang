package net.minecraft.advancements;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record AdvancementHolder(ResourceLocation id, Advancement value) {
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.id);
		this.value.write(friendlyByteBuf);
	}

	public static AdvancementHolder read(FriendlyByteBuf friendlyByteBuf) {
		return new AdvancementHolder(friendlyByteBuf.readResourceLocation(), Advancement.read(friendlyByteBuf));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof AdvancementHolder advancementHolder && this.id.equals(advancementHolder.id)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public String toString() {
		return this.id.toString();
	}
}
