/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.pathfinder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;

public class Target
extends Node {
    private float bestHeuristic = Float.MAX_VALUE;
    private Node bestNode;
    private boolean reached;

    public Target(Node node) {
        super(node.x, node.y, node.z);
    }

    @Environment(value=EnvType.CLIENT)
    public Target(int i, int j, int k) {
        super(i, j, k);
    }

    public void updateBest(float f, Node node) {
        if (f < this.bestHeuristic) {
            this.bestHeuristic = f;
            this.bestNode = node;
        }
    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    @Environment(value=EnvType.CLIENT)
    public static Target createFromStream(FriendlyByteBuf friendlyByteBuf) {
        Target target = new Target(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        target.walkedDistance = friendlyByteBuf.readFloat();
        target.costMalus = friendlyByteBuf.readFloat();
        target.closed = friendlyByteBuf.readBoolean();
        target.type = BlockPathTypes.values()[friendlyByteBuf.readInt()];
        target.f = friendlyByteBuf.readFloat();
        return target;
    }
}

