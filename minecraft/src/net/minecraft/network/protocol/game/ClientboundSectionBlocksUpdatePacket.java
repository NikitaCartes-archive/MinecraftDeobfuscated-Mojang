package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.io.IOException;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
	private SectionPos sectionPos;
	private short[] positions;
	private BlockState[] states;
	private boolean suppressLightUpdates;

	public ClientboundSectionBlocksUpdatePacket() {
	}

	public ClientboundSectionBlocksUpdatePacket(SectionPos sectionPos, ShortSet shortSet, LevelChunkSection levelChunkSection, boolean bl) {
		this.sectionPos = sectionPos;
		this.suppressLightUpdates = bl;
		this.initFields(shortSet.size());
		int i = 0;

		for (short s : shortSet) {
			this.positions[i] = s;
			this.states[i] = levelChunkSection.getBlockState(SectionPos.sectionRelativeX(s), SectionPos.sectionRelativeY(s), SectionPos.sectionRelativeZ(s));
			i++;
		}
	}

	private void initFields(int i) {
		this.positions = new short[i];
		this.states = new BlockState[i];
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.sectionPos = SectionPos.of(friendlyByteBuf.readLong());
		this.suppressLightUpdates = friendlyByteBuf.readBoolean();
		int i = friendlyByteBuf.readVarInt();
		this.initFields(i);

		for (int j = 0; j < this.positions.length; j++) {
			long l = friendlyByteBuf.readVarLong();
			this.positions[j] = (short)((int)(l & 4095L));
			this.states[j] = Block.BLOCK_STATE_REGISTRY.byId((int)(l >>> 12));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeLong(this.sectionPos.asLong());
		friendlyByteBuf.writeBoolean(this.suppressLightUpdates);
		friendlyByteBuf.writeVarInt(this.positions.length);

		for (int i = 0; i < this.positions.length; i++) {
			friendlyByteBuf.writeVarLong((long)(Block.getId(this.states[i]) << 12 | this.positions[i]));
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChunkBlocksUpdate(this);
	}

	public void runUpdates(BiConsumer<BlockPos, BlockState> biConsumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < this.positions.length; i++) {
			short s = this.positions[i];
			mutableBlockPos.set(this.sectionPos.relativeToBlockX(s), this.sectionPos.relativeToBlockY(s), this.sectionPos.relativeToBlockZ(s));
			biConsumer.accept(mutableBlockPos, this.states[i]);
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldSuppressLightUpdates() {
		return this.suppressLightUpdates;
	}
}
