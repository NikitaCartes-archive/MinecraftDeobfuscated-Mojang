/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class DifficultyInstance {
    private static final float DIFFICULTY_TIME_GLOBAL_OFFSET = -72000.0f;
    private static final float MAX_DIFFICULTY_TIME_GLOBAL = 1440000.0f;
    private static final float MAX_DIFFICULTY_TIME_LOCAL = 3600000.0f;
    private final Difficulty base;
    private final float effectiveDifficulty;

    public DifficultyInstance(Difficulty difficulty, long l, long m, float f) {
        this.base = difficulty;
        this.effectiveDifficulty = this.calculateDifficulty(difficulty, l, m, f);
    }

    public Difficulty getDifficulty() {
        return this.base;
    }

    public float getEffectiveDifficulty() {
        return this.effectiveDifficulty;
    }

    public boolean isHard() {
        return this.effectiveDifficulty >= (float)Difficulty.HARD.ordinal();
    }

    public boolean isHarderThan(float f) {
        return this.effectiveDifficulty > f;
    }

    public float getSpecialMultiplier() {
        if (this.effectiveDifficulty < 2.0f) {
            return 0.0f;
        }
        if (this.effectiveDifficulty > 4.0f) {
            return 1.0f;
        }
        return (this.effectiveDifficulty - 2.0f) / 2.0f;
    }

    private float calculateDifficulty(Difficulty difficulty, long l, long m, float f) {
        if (difficulty == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        boolean bl = difficulty == Difficulty.HARD;
        float g = 0.75f;
        float h = Mth.clamp(((float)l + -72000.0f) / 1440000.0f, 0.0f, 1.0f) * 0.25f;
        g += h;
        float i = 0.0f;
        i += Mth.clamp((float)m / 3600000.0f, 0.0f, 1.0f) * (bl ? 1.0f : 0.75f);
        i += Mth.clamp(f * 0.25f, 0.0f, h);
        if (difficulty == Difficulty.EASY) {
            i *= 0.5f;
        }
        return (float)difficulty.getId() * (g += i);
    }
}

