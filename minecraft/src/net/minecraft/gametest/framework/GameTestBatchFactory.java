package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatchFactory {
	private static final int MAX_TESTS_PER_BATCH = 50;

	public static Collection<GameTestBatch> fromTestFunction(Collection<TestFunction> collection, ServerLevel serverLevel) {
		Map<String, List<TestFunction>> map = (Map<String, List<TestFunction>>)collection.stream().collect(Collectors.groupingBy(TestFunction::batchName));
		return map.entrySet()
			.stream()
			.flatMap(
				entry -> {
					String string = (String)entry.getKey();
					List<TestFunction> list = (List<TestFunction>)entry.getValue();
					return Streams.mapWithIndex(
						Lists.partition(list, 50).stream(),
						(listx, l) -> toGameTestBatch(listx.stream().map(testFunction -> toGameTestInfo(testFunction, 0, serverLevel)).toList(), string, l)
					);
				}
			)
			.toList();
	}

	public static GameTestInfo toGameTestInfo(TestFunction testFunction, int i, ServerLevel serverLevel) {
		return new GameTestInfo(testFunction, StructureUtils.getRotationForRotationSteps(i), serverLevel, RetryOptions.noRetries());
	}

	public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
		return fromGameTestInfo(50);
	}

	public static GameTestRunner.GameTestBatcher fromGameTestInfo(int i) {
		return collection -> {
			Map<String, List<GameTestInfo>> map = (Map<String, List<GameTestInfo>>)collection.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(gameTestInfo -> gameTestInfo.getTestFunction().batchName()));
			return map.entrySet().stream().flatMap(entry -> {
				String string = (String)entry.getKey();
				List<GameTestInfo> list = (List<GameTestInfo>)entry.getValue();
				return Streams.mapWithIndex(Lists.partition(list, i).stream(), (listx, l) -> toGameTestBatch(List.copyOf(listx), string, l));
			}).toList();
		};
	}

	public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> collection, String string, long l) {
		Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
		Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
		return new GameTestBatch(string + ":" + l, collection, consumer, consumer2);
	}
}
