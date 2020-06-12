/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ServerboundInteractPacket
implements Packet<ServerGamePacketListener> {
    private int entityId;
    private Action action;
    private Vec3 location;
    private InteractionHand hand;
    private boolean usingSecondaryAction;

    public ServerboundInteractPacket() {
    }

    @Environment(value=EnvType.CLIENT)
    public ServerboundInteractPacket(Entity entity, boolean bl) {
        this.entityId = entity.getId();
        this.action = Action.ATTACK;
        this.usingSecondaryAction = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public ServerboundInteractPacket(Entity entity, InteractionHand interactionHand, boolean bl) {
        this.entityId = entity.getId();
        this.action = Action.INTERACT;
        this.hand = interactionHand;
        this.usingSecondaryAction = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public ServerboundInteractPacket(Entity entity, InteractionHand interactionHand, Vec3 vec3, boolean bl) {
        this.entityId = entity.getId();
        this.action = Action.INTERACT_AT;
        this.hand = interactionHand;
        this.location = vec3;
        this.usingSecondaryAction = bl;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.entityId = friendlyByteBuf.readVarInt();
        this.action = friendlyByteBuf.readEnum(Action.class);
        if (this.action == Action.INTERACT_AT) {
            this.location = new Vec3(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
        }
        if (this.action == Action.INTERACT || this.action == Action.INTERACT_AT) {
            this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
        }
        this.usingSecondaryAction = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeEnum(this.action);
        if (this.action == Action.INTERACT_AT) {
            friendlyByteBuf.writeFloat((float)this.location.x);
            friendlyByteBuf.writeFloat((float)this.location.y);
            friendlyByteBuf.writeFloat((float)this.location.z);
        }
        if (this.action == Action.INTERACT || this.action == Action.INTERACT_AT) {
            friendlyByteBuf.writeEnum(this.hand);
        }
        friendlyByteBuf.writeBoolean(this.usingSecondaryAction);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleInteract(this);
    }

    @Nullable
    public Entity getTarget(Level level) {
        return level.getEntity(this.entityId);
    }

    public Action getAction() {
        return this.action;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public Vec3 getLocation() {
        return this.location;
    }

    public boolean isUsingSecondaryAction() {
        return this.usingSecondaryAction;
    }

    public static enum Action {
        INTERACT,
        ATTACK,
        INTERACT_AT;

    }
}

