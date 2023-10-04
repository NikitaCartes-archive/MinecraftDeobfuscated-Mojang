package net.minecraft.network.protocol.game;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
		}
	};

	private ClientboundBossEventPacket(UUID uUID, ClientboundBossEventPacket.Operation operation) {
		this.id = uUID;
		this.operation = operation;
	}

	public ClientboundBossEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readUUID();
		ClientboundBossEventPacket.OperationType operationType = friendlyByteBuf.readEnum(ClientboundBossEventPacket.OperationType.class);
		this.operation = (ClientboundBossEventPacket.Operation)operationType.reader.apply(friendlyByteBuf);
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

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
		friendlyByteBuf.writeEnum(this.operation.getType());
		this.operation.write(friendlyByteBuf);
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

		private AddOperation(FriendlyByteBuf friendlyByteBuf) {
			this.name = friendlyByteBuf.readComponentTrusted();
			this.progress = friendlyByteBuf.readFloat();
			this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
			this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
			int i = friendlyByteBuf.readUnsignedByte();
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeComponent(this.name);
			friendlyByteBuf.writeFloat(this.progress);
			friendlyByteBuf.writeEnum(this.color);
			friendlyByteBuf.writeEnum(this.overlay);
			friendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
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

		void write(FriendlyByteBuf friendlyByteBuf);
	}

	static enum OperationType {
		ADD(ClientboundBossEventPacket.AddOperation::new),
		REMOVE(friendlyByteBuf -> ClientboundBossEventPacket.REMOVE_OPERATION),
		UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
		UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
		UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
		UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

		final Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

		private OperationType(Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> function) {
			this.reader = function;
		}
	}

	static class UpdateNameOperation implements ClientboundBossEventPacket.Operation {
		private final Component name;

		UpdateNameOperation(Component component) {
			this.name = component;
		}

		private UpdateNameOperation(FriendlyByteBuf friendlyByteBuf) {
			this.name = friendlyByteBuf.readComponentTrusted();
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeComponent(this.name);
		}
	}

	static class UpdateProgressOperation implements ClientboundBossEventPacket.Operation {
		private final float progress;

		UpdateProgressOperation(float f) {
			this.progress = f;
		}

		private UpdateProgressOperation(FriendlyByteBuf friendlyByteBuf) {
			this.progress = friendlyByteBuf.readFloat();
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeFloat(this.progress);
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

		private UpdatePropertiesOperation(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readUnsignedByte();
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
		}
	}

	static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
		private final BossEvent.BossBarColor color;
		private final BossEvent.BossBarOverlay overlay;

		UpdateStyleOperation(BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
			this.color = bossBarColor;
			this.overlay = bossBarOverlay;
		}

		private UpdateStyleOperation(FriendlyByteBuf friendlyByteBuf) {
			this.color = friendlyByteBuf.readEnum(BossEvent.BossBarColor.class);
			this.overlay = friendlyByteBuf.readEnum(BossEvent.BossBarOverlay.class);
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
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeEnum(this.color);
			friendlyByteBuf.writeEnum(this.overlay);
		}
	}
}
