package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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
		private final UnaryOperator<Supplier<Stream<TestFunction>>> testFunctionFinderWrapper;
		private final UnaryOperator<Supplier<Stream<BlockPos>>> structureBlockPosFinderWrapper;

		public Builder(Function<TestFinder<T>, T> function) {
			this.contextProvider = function;
			this.testFunctionFinderWrapper = supplier -> supplier;
			this.structureBlockPosFinderWrapper = supplier -> supplier;
		}

		private Builder(
			Function<TestFinder<T>, T> function, UnaryOperator<Supplier<Stream<TestFunction>>> unaryOperator, UnaryOperator<Supplier<Stream<BlockPos>>> unaryOperator2
		) {
			this.contextProvider = function;
			this.testFunctionFinderWrapper = unaryOperator;
			this.structureBlockPosFinderWrapper = unaryOperator2;
		}

		public TestFinder.Builder<T> createMultipleCopies(int i) {
			return new TestFinder.Builder<>(this.contextProvider, createCopies(i), createCopies(i));
		}

		private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int i) {
			return supplier -> {
				List<Q> list = new LinkedList();
				List<Q> list2 = ((Stream)supplier.get()).toList();

				for (int j = 0; j < i; j++) {
					list.addAll(list2);
				}

				return list::stream;
			};
		}

		private T build(CommandSourceStack commandSourceStack, TestFunctionFinder testFunctionFinder, StructureBlockPosFinder structureBlockPosFinder) {
			return new TestFinder<T>(
					commandSourceStack,
					this.contextProvider,
					((Supplier)this.testFunctionFinderWrapper.apply(testFunctionFinder::findTestFunctions))::get,
					((Supplier)this.structureBlockPosFinderWrapper.apply(structureBlockPosFinder::findStructureBlockPos))::get
				)
				.get();
		}

		public T radius(CommandContext<CommandSourceStack> commandContext, int i) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			return this.build(
				commandSourceStack,
				TestFinder.NO_FUNCTIONS,
				() -> StructureUtils.radiusStructureBlockPos(i, commandSourceStack.getPosition(), commandSourceStack.getLevel())
			);
		}

		public T nearest(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
			return this.build(
				commandSourceStack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findNearestStructureBlock(blockPos, 15, commandSourceStack.getLevel()).stream()
			);
		}

		public T allNearby(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
			return this.build(commandSourceStack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockPos, 200, commandSourceStack.getLevel()));
		}

		public T lookedAt(CommandContext<CommandSourceStack> commandContext) {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			return this.build(
				commandSourceStack,
				TestFinder.NO_FUNCTIONS,
				() -> StructureUtils.lookedAtStructureBlockPos(
						BlockPos.containing(commandSourceStack.getPosition()), commandSourceStack.getPlayer().getCamera(), commandSourceStack.getLevel()
					)
			);
		}

		public T allTests(CommandContext<CommandSourceStack> commandContext) {
			return this.build(
				commandContext.getSource(),
				() -> GameTestRegistry.getAllTestFunctions().stream().filter(testFunction -> !testFunction.manualOnly()),
				TestFinder.NO_STRUCTURES
			);
		}

		public T allTestsInClass(CommandContext<CommandSourceStack> commandContext, String string) {
			return this.build(
				commandContext.getSource(),
				() -> GameTestRegistry.getTestFunctionsForClassName(string).filter(testFunction -> !testFunction.manualOnly()),
				TestFinder.NO_STRUCTURES
			);
		}

		public T failedTests(CommandContext<CommandSourceStack> commandContext, boolean bl) {
			return this.build(
				commandContext.getSource(), () -> GameTestRegistry.getLastFailedTests().filter(testFunction -> !bl || testFunction.required()), TestFinder.NO_STRUCTURES
			);
		}

		public T byArgument(CommandContext<CommandSourceStack> commandContext, String string) {
			return this.build(commandContext.getSource(), () -> Stream.of(TestFunctionArgument.getTestFunction(commandContext, string)), TestFinder.NO_STRUCTURES);
		}

		public T failedTests(CommandContext<CommandSourceStack> commandContext) {
			return this.failedTests(commandContext, false);
		}
	}
}
