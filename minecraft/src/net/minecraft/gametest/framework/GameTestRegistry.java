package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestRegistry {
	private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.<TestFunction>newArrayList();
	private static final Set<String> TEST_CLASS_NAMES = Sets.<String>newHashSet();
	private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.<String, Consumer<ServerLevel>>newHashMap();
	private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.<String, Consumer<ServerLevel>>newHashMap();
	private static final Collection<TestFunction> LAST_FAILED_TESTS = Sets.<TestFunction>newHashSet();

	public static void register(Class<?> class_) {
		Arrays.stream(class_.getDeclaredMethods()).forEach(GameTestRegistry::register);
	}

	public static void register(Method method) {
		String string = method.getDeclaringClass().getSimpleName();
		GameTest gameTest = (GameTest)method.getAnnotation(GameTest.class);
		if (gameTest != null) {
			TEST_FUNCTIONS.add(turnMethodIntoTestFunction(method));
			TEST_CLASS_NAMES.add(string);
		}

		GameTestGenerator gameTestGenerator = (GameTestGenerator)method.getAnnotation(GameTestGenerator.class);
		if (gameTestGenerator != null) {
			TEST_FUNCTIONS.addAll(useTestGeneratorMethod(method));
			TEST_CLASS_NAMES.add(string);
		}

		registerBatchFunction(method, BeforeBatch.class, BeforeBatch::batch, BEFORE_BATCH_FUNCTIONS);
		registerBatchFunction(method, AfterBatch.class, AfterBatch::batch, AFTER_BATCH_FUNCTIONS);
	}

	private static <T extends Annotation> void registerBatchFunction(
		Method method, Class<T> class_, Function<T, String> function, Map<String, Consumer<ServerLevel>> map
	) {
		T annotation = (T)method.getAnnotation(class_);
		if (annotation != null) {
			String string = (String)function.apply(annotation);
			Consumer<ServerLevel> consumer = (Consumer<ServerLevel>)map.putIfAbsent(string, turnMethodIntoConsumer(method));
			if (consumer != null) {
				throw new RuntimeException("Hey, there should only be one " + class_ + " method per batch. Batch '" + string + "' has more than one!");
			}
		}
	}

	public static Collection<TestFunction> getTestFunctionsForClassName(String string) {
		return (Collection<TestFunction>)TEST_FUNCTIONS.stream().filter(testFunction -> isTestFunctionPartOfClass(testFunction, string)).collect(Collectors.toList());
	}

	public static Collection<TestFunction> getAllTestFunctions() {
		return TEST_FUNCTIONS;
	}

	public static Collection<String> getAllTestClassNames() {
		return TEST_CLASS_NAMES;
	}

	public static boolean isTestClass(String string) {
		return TEST_CLASS_NAMES.contains(string);
	}

	@Nullable
	public static Consumer<ServerLevel> getBeforeBatchFunction(String string) {
		return (Consumer<ServerLevel>)BEFORE_BATCH_FUNCTIONS.get(string);
	}

	@Nullable
	public static Consumer<ServerLevel> getAfterBatchFunction(String string) {
		return (Consumer<ServerLevel>)AFTER_BATCH_FUNCTIONS.get(string);
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

	private static Collection<TestFunction> useTestGeneratorMethod(Method method) {
		try {
			Object object = method.getDeclaringClass().newInstance();
			return (Collection<TestFunction>)method.invoke(object);
		} catch (ReflectiveOperationException var2) {
			throw new RuntimeException(var2);
		}
	}

	private static TestFunction turnMethodIntoTestFunction(Method method) {
		GameTest gameTest = (GameTest)method.getAnnotation(GameTest.class);
		String string = method.getDeclaringClass().getSimpleName();
		String string2 = string.toLowerCase();
		String string3 = string2 + "." + method.getName().toLowerCase();
		String string4 = gameTest.template().isEmpty() ? string3 : string2 + "." + gameTest.template();
		String string5 = gameTest.batch();
		Rotation rotation = StructureUtils.getRotationForRotationSteps(gameTest.rotationSteps());
		return new TestFunction(
			string5,
			string3,
			string4,
			rotation,
			gameTest.timeoutTicks(),
			gameTest.setupTicks(),
			gameTest.required(),
			gameTest.requiredSuccesses(),
			gameTest.attempts(),
			(Consumer<GameTestHelper>)turnMethodIntoConsumer(method)
		);
	}

	private static Consumer<?> turnMethodIntoConsumer(Method method) {
		return object -> {
			try {
				Object object2 = method.getDeclaringClass().newInstance();
				method.invoke(object2, object);
			} catch (InvocationTargetException var3) {
				if (var3.getCause() instanceof RuntimeException) {
					throw (RuntimeException)var3.getCause();
				} else {
					throw new RuntimeException(var3.getCause());
				}
			} catch (ReflectiveOperationException var4) {
				throw new RuntimeException(var4);
			}
		};
	}

	private static boolean isTestFunctionPartOfClass(TestFunction testFunction, String string) {
		return testFunction.getTestName().toLowerCase().startsWith(string.toLowerCase() + ".");
	}

	public static Collection<TestFunction> getLastFailedTests() {
		return LAST_FAILED_TESTS;
	}

	public static void rememberFailedTest(TestFunction testFunction) {
		LAST_FAILED_TESTS.add(testFunction);
	}

	public static void forgetFailedTests() {
		LAST_FAILED_TESTS.clear();
	}
}
