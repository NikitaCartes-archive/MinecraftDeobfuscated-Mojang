package net.minecraft.world.scores;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

class PlayerScores {
	private final Map<Objective, Score> scores = new HashMap();

	@Nullable
	public Score get(Objective objective) {
		return (Score)this.scores.get(objective);
	}

	public Score getOrCreate(Objective objective, Consumer<Score> consumer) {
		return (Score)this.scores.computeIfAbsent(objective, objectivex -> {
			Score score = new Score();
			consumer.accept(score);
			return score;
		});
	}

	public boolean remove(Objective objective) {
		return this.scores.get(objective) != null;
	}

	public boolean hasScores() {
		return !this.scores.isEmpty();
	}

	public Object2IntMap<Objective> listScores() {
		Object2IntMap<Objective> object2IntMap = new Object2IntOpenHashMap<>();
		this.scores.forEach((objective, score) -> object2IntMap.put(objective, score.value()));
		return object2IntMap;
	}

	void setScore(Objective objective, Score score) {
		this.scores.put(objective, score);
	}

	Map<Objective, Score> listRawScores() {
		return Collections.unmodifiableMap(this.scores);
	}
}
