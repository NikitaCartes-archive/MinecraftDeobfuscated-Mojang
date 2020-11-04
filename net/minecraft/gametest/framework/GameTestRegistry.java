/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class GameTestRegistry {
    private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
    private static final Set<String> TEST_CLASS_NAMES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Collection<TestFunction> LAST_FAILED_TESTS = Sets.newHashSet();

    public static Collection<TestFunction> getTestFunctionsForClassName(String string) {
        return TEST_FUNCTIONS.stream().filter(testFunction -> GameTestRegistry.isTestFunctionPartOfClass(testFunction, string)).collect(Collectors.toList());
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
        return BEFORE_BATCH_FUNCTIONS.get(string);
    }

    @Nullable
    public static Consumer<ServerLevel> getAfterBatchFunction(String string) {
        return AFTER_BATCH_FUNCTIONS.get(string);
    }

    public static Optional<TestFunction> findTestFunction(String string) {
        return GameTestRegistry.getAllTestFunctions().stream().filter(testFunction -> testFunction.getTestName().equalsIgnoreCase(string)).findFirst();
    }

    public static TestFunction getTestFunction(String string) {
        Optional<TestFunction> optional = GameTestRegistry.findTestFunction(string);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Can't find the test function for " + string);
        }
        return optional.get();
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

