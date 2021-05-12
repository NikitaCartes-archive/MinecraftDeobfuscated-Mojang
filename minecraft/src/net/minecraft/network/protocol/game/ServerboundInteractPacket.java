package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
	private final int entityId;
	private final ServerboundInteractPacket.Action action;
	private final boolean usingSecondaryAction;
	static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
		@Override
		public ServerboundInteractPacket.ActionType getType() {
			return ServerboundInteractPacket.ActionType.ATTACK;
		}

		@Override
		public void dispatch(ServerboundInteractPacket.Handler handler) {
			handler.onAttack();
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
		}
	};

	private ServerboundInteractPacket(int i, boolean bl, ServerboundInteractPacket.Action action) {
		this.entityId = i;
		this.action = action;
		this.usingSecondaryAction = bl;
	}

	public static ServerboundInteractPacket createAttackPacket(Entity entity, boolean bl) {
		return new ServerboundInteractPacket(entity.getId(), bl, ATTACK_ACTION);
	}

	public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean bl, InteractionHand interactionHand) {
		return new ServerboundInteractPacket(entity.getId(), bl, new ServerboundInteractPacket.InteractionAction(interactionHand));
	}

	public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean bl, InteractionHand interactionHand, Vec3 vec3) {
		return new ServerboundInteractPacket(entity.getId(), bl, new ServerboundInteractPacket.InteractionAtLocationAction(interactionHand, vec3));
	}

	public ServerboundInteractPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		ServerboundInteractPacket.ActionType actionType = friendlyByteBuf.readEnum(ServerboundInteractPacket.ActionType.class);
		this.action = (ServerboundInteractPacket.Action)actionType.reader.apply(friendlyByteBuf);
		this.usingSecondaryAction = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeEnum(this.action.getType());
		this.action.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.usingSecondaryAction);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleInteract(this);
	}

	@Nullable
	public Entity getTarget(ServerLevel serverLevel) {
		return serverLevel.getEntityOrPart(this.entityId);
	}

	public boolean isUsingSecondaryAction() {
		return this.usingSecondaryAction;
	}

	public void dispatch(ServerboundInteractPacket.Handler handler) {
		this.action.dispatch(handler);
	}

	interface Action {
		ServerboundInteractPacket.ActionType getType();

		void dispatch(ServerboundInteractPacket.Handler handler);

		void write(FriendlyByteBuf friendlyByteBuf);
	}

	static enum ActionType {
		INTERACT(ServerboundInteractPacket.InteractionAction::new),
		ATTACK(friendlyByteBuf -> ServerboundInteractPacket.ATTACK_ACTION),
		INTERACT_AT(ServerboundInteractPacket.InteractionAtLocationAction::new);

		final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

		private ActionType(Function<FriendlyByteBuf, ServerboundInteractPacket.Action> function) {
			this.reader = function;
		}
	}

	public interface Handler {
		void onInteraction(InteractionHand interactionHand);

		void onInteraction(InteractionHand interactionHand, Vec3 vec3);

		void onAttack();
	}

	static class InteractionAction implements ServerboundInteractPacket.Action {
		private final InteractionHand hand;

		InteractionAction(InteractionHand interactionHand) {
			this.hand = interactionHand;
		}

		private InteractionAction(FriendlyByteBuf friendlyByteBuf) {
			this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
		}

		@Override
		public ServerboundInteractPacket.ActionType getType() {
			return ServerboundInteractPacket.ActionType.INTERACT;
		}

		@Override
		public void dispatch(ServerboundInteractPacket.Handler handler) {
			handler.onInteraction(this.hand);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeEnum(this.hand);
		}
	}

	static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
		private final InteractionHand hand;
		private final Vec3 location;

		InteractionAtLocationAction(InteractionHand interactionHand, Vec3 vec3) {
			this.hand = interactionHand;
			this.location = vec3;
		}

		private InteractionAtLocationAction(FriendlyByteBuf friendlyByteBuf) {
			this.location = new Vec3((double)friendlyByteBuf.readFloat(), (double)friendlyByteBuf.readFloat(), (double)friendlyByteBuf.readFloat());
			this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
		}

		@Override
		public ServerboundInteractPacket.ActionType getType() {
			return ServerboundInteractPacket.ActionType.INTERACT_AT;
		}

		@Override
		public void dispatch(ServerboundInteractPacket.Handler handler) {
			handler.onInteraction(this.hand, this.location);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeFloat((float)this.location.x);
			friendlyByteBuf.writeFloat((float)this.location.y);
			friendlyByteBuf.writeFloat((float)this.location.z);
			friendlyByteBuf.writeEnum(this.hand);
		}
	}
}
