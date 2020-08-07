package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestRegistry {
	private static final Collection<TestFunction> testFunctions = Lists.<TestFunction>newArrayList();
	private static final Set<String> testClassNames = Sets.<String>newHashSet();
	private static final Map<String, Consumer<ServerLevel>> beforeBatchFunctions = Maps.<String, Consumer<ServerLevel>>newHashMap();
	private static final Collection<TestFunction> lastFailedTests = Sets.<TestFunction>newHashSet();

	public static Collection<TestFunction> getTestFunctionsForClassName(String string) {
		return (Collection<TestFunction>)testFunctions.stream().filter(testFunction -> isTestFunctionPartOfClass(testFunction, string)).collect(Collectors.toList());
	}

	public static Collection<TestFunction> getAllTestFunctions() {
		return testFunctions;
	}

	public static Collection<String> getAllTestClassNames() {
		return testClassNames;
	}

	public static boolean isTestClass(String string) {
		return testClassNames.contains(string);
	}

	@Nullable
	public static Consumer<ServerLevel> getBeforeBatchFunction(String string) {
		return (Consumer<ServerLevel>)beforeBatchFunctions.get(string);
	}

	public static Optional<TestFunction> findTestFunction(String string) {
		return getAllTestFunctions().stream().filter(testFunction -> testFunction.getTestName().equalsIgnoreCase(string)).findFirst();
	}

	public static TestFunction getTestFunction(String string) {
		Optional<TestFunction> optional = findTestFunction(string);
		if (!optional.isPresent()) {
			throw new IllegalArgumentException("Can't find the test function for " + string);
		} else {
			return (TestFunction)optional.get();
		}
	}

	private static boolean isTestFunctionPartOfClass(TestFunction testFunction, String string) {
		return testFunction.getTestName().toLowerCase().startsWith(string.toLowerCase() + ".");
	}

	public static Collection<TestFunction> getLastFailedTests() {
		return lastFailedTests;
	}

	public static void rememberFailedTest(TestFunction testFunction) {
		lastFailedTests.add(testFunction);
	}

	public static void forgetFailedTests() {
		lastFailedTests.clear();
	}
}
