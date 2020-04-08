/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;

public abstract class NodeEvaluator {
    protected PathNavigationRegion level;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<Node>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;

    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        this.level = pathNavigationRegion;
        this.mob = mob;
        this.nodes.clear();
        this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0f);
        this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0f);
        this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0f);
    }

    public void done() {
        this.level = null;
        this.mob = null;
    }

    protected Node getNode(BlockPos blockPos) {
        return this.getNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    protected Node getNode(int i, int j, int k) {
        return this.nodes.computeIfAbsent(Node.createHash(i, j, k), l -> new Node(i, j, k));
    }

    public abstract Node getStart();

    public abstract Target getGoal(double var1, double var3, double var5);

    public abstract int getNeighbors(Node[] var1, Node var2);

    public abstract BlockPathTypes getBlockPathType(BlockGetter var1, int var2, int var3, int var4, Mob var5, int var6, int var7, int var8, boolean var9, boolean var10);

    public abstract BlockPathTypes getBlockPathType(BlockGetter var1, int var2, int var3, int var4);

    public void setCanPassDoors(boolean bl) {
        this.canPassDoors = bl;
    }

    public void setCanOpenDoors(boolean bl) {
        this.canOpenDoors = bl;
    }

    public void setCanFloat(boolean bl) {
        this.canFloat = bl;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }
}

