package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;

@Immutable
public class DifficultyInstance {
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

	public boolean isHarderThan(float f) {
		return this.effectiveDifficulty > f;
	}

	public float getSpecialMultiplier() {
		if (this.effectiveDifficulty < 2.0F) {
			return 0.0F;
		} else {
			return this.effectiveDifficulty > 4.0F ? 1.0F : (this.effectiveDifficulty - 2.0F) / 2.0F;
		}
	}

	private float calculateDifficulty(Difficulty difficulty, long l, long m, float f) {
		if (difficulty == Difficulty.PEACEFUL) {
			return 0.0F;
		} else {
			boolean bl = difficulty == Difficulty.HARD;
			float g = 0.75F;
			float h = Mth.clamp(((float)l + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
			g += h;
			float i = 0.0F;
			i += Mth.clamp((float)m / 3600000.0F, 0.0F, 1.0F) * (bl ? 1.0F : 0.75F);
			i += Mth.clamp(f * 0.25F, 0.0F, h);
			if (difficulty == Difficulty.EASY) {
				i *= 0.5F;
			}

			g += i;
			return (float)difficulty.getId() * g;
		}
	}
}
