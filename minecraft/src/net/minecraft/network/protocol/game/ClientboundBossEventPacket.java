package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBossEventPacket> STREAM_CODEC = Packet.codec(
		ClientboundBossEventPacket::write, ClientboundBossEventPacket::new
	);
	private static final int FLAG_DARKEN = 1;
	private static final int FLAG_MUSIC = 2;
	private static final int FLAG_FOG = 4;
	private final UUID id;
	private final ClientboundBossEventPacket.Operation operation;
	static final ClientboundBossEventPacket.Operation REMOVE_OPERATION = new ClientboundBossEventPacket.Operation() {
		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.REMOVE;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.remove(uUID);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		}
	};

	private ClientboundBossEventPacket(UUID uUID, ClientboundBossEventPacket.Operation operation) {
		this.id = uUID;
		this.operation = operation;
	}

	private ClientboundBossEventPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.id = registryFriendlyByteBuf.readUUID();
		ClientboundBossEventPacket.OperationType operationType = registryFriendlyByteBuf.readEnum(ClientboundBossEventPacket.OperationType.class);
		this.operation = operationType.reader.decode(registryFriendlyByteBuf);
	}

	public static ClientboundBossEventPacket createAddPacket(BossEvent bossEvent) {
		return new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.AddOperation(bossEvent));
	}

	public static ClientboundBossEventPacket createRemovePacket(UUID uUID) {
		return new ClientboundBossEventPacket(uUID, REMOVE_OPERATION);
	}

	public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent bossEvent) {
		return new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.UpdateProgressOperation(bossEvent.getProgress()));
	}

	public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent bossEvent) {
		return new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.UpdateNameOperation(bossEvent.getName()));
	}

	public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent bossEvent) {
		return new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.UpdateStyleOperation(bossEvent.getColor(), bossEvent.getOverlay()));
	}

	public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent bossEvent) {
		return new ClientboundBossEventPacket(
			bossEvent.getId(),
			new ClientboundBossEventPacket.UpdatePropertiesOperation(bossEvent.shouldDarkenScreen(), bossEvent.shouldPlayBossMusic(), bossEvent.shouldCreateWorldFog())
		);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeUUID(this.id);
		registryFriendlyByteBuf.writeEnum(this.operation.getType());
		this.operation.write(registryFriendlyByteBuf);
	}

	static int encodeProperties(boolean bl, boolean bl2, boolean bl3) {
		int i = 0;
		if (bl) {
			i |= 1;
		}

		if (bl2) {
			i |= 2;
		}

		if (bl3) {
			i |= 4;
		}

		return i;
	}

	@Override
	public PacketType<ClientboundBossEventPacket> type() {
		return GamePacketTypes.CLIENTBOUND_BOSS_EVENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBossUpdate(this);
	}

	public void dispatch(ClientboundBossEventPacket.Handler handler) {
		this.operation.dispatch(this.id, handler);
	}

	static class AddOperation implements ClientboundBossEventPacket.Operation {
		private final Component name;
		private final float progress;
		private final BossEvent.BossBarColor color;
		private final BossEvent.BossBarOverlay overlay;
		private final boolean darkenScreen;
		private final boolean playMusic;
		private final boolean createWorldFog;

		AddOperation(BossEvent bossEvent) {
			this.name = bossEvent.getName();
			this.progress = bossEvent.getProgress();
			this.color = bossEvent.getColor();
			this.overlay = bossEvent.getOverlay();
			this.darkenScreen = bossEvent.shouldDarkenScreen();
			this.playMusic = bossEvent.shouldPlayBossMusic();
			this.createWorldFog = bossEvent.shouldCreateWorldFog();
		}

		private AddOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			this.name = ComponentSerialization.STREAM_CODEC.decode(registryFriendlyByteBuf);
			this.progress = registryFriendlyByteBuf.readFloat();
			this.color = registryFriendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
			this.overlay = registryFriendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
			int i = registryFriendlyByteBuf.readUnsignedByte();
			this.darkenScreen = (i & 1) > 0;
			this.playMusic = (i & 2) > 0;
			this.createWorldFog = (i & 4) > 0;
		}

		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.ADD;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.add(uUID, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			ComponentSerialization.STREAM_CODEC.encode(registryFriendlyByteBuf, this.name);
			registryFriendlyByteBuf.writeFloat(this.progress);
			registryFriendlyByteBuf.writeEnum(this.color);
			registryFriendlyByteBuf.writeEnum(this.overlay);
			registryFriendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
		}
	}

	public interface Handler {
		default void add(
			UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3
		) {
		}

		default void remove(UUID uUID) {
		}

		default void updateProgress(UUID uUID, float f) {
		}

		default void updateName(UUID uUID, Component component) {
		}

		default void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
		}

		default void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
		}
	}

	interface Operation {
		ClientboundBossEventPacket.OperationType getType();

		void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler);

		void write(RegistryFriendlyByteBuf registryFriendlyByteBuf);
	}

	static enum OperationType {
		ADD(ClientboundBossEventPacket.AddOperation::new),
		REMOVE(registryFriendlyByteBuf -> ClientboundBossEventPacket.REMOVE_OPERATION),
		UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
		UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
		UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
		UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

		final StreamDecoder<RegistryFriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

		private OperationType(StreamDecoder<RegistryFriendlyByteBuf, ClientboundBossEventPacket.Operation> streamDecoder) {
			this.reader = streamDecoder;
		}
	}

	static record UpdateNameOperation(Component name) implements ClientboundBossEventPacket.Operation {
		private UpdateNameOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			this(ComponentSerialization.STREAM_CODEC.decode(registryFriendlyByteBuf));
		}

		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.UPDATE_NAME;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.updateName(uUID, this.name);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			ComponentSerialization.STREAM_CODEC.encode(registryFriendlyByteBuf, this.name);
		}
	}

	static record UpdateProgressOperation(float progress) implements ClientboundBossEventPacket.Operation {
		private UpdateProgressOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			this(registryFriendlyByteBuf.readFloat());
		}

		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.UPDATE_PROGRESS;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.updateProgress(uUID, this.progress);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			registryFriendlyByteBuf.writeFloat(this.progress);
		}
	}

	static class UpdatePropertiesOperation implements ClientboundBossEventPacket.Operation {
		private final boolean darkenScreen;
		private final boolean playMusic;
		private final boolean createWorldFog;

		UpdatePropertiesOperation(boolean bl, boolean bl2, boolean bl3) {
			this.darkenScreen = bl;
			this.playMusic = bl2;
			this.createWorldFog = bl3;
		}

		private UpdatePropertiesOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			int i = registryFriendlyByteBuf.readUnsignedByte();
			this.darkenScreen = (i & 1) > 0;
			this.playMusic = (i & 2) > 0;
			this.createWorldFog = (i & 4) > 0;
		}

		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.UPDATE_PROPERTIES;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.updateProperties(uUID, this.darkenScreen, this.playMusic, this.createWorldFog);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			registryFriendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
		}
	}

	static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
		private final BossEvent.BossBarColor color;
		private final BossEvent.BossBarOverlay overlay;

		UpdateStyleOperation(BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
			this.color = bossBarColor;
			this.overlay = bossBarOverlay;
		}

		private UpdateStyleOperation(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			this.color = registryFriendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
			this.overlay = registryFriendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
		}

		@Override
		public ClientboundBossEventPacket.OperationType getType() {
			return ClientboundBossEventPacket.OperationType.UPDATE_STYLE;
		}

		@Override
		public void dispatch(UUID uUID, ClientboundBossEventPacket.Handler handler) {
			handler.updateStyle(uUID, this.color, this.overlay);
		}

		@Override
		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			registryFriendlyByteBuf.writeEnum(this.color);
			registryFriendlyByteBuf.writeEnum(this.overlay);
		}
	}
}
