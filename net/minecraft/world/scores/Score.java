/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.scores;

import java.util.Comparator;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

public class Score {
    public static final Comparator<Score> SCORE_COMPARATOR = (score, score2) -> {
        if (score.getScore() > score2.getScore()) {
            return 1;
        }
        if (score.getScore() < score2.getScore()) {
            return -1;
        }
        return score2.getOwner().compareToIgnoreCase(score.getOwner());
    };
    private final Scoreboard scoreboard;
    @Nullable
    private final Objective objective;
    private final String owner;
    private int count;
    private boolean locked;
    private boolean forceUpdate;

    public Score(Scoreboard scoreboard, Objective objective, String string) {
        this.scoreboard = scoreboard;
        this.objective = objective;
        this.owner = string;
        this.locked = true;
        this.forceUpdate = true;
    }

    public void add(int i) {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        this.setScore(this.getScore() + i);
    }

    public void increment() {
        this.add(1);
    }

    public int getScore() {
        return this.count;
    }

    public void reset() {
        this.setScore(0);
    }

    public void setScore(int i) {
        int j = this.count;
        this.count = i;
        if (j != i || this.forceUpdate) {
            this.forceUpdate = false;
            this.getScoreboard().onScoreChanged(this);
        }
    }

    @Nullable
    public Objective getObjective() {
        return this.objective;
    }

    public String getOwner() {
        return this.owner;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean bl) {
        this.locked = bl;
    }
}

