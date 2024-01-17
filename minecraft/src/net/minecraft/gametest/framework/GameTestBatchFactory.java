package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestBatchFactory {
	private static final int MAX_TESTS_PER_BATCH = 50;

	public static Collection<GameTestBatch> fromTestFunction(Collection<TestFunction> collection, ServerLevel serverLevel) {
		Map<String, List<TestFunction>> map = (Map<String, List<TestFunction>>)collection.stream().collect(Collectors.groupingBy(TestFunction::batchName));
		return (Collection<GameTestBatch>)map.entrySet()
			.stream()
			.flatMap(
				entry -> {
					String string = (String)entry.getKey();
					Collection<TestFunction> collectionx = (Collection<TestFunction>)entry.getValue();
					MutableInt mutableInt = new MutableInt();
					return Streams.stream(Iterables.partition(collectionx, 50))
						.map(list -> toGameTestBatch(list.stream().map(testFunction -> toGameTestInfo(testFunction, 0, serverLevel)).toList(), string, mutableInt));
				}
			)
			.collect(ImmutableList.toImmutableList());
	}

	public static GameTestInfo toGameTestInfo(TestFunction testFunction, int i, ServerLevel serverLevel) {
		return new GameTestInfo(testFunction, StructureUtils.getRotationForRotationSteps(i), serverLevel, RetryOptions.noRetries());
	}

	public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
		return collection -> {
			Map<String, List<GameTestInfo>> map = (Map<String, List<GameTestInfo>>)collection.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(gameTestInfo -> gameTestInfo.getTestFunction().batchName()));
			return (Collection<GameTestBatch>)map.entrySet().stream().flatMap(entry -> {
				String string = (String)entry.getKey();
				Collection<GameTestInfo> collectionx = (Collection<GameTestInfo>)entry.getValue();
				MutableInt mutableInt = new MutableInt();
				return Streams.stream(Iterables.partition(collectionx, 50)).map(list -> toGameTestBatch(ImmutableList.copyOf(list), string, mutableInt));
			}).collect(ImmutableList.toImmutableList());
		};
	}

	private static GameTestBatch toGameTestBatch(List<GameTestInfo> list, String string, MutableInt mutableInt) {
		Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
		Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
		return new GameTestBatch(string + ":" + mutableInt.incrementAndGet(), list, consumer, consumer2);
	}
}
