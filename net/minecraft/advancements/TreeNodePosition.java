/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.advancements.Advancement;
import org.jetbrains.annotations.Nullable;

public class TreeNodePosition {
    private final Advancement advancement;
    @Nullable
    private final TreeNodePosition parent;
    @Nullable
    private final TreeNodePosition previousSibling;
    private final int childIndex;
    private final List<TreeNodePosition> children = Lists.newArrayList();
    private TreeNodePosition ancestor;
    @Nullable
    private TreeNodePosition thread;
    private int x;
    private float y;
    private float mod;
    private float change;
    private float shift;

    public TreeNodePosition(Advancement advancement, @Nullable TreeNodePosition treeNodePosition, @Nullable TreeNodePosition treeNodePosition2, int i, int j) {
        if (advancement.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position an invisible advancement!");
        }
        this.advancement = advancement;
        this.parent = treeNodePosition;
        this.previousSibling = treeNodePosition2;
        this.childIndex = i;
        this.ancestor = this;
        this.x = j;
        this.y = -1.0f;
        TreeNodePosition treeNodePosition3 = null;
        for (Advancement advancement2 : advancement.getChildren()) {
            treeNodePosition3 = this.addChild(advancement2, treeNodePosition3);
        }
    }

    @Nullable
    private TreeNodePosition addChild(Advancement advancement, @Nullable TreeNodePosition treeNodePosition) {
        if (advancement.getDisplay() != null) {
            treeNodePosition = new TreeNodePosition(advancement, this, treeNodePosition, this.children.size() + 1, this.x + 1);
            this.children.add(treeNodePosition);
        } else {
            for (Advancement advancement2 : advancement.getChildren()) {
                treeNodePosition = this.addChild(advancement2, treeNodePosition);
            }
        }
        return treeNodePosition;
    }

    private void firstWalk() {
        if (this.children.isEmpty()) {
            this.y = this.previousSibling != null ? this.previousSibling.y + 1.0f : 0.0f;
            return;
        }
        TreeNodePosition treeNodePosition = null;
        for (TreeNodePosition treeNodePosition2 : this.children) {
            treeNodePosition2.firstWalk();
            treeNodePosition = treeNodePosition2.apportion(treeNodePosition == null ? treeNodePosition2 : treeNodePosition);
        }
        this.executeShifts();
        float f = (this.children.get((int)0).y + this.children.get((int)(this.children.size() - 1)).y) / 2.0f;
        if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0f;
            this.mod = this.y - f;
        } else {
            this.y = f;
        }
    }

    private float secondWalk(float f, int i, float g) {
        this.y += f;
        this.x = i;
        if (this.y < g) {
            g = this.y;
        }
        for (TreeNodePosition treeNodePosition : this.children) {
            g = treeNodePosition.secondWalk(f + this.mod, i + 1, g);
        }
        return g;
    }

    private void thirdWalk(float f) {
        this.y += f;
        for (TreeNodePosition treeNodePosition : this.children) {
            treeNodePosition.thirdWalk(f);
        }
    }

    private void executeShifts() {
        float f = 0.0f;
        float g = 0.0f;
        for (int i = this.children.size() - 1; i >= 0; --i) {
            TreeNodePosition treeNodePosition = this.children.get(i);
            treeNodePosition.y += f;
            treeNodePosition.mod += f;
            f += treeNodePosition.shift + (g += treeNodePosition.change);
        }
    }

    @Nullable
    private TreeNodePosition previousOrThread() {
        if (this.thread != null) {
            return this.thread;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    @Nullable
    private TreeNodePosition nextOrThread() {
        if (this.thread != null) {
            return this.thread;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    private TreeNodePosition apportion(TreeNodePosition treeNodePosition) {
        if (this.previousSibling == null) {
            return treeNodePosition;
        }
        TreeNodePosition treeNodePosition2 = this;
        TreeNodePosition treeNodePosition3 = this;
        TreeNodePosition treeNodePosition4 = this.previousSibling;
        TreeNodePosition treeNodePosition5 = this.parent.children.get(0);
        float f = this.mod;
        float g = this.mod;
        float h = treeNodePosition4.mod;
        float i = treeNodePosition5.mod;
        while (treeNodePosition4.nextOrThread() != null && treeNodePosition2.previousOrThread() != null) {
            treeNodePosition4 = treeNodePosition4.nextOrThread();
            treeNodePosition2 = treeNodePosition2.previousOrThread();
            treeNodePosition5 = treeNodePosition5.previousOrThread();
            treeNodePosition3 = treeNodePosition3.nextOrThread();
            treeNodePosition3.ancestor = this;
            float j = treeNodePosition4.y + h - (treeNodePosition2.y + f) + 1.0f;
            if (j > 0.0f) {
                treeNodePosition4.getAncestor(this, treeNodePosition).moveSubtree(this, j);
                f += j;
                g += j;
            }
            h += treeNodePosition4.mod;
            f += treeNodePosition2.mod;
            i += treeNodePosition5.mod;
            g += treeNodePosition3.mod;
        }
        if (treeNodePosition4.nextOrThread() != null && treeNodePosition3.nextOrThread() == null) {
            treeNodePosition3.thread = treeNodePosition4.nextOrThread();
            treeNodePosition3.mod += h - g;
        } else {
            if (treeNodePosition2.previousOrThread() != null && treeNodePosition5.previousOrThread() == null) {
                treeNodePosition5.thread = treeNodePosition2.previousOrThread();
                treeNodePosition5.mod += f - i;
            }
            treeNodePosition = this;
        }
        return treeNodePosition;
    }

    private void moveSubtree(TreeNodePosition treeNodePosition, float f) {
        float g = treeNodePosition.childIndex - this.childIndex;
        if (g != 0.0f) {
            treeNodePosition.change -= f / g;
            this.change += f / g;
        }
        treeNodePosition.shift += f;
        treeNodePosition.y += f;
        treeNodePosition.mod += f;
    }

    private TreeNodePosition getAncestor(TreeNodePosition treeNodePosition, TreeNodePosition treeNodePosition2) {
        if (this.ancestor != null && treeNodePosition.parent.children.contains(this.ancestor)) {
            return this.ancestor;
        }
        return treeNodePosition2;
    }

    private void finalizePosition() {
        if (this.advancement.getDisplay() != null) {
            this.advancement.getDisplay().setLocation(this.x, this.y);
        }
        if (!this.children.isEmpty()) {
            for (TreeNodePosition treeNodePosition : this.children) {
                treeNodePosition.finalizePosition();
            }
        }
    }

    public static void run(Advancement advancement) {
        if (advancement.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position children of an invisible root!");
        }
        TreeNodePosition treeNodePosition = new TreeNodePosition(advancement, null, null, 1, 0);
        treeNodePosition.firstWalk();
        float f = treeNodePosition.secondWalk(0.0f, 0, treeNodePosition.y);
        if (f < 0.0f) {
            treeNodePosition.thirdWalk(-f);
        }
        treeNodePosition.finalizePosition();
    }
}

