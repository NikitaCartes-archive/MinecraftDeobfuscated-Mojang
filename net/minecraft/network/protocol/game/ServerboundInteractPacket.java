/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ServerboundInteractPacket
implements Packet<ServerGamePacketListener> {
    private final int entityId;
    private final Action action;
    private final boolean usingSecondaryAction;
    static final Action ATTACK_ACTION = new Action(){

        @Override
        public ActionType getType() {
            return ActionType.ATTACK;
        }

        @Override
        public void dispatch(Handler handler) {
            handler.onAttack();
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
        }
    };

    private ServerboundInteractPacket(int i, boolean bl, Action action) {
        this.entityId = i;
        this.action = action;
        this.usingSecondaryAction = bl;
    }

    public static ServerboundInteractPacket createAttackPacket(Entity entity, boolean bl) {
        return new ServerboundInteractPacket(entity.getId(), bl, ATTACK_ACTION);
    }

    public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean bl, InteractionHand interactionHand) {
        return new ServerboundInteractPacket(entity.getId(), bl, new InteractionAction(interactionHand));
    }

    public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean bl, InteractionHand interactionHand, Vec3 vec3) {
        return new ServerboundInteractPacket(entity.getId(), bl, new InteractionAtLocationAction(interactionHand, vec3));
    }

    public ServerboundInteractPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        ActionType actionType = friendlyByteBuf.readEnum(ActionType.class);
        this.action = actionType.reader.apply(friendlyByteBuf);
        this.usingSecondaryAction = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeEnum(this.action.getType());
        this.action.write(friendlyByteBuf);
        friendlyByteBuf.writeBoolean(this.usingSecondaryAction);
    }

    @Override
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

    public void dispatch(Handler handler) {
        this.action.dispatch(handler);
    }

    static interface Action {
        public ActionType getType();

        public void dispatch(Handler var1);

        public void write(FriendlyByteBuf var1);
    }

    static class InteractionAction
    implements Action {
        private final InteractionHand hand;

        InteractionAction(InteractionHand interactionHand) {
            this.hand = interactionHand;
        }

        private InteractionAction(FriendlyByteBuf friendlyByteBuf) {
            this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
        }

        @Override
        public ActionType getType() {
            return ActionType.INTERACT;
        }

        @Override
        public void dispatch(Handler handler) {
            handler.onInteraction(this.hand);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeEnum(this.hand);
        }
    }

    static class InteractionAtLocationAction
    implements Action {
        private final InteractionHand hand;
        private final Vec3 location;

        InteractionAtLocationAction(InteractionHand interactionHand, Vec3 vec3) {
            this.hand = interactionHand;
            this.location = vec3;
        }

        private InteractionAtLocationAction(FriendlyByteBuf friendlyByteBuf) {
            this.location = new Vec3(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
            this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
        }

        @Override
        public ActionType getType() {
            return ActionType.INTERACT_AT;
        }

        @Override
        public void dispatch(Handler handler) {
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

    static enum ActionType {
        INTERACT(InteractionAction::new),
        ATTACK(friendlyByteBuf -> ATTACK_ACTION),
        INTERACT_AT(InteractionAtLocationAction::new);

        final Function<FriendlyByteBuf, Action> reader;

        private ActionType(Function<FriendlyByteBuf, Action> function) {
            this.reader = function;
        }
    }

    public static interface Handler {
        public void onInteraction(InteractionHand var1);

        public void onInteraction(InteractionHand var1, Vec3 var2);

        public void onAttack();
    }
}

