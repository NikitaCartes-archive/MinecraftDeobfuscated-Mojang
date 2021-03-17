package net.minecraft.util.profiling;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class EmptyProfileResults implements ProfileResults {
	public static final EmptyProfileResults EMPTY = new EmptyProfileResults();

	private EmptyProfileResults() {
	}

	@Environment(EnvType.CLIENT)
	@Override
	public List<ResultField> getTimes(String string) {
		return Collections.emptyList();
	}

	@Override
	public boolean saveResults(Path path) {
		return false;
	}

	@Override
	public long getStartTimeNano() {
		return 0L;
	}

	@Override
	public int getStartTimeTicks() {
		return 0;
	}

	@Override
	public long getEndTimeNano() {
		return 0L;
	}

	@Override
	public int getEndTimeTicks() {
		return 0;
	}
}
