package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class MultipleTestTracker {
	private final Collection<GameTestInfo> tests = Lists.<GameTestInfo>newArrayList();
	@Nullable
	private final Collection<GameTestListener> listeners = Lists.<GameTestListener>newArrayList();

	public MultipleTestTracker() {
	}

	public MultipleTestTracker(Collection<GameTestInfo> collection) {
		this.tests.addAll(collection);
	}

	public void addTestToTrack(GameTestInfo gameTestInfo) {
		this.tests.add(gameTestInfo);
		this.listeners.forEach(gameTestInfo::addListener);
	}

	public void addListener(GameTestListener gameTestListener) {
		this.listeners.add(gameTestListener);
		this.tests.forEach(gameTestInfo -> gameTestInfo.addListener(gameTestListener));
	}

	public void addFailureListener(Consumer<GameTestInfo> consumer) {
		this.addListener(new GameTestListener() {
			@Override
			public void testStructureLoaded(GameTestInfo gameTestInfo) {
			}

			@Override
			public void testFailed(GameTestInfo gameTestInfo) {
				consumer.accept(gameTestInfo);
			}
		});
	}

	public int getFailedRequiredCount() {
		return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).count();
	}

	public int getFailedOptionalCount() {
		return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).count();
	}

	public int getDoneCount() {
		return (int)this.tests.stream().filter(GameTestInfo::isDone).count();
	}

	public boolean hasFailedRequired() {
		return this.getFailedRequiredCount() > 0;
	}

	public boolean hasFailedOptional() {
		return this.getFailedOptionalCount() > 0;
	}

	public int getTotalCount() {
		return this.tests.size();
	}

	public boolean isDone() {
		return this.getDoneCount() == this.getTotalCount();
	}

	public String getProgressBar() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append('[');
		this.tests.forEach(gameTestInfo -> {
			if (!gameTestInfo.hasStarted()) {
				stringBuffer.append(' ');
			} else if (gameTestInfo.hasSucceeded()) {
				stringBuffer.append('+');
			} else if (gameTestInfo.hasFailed()) {
				stringBuffer.append((char)(gameTestInfo.isRequired() ? 'X' : 'x'));
			} else {
				stringBuffer.append('_');
			}
		});
		stringBuffer.append(']');
		return stringBuffer.toString();
	}

	public String toString() {
		return this.getProgressBar();
	}
}
