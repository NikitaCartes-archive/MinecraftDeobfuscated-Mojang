package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultipleTestTracker {
	private static final char NOT_STARTED_TEST_CHAR = ' ';
	private static final char ONGOING_TEST_CHAR = '_';
	private static final char SUCCESSFUL_TEST_CHAR = '+';
	private static final char FAILED_OPTIONAL_TEST_CHAR = 'x';
	private static final char FAILED_REQUIRED_TEST_CHAR = 'X';
	private final Collection<GameTestInfo> tests = Lists.<GameTestInfo>newArrayList();
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
			public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
			}

			@Override
			public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
				consumer.accept(gameTestInfo);
			}

			@Override
			public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
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

	public Collection<GameTestInfo> getFailedRequired() {
		return (Collection<GameTestInfo>)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).collect(Collectors.toList());
	}

	public Collection<GameTestInfo> getFailedOptional() {
		return (Collection<GameTestInfo>)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).collect(Collectors.toList());
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

	public void remove(GameTestInfo gameTestInfo) {
		this.tests.remove(gameTestInfo);
	}
}
