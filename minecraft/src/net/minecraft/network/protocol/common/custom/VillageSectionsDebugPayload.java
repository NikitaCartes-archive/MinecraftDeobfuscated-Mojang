package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record VillageSectionsDebugPayload(Set<SectionPos> villageChunks, Set<SectionPos> notVillageChunks) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation("debug/village_sections");

	public VillageSectionsDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos), friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.villageChunks, FriendlyByteBuf::writeSectionPos);
		friendlyByteBuf.writeCollection(this.notVillageChunks, FriendlyByteBuf::writeSectionPos);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
