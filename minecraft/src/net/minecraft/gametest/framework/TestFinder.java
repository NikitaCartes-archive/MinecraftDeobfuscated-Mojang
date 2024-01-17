package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

public class TestFinder<T> implements StructureBlockPosFinder, TestFunctionFinder {
	static final TestFunctionFinder NO_FUNCTIONS = Stream::empty;
	static final StructureBlockPosFinder NO_STRUCTURES = Stream::empty;
	private final TestFunctionFinder testFunctionFinder;
	private final StructureBlockPosFinder structureBlockPosFinder;
	private final CommandSourceStack source;
	private final Function<TestFinder<T>, T> contextProvider;

	@Override
	public Stream<BlockPos> findStructureBlockPos() {
		return this.structureBlockPosFinder.findStructureBlockPos();
	}

	TestFinder(
		CommandSourceStack commandSourceStack,
		Function<TestFinder<T>, T> function,
		TestFunctionFinder testFunctionFinder,
		StructureBlockPosFinder structureBlockPosFinder
	) {
		this.source = commandSourceStack;
		this.contextProvider = function;
		this.testFunctionFinder = testFunctionFinder;
		this.structureBlockPosFinder = structureBlockPosFinder;
	}

	T get() {
		return (T)this.contextProvider.apply(this);
	}

	public CommandSourceStack source() {
		return this.source;
	}

	@Override
	public Stream<TestFunction> findTestFunctions() {
		return this.testFunctionFinder.findTestFunctions();
	}

	public static class Builder<T> {
		private final Function<TestFinder<T>, T> contextProvider;

		public Builder(Function<TestFinder<T>, T> function) {
			this.contextProvider = function;
		}

		public T radius(CommandContext<CommandSourceStack> commandContext, int i) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			return new TestFinder<T>(
					commandSourceStack,
					this.contextProvider,
					TestFinder.NO_FUNCTIONS,
					() -> StructureUtils.radiusStructureBlockPos(i, commandSourceStack.getPosition(), commandSourceStack.getLevel())
				)
				.get();
		}

		public T nearest(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
			return new TestFinder<T>(
					commandSourceStack,
					this.contextProvider,
					TestFinder.NO_FUNCTIONS,
					() -> StructureUtils.findNearestStructureBlock(blockPos, 15, commandSourceStack.getLevel()).stream()
				)
				.get();
		}

		public T allNearby(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
			return new TestFinder<T>(
					commandSourceStack, this.contextProvider, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockPos, 200, commandSourceStack.getLevel())
				)
				.get();
		}

		public T lookedAt(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			return new TestFinder<T>(
					commandSourceStack,
					this.contextProvider,
					TestFinder.NO_FUNCTIONS,
					() -> StructureUtils.lookedAtStructureBlockPos(
							BlockPos.containing(commandSourceStack.getPosition()), commandSourceStack.getPlayer().getCamera(), commandSourceStack.getLevel()
						)
				)
				.get();
		}

		public T allTests(CommandContext<CommandSourceStack> commandContext) {
			return new TestFinder<T>(commandContext.getSource(), this.contextProvider, () -> GameTestRegistry.getAllTestFunctions().stream(), TestFinder.NO_STRUCTURES)
				.get();
		}

		public T allTestsInClass(CommandContext<CommandSourceStack> commandContext, String string) {
			return new TestFinder<T>(
					commandContext.getSource(), this.contextProvider, () -> GameTestRegistry.getTestFunctionsForClassName(string), TestFinder.NO_STRUCTURES
				)
				.get();
		}

		public T failedTests(CommandContext<CommandSourceStack> commandContext, boolean bl) {
			return new TestFinder<T>(
					commandContext.getSource(),
					this.contextProvider,
					() -> GameTestRegistry.getLastFailedTests().filter(testFunction -> !bl || testFunction.required()),
					TestFinder.NO_STRUCTURES
				)
				.get();
		}

		public T byArgument(CommandContext<CommandSourceStack> commandContext, String string) {
			return new TestFinder<T>(
					commandContext.getSource(), this.contextProvider, () -> Stream.of(TestFunctionArgument.getTestFunction(commandContext, string)), TestFinder.NO_STRUCTURES
				)
				.get();
		}

		public T failedTests(CommandContext<CommandSourceStack> commandContext) {
			return this.failedTests(commandContext, false);
		}
	}
}
