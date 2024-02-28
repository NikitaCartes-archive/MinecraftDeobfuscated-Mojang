package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class TestCommand {
	public static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
	public static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int DEFAULT_CLEAR_RADIUS = 200;
	private static final int MAX_CLEAR_RADIUS = 1024;
	private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
	private static final int SHOW_POS_DURATION_MS = 10000;
	private static final int DEFAULT_X_SIZE = 5;
	private static final int DEFAULT_Y_SIZE = 5;
	private static final int DEFAULT_Z_SIZE = 5;
	private static final String STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND = "Structure block entity could not be found";
	private static final TestFinder.Builder<TestCommand.Runner> testFinder = new TestFinder.Builder<>(TestCommand.Runner::new);

	private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder,
		Function<CommandContext<CommandSourceStack>, TestCommand.Runner> function,
		Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function2
	) {
		return argumentBuilder.executes(commandContext -> ((TestCommand.Runner)function.apply(commandContext)).run())
			.then(
				Commands.argument("numberOfTimes", IntegerArgumentType.integer(0))
					.executes(
						commandContext -> ((TestCommand.Runner)function.apply(commandContext))
								.run(new RetryOptions(IntegerArgumentType.getInteger(commandContext, "numberOfTimes"), false))
					)
					.then(
						(ArgumentBuilder<CommandSourceStack, ?>)function2.apply(
							Commands.argument("untilFailed", BoolArgumentType.bool())
								.executes(
									commandContext -> ((TestCommand.Runner)function.apply(commandContext))
											.run(new RetryOptions(IntegerArgumentType.getInteger(commandContext, "numberOfTimes"), BoolArgumentType.getBool(commandContext, "untilFailed")))
								)
						)
					)
			);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> function
	) {
		return runWithRetryOptions(argumentBuilder, function, argumentBuilderx -> argumentBuilderx);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> function
	) {
		return runWithRetryOptions(
			argumentBuilder,
			function,
			argumentBuilderx -> argumentBuilderx.then(
					Commands.argument("rotationSteps", IntegerArgumentType.integer())
						.executes(
							commandContext -> ((TestCommand.Runner)function.apply(commandContext))
									.run(
										new RetryOptions(IntegerArgumentType.getInteger(commandContext, "numberOfTimes"), BoolArgumentType.getBool(commandContext, "untilFailed")),
										IntegerArgumentType.getInteger(commandContext, "rotationSteps")
									)
						)
						.then(
							Commands.argument("testsPerRow", IntegerArgumentType.integer())
								.executes(
									commandContext -> ((TestCommand.Runner)function.apply(commandContext))
											.run(
												new RetryOptions(IntegerArgumentType.getInteger(commandContext, "numberOfTimes"), BoolArgumentType.getBool(commandContext, "untilFailed")),
												IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
												IntegerArgumentType.getInteger(commandContext, "testsPerRow")
											)
								)
						)
				)
		);
	}

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = runWithRetryOptionsAndBuildInfo(
			Commands.argument("onlyRequiredTests", BoolArgumentType.bool()),
			commandContext -> testFinder.failedTests(commandContext, BoolArgumentType.getBool(commandContext, "onlyRequiredTests"))
		);
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2 = runWithRetryOptionsAndBuildInfo(
			Commands.argument("testClassName", TestClassNameArgument.testClassName()),
			commandContext -> testFinder.allTestsInClass(commandContext, TestClassNameArgument.getTestClassName(commandContext, "testClassName"))
		);
		commandDispatcher.register(
			Commands.literal("test")
				.then(
					Commands.literal("run")
						.then(
							runWithRetryOptionsAndBuildInfo(
								Commands.argument("testName", TestFunctionArgument.testFunctionArgument()), commandContext -> testFinder.byArgument(commandContext, "testName")
							)
						)
				)
				.then(
					Commands.literal("runmultiple")
						.then(
							Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
								.executes(commandContext -> testFinder.byArgument(commandContext, "testName").run())
								.then(
									Commands.argument("amount", IntegerArgumentType.integer())
										.executes(
											commandContext -> testFinder.createMultipleCopies(IntegerArgumentType.getInteger(commandContext, "amount"))
													.byArgument(commandContext, "testName")
													.run()
										)
								)
						)
				)
				.then(runWithRetryOptionsAndBuildInfo(Commands.literal("runall").then(argumentBuilder2), testFinder::allTests))
				.then(runWithRetryOptions(Commands.literal("runthese"), testFinder::allNearby))
				.then(runWithRetryOptions(Commands.literal("runclosest"), testFinder::nearest))
				.then(runWithRetryOptions(Commands.literal("runthat"), testFinder::lookedAt))
				.then(runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(argumentBuilder), testFinder::failedTests))
				.then(Commands.literal("resetclosest").executes(commandContext -> testFinder.nearest(commandContext).reset()))
				.then(Commands.literal("resetthese").executes(commandContext -> testFinder.allNearby(commandContext).reset()))
				.then(Commands.literal("resetthat").executes(commandContext -> testFinder.lookedAt(commandContext).reset()))
				.then(
					Commands.literal("export")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.executes(commandContext -> exportTestStructure(commandContext.getSource(), "minecraft:" + StringArgumentType.getString(commandContext, "testName")))
						)
				)
				.then(Commands.literal("exportclosest").executes(commandContext -> testFinder.nearest(commandContext).export()))
				.then(Commands.literal("exportthese").executes(commandContext -> testFinder.allNearby(commandContext).export()))
				.then(Commands.literal("exportthat").executes(commandContext -> testFinder.lookedAt(commandContext).export()))
				.then(Commands.literal("clearthat").executes(commandContext -> testFinder.lookedAt(commandContext).clear()))
				.then(Commands.literal("clearthese").executes(commandContext -> testFinder.allNearby(commandContext).clear()))
				.then(
					Commands.literal("clearall")
						.executes(commandContext -> testFinder.radius(commandContext, 200).clear())
						.then(
							Commands.argument("radius", IntegerArgumentType.integer())
								.executes(commandContext -> testFinder.radius(commandContext, Mth.clamp(IntegerArgumentType.getInteger(commandContext, "radius"), 0, 1024)).clear())
						)
				)
				.then(
					Commands.literal("import")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.executes(commandContext -> importTestStructure(commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))
						)
				)
				.then(Commands.literal("stop").executes(commandContext -> stopTests()))
				.then(
					Commands.literal("pos")
						.executes(commandContext -> showPos(commandContext.getSource(), "pos"))
						.then(
							Commands.argument("var", StringArgumentType.word())
								.executes(commandContext -> showPos(commandContext.getSource(), StringArgumentType.getString(commandContext, "var")))
						)
				)
				.then(
					Commands.literal("create")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.suggests(TestFunctionArgument::suggestTestFunction)
								.executes(commandContext -> createNewStructure(commandContext.getSource(), StringArgumentType.getString(commandContext, "testName"), 5, 5, 5))
								.then(
									Commands.argument("width", IntegerArgumentType.integer())
										.executes(
											commandContext -> createNewStructure(
													commandContext.getSource(),
													StringArgumentType.getString(commandContext, "testName"),
													IntegerArgumentType.getInteger(commandContext, "width"),
													IntegerArgumentType.getInteger(commandContext, "width"),
													IntegerArgumentType.getInteger(commandContext, "width")
												)
										)
										.then(
											Commands.argument("height", IntegerArgumentType.integer())
												.then(
													Commands.argument("depth", IntegerArgumentType.integer())
														.executes(
															commandContext -> createNewStructure(
																	commandContext.getSource(),
																	StringArgumentType.getString(commandContext, "testName"),
																	IntegerArgumentType.getInteger(commandContext, "width"),
																	IntegerArgumentType.getInteger(commandContext, "height"),
																	IntegerArgumentType.getInteger(commandContext, "depth")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int resetGameTestInfo(GameTestInfo gameTestInfo) {
		gameTestInfo.getLevel().getEntities(null, gameTestInfo.getStructureBounds()).stream().forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
		gameTestInfo.getStructureBlockEntity().placeStructure(gameTestInfo.getLevel());
		StructureUtils.removeBarriers(gameTestInfo.getStructureBounds(), gameTestInfo.getLevel());
		say(gameTestInfo.getLevel(), "Reset succeded for: " + gameTestInfo.getTestName(), ChatFormatting.GREEN);
		return 1;
	}

	static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack commandSourceStack, RetryOptions retryOptions, StructureBlockPosFinder structureBlockPosFinder) {
		return structureBlockPosFinder.findStructureBlockPos()
			.map(blockPos -> createGameTestInfo(blockPos, commandSourceStack.getLevel(), retryOptions))
			.flatMap(Optional::stream);
	}

	static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack commandSourceStack, RetryOptions retryOptions, TestFunctionFinder testFunctionFinder, int i) {
		return testFunctionFinder.findTestFunctions()
			.filter(testFunction -> verifyStructureExists(commandSourceStack.getLevel(), testFunction.structureName()))
			.map(testFunction -> new GameTestInfo(testFunction, StructureUtils.getRotationForRotationSteps(i), commandSourceStack.getLevel(), retryOptions));
	}

	private static Optional<GameTestInfo> createGameTestInfo(BlockPos blockPos, ServerLevel serverLevel, RetryOptions retryOptions) {
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		if (structureBlockEntity == null) {
			say(serverLevel, "Structure block entity could not be found", ChatFormatting.RED);
			return Optional.empty();
		} else {
			String string = structureBlockEntity.getMetaData();
			Optional<TestFunction> optional = GameTestRegistry.findTestFunction(string);
			if (optional.isEmpty()) {
				say(serverLevel, "Test function for test " + string + " could not be found", ChatFormatting.RED);
				return Optional.empty();
			} else {
				TestFunction testFunction = (TestFunction)optional.get();
				GameTestInfo gameTestInfo = new GameTestInfo(testFunction, structureBlockEntity.getRotation(), serverLevel, retryOptions);
				gameTestInfo.setStructureBlockPos(blockPos);
				return !verifyStructureExists(serverLevel, gameTestInfo.getStructureName()) ? Optional.empty() : Optional.of(gameTestInfo);
			}
		}
	}

	private static int createNewStructure(CommandSourceStack commandSourceStack, String string, int i, int j, int k) {
		if (i <= 48 && j <= 48 && k <= 48) {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			BlockPos blockPos = createTestPositionAround(commandSourceStack).below();
			StructureUtils.createNewEmptyStructureBlock(string.toLowerCase(), blockPos, new Vec3i(i, j, k), Rotation.NONE, serverLevel);
			BlockPos blockPos2 = blockPos.above();
			BlockPos blockPos3 = blockPos2.offset(i - 1, 0, k - 1);
			BlockPos.betweenClosedStream(blockPos2, blockPos3).forEach(blockPosx -> serverLevel.setBlockAndUpdate(blockPosx, Blocks.BEDROCK.defaultBlockState()));
			StructureUtils.addCommandBlockAndButtonToStartTest(blockPos, new BlockPos(1, 0, -1), Rotation.NONE, serverLevel);
			return 0;
		} else {
			throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
		}
	}

	private static int showPos(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		BlockHitResult blockHitResult = (BlockHitResult)commandSourceStack.getPlayerOrException().pick(10.0, 1.0F, false);
		BlockPos blockPos = blockHitResult.getBlockPos();
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockPos, 15, serverLevel);
		if (optional.isEmpty()) {
			optional = StructureUtils.findStructureBlockContainingPos(blockPos, 200, serverLevel);
		}

		if (optional.isEmpty()) {
			commandSourceStack.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockPos));
			return 0;
		} else {
			StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)optional.get());
			if (structureBlockEntity == null) {
				say(serverLevel, "Structure block entity could not be found", ChatFormatting.RED);
				return 0;
			} else {
				BlockPos blockPos2 = blockPos.subtract((Vec3i)optional.get());
				String string2 = blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ();
				String string3 = structureBlockEntity.getMetaData();
				Component component = Component.literal(string2)
					.setStyle(
						Style.EMPTY
							.withBold(true)
							.withColor(ChatFormatting.GREEN)
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard")))
							.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + string + " = new BlockPos(" + string2 + ");"))
					);
				commandSourceStack.sendSuccess(() -> Component.literal("Position relative to " + string3 + ": ").append(component), false);
				DebugPackets.sendGameTestAddMarker(serverLevel, new BlockPos(blockPos), string2, -2147418368, 10000);
				return 1;
			}
		}
	}

	static int stopTests() {
		GameTestTicker.SINGLETON.clear();
		return 1;
	}

	static int trackAndStartRunner(CommandSourceStack commandSourceStack, ServerLevel serverLevel, GameTestRunner gameTestRunner) {
		gameTestRunner.addListener(new TestCommand.TestBatchSummaryDisplayer(commandSourceStack));
		MultipleTestTracker multipleTestTracker = new MultipleTestTracker(gameTestRunner.getTestInfos());
		multipleTestTracker.addListener(new TestCommand.TestSummaryDisplayer(serverLevel, multipleTestTracker));
		multipleTestTracker.addFailureListener(gameTestInfo -> GameTestRegistry.rememberFailedTest(gameTestInfo.getTestFunction()));
		gameTestRunner.start();
		return 1;
	}

	static int saveAndExportTestStructure(CommandSourceStack commandSourceStack, StructureBlockEntity structureBlockEntity) {
		String string = structureBlockEntity.getStructureName();
		if (!structureBlockEntity.saveStructure(true)) {
			say(commandSourceStack, "Failed to save structure " + string);
		}

		return exportTestStructure(commandSourceStack, string);
	}

	private static int exportTestStructure(CommandSourceStack commandSourceStack, String string) {
		Path path = Paths.get(StructureUtils.testStructuresDir);
		ResourceLocation resourceLocation = new ResourceLocation(string);
		Path path2 = commandSourceStack.getLevel().getStructureManager().getPathToGeneratedStructure(resourceLocation, ".nbt");
		Path path3 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path2, resourceLocation.getPath(), path);
		if (path3 == null) {
			say(commandSourceStack, "Failed to export " + path2);
			return 1;
		} else {
			try {
				FileUtil.createDirectoriesSafe(path3.getParent());
			} catch (IOException var7) {
				say(commandSourceStack, "Could not create folder " + path3.getParent());
				LOGGER.error("Could not create export folder", (Throwable)var7);
				return 1;
			}

			say(commandSourceStack, "Exported " + string + " to " + path3.toAbsolutePath());
			return 0;
		}
	}

	private static boolean verifyStructureExists(ServerLevel serverLevel, String string) {
		if (serverLevel.getStructureManager().get(new ResourceLocation(string)).isEmpty()) {
			say(serverLevel, "Test structure " + string + " could not be found", ChatFormatting.RED);
			return false;
		} else {
			return true;
		}
	}

	static BlockPos createTestPositionAround(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		int i = commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
		return new BlockPos(blockPos.getX(), i + 1, blockPos.getZ() + 3);
	}

	static void say(CommandSourceStack commandSourceStack, String string) {
		commandSourceStack.sendSuccess(() -> Component.literal(string), false);
	}

	private static int importTestStructure(CommandSourceStack commandSourceStack, String string) {
		Path path = Paths.get(StructureUtils.testStructuresDir, string + ".snbt");
		ResourceLocation resourceLocation = new ResourceLocation("minecraft", string);
		Path path2 = commandSourceStack.getLevel().getStructureManager().getPathToGeneratedStructure(resourceLocation, ".nbt");

		try {
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			String string2 = IOUtils.toString(bufferedReader);
			Files.createDirectories(path2.getParent());
			OutputStream outputStream = Files.newOutputStream(path2);

			try {
				NbtIo.writeCompressed(NbtUtils.snbtToStructure(string2), outputStream);
			} catch (Throwable var11) {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Throwable var10) {
						var11.addSuppressed(var10);
					}
				}

				throw var11;
			}

			if (outputStream != null) {
				outputStream.close();
			}

			commandSourceStack.getLevel().getStructureManager().remove(resourceLocation);
			say(commandSourceStack, "Imported to " + path2.toAbsolutePath());
			return 0;
		} catch (CommandSyntaxException | IOException var12) {
			LOGGER.error("Failed to load structure {}", string, var12);
			return 1;
		}
	}

	static void say(ServerLevel serverLevel, String string, ChatFormatting chatFormatting) {
		serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
	}

	public static class Runner {
		private final TestFinder<TestCommand.Runner> finder;

		public Runner(TestFinder<TestCommand.Runner> testFinder) {
			this.finder = testFinder;
		}

		public int reset() {
			TestCommand.stopTests();
			return TestCommand.toGameTestInfos(this.finder.source(), RetryOptions.noRetries(), this.finder).map(TestCommand::resetGameTestInfo).toList().isEmpty()
				? 0
				: 1;
		}

		private <T> void logAndRun(Stream<T> stream, ToIntFunction<T> toIntFunction, Runnable runnable, Consumer<Integer> consumer) {
			int i = stream.mapToInt(toIntFunction).sum();
			if (i == 0) {
				runnable.run();
			} else {
				consumer.accept(i);
			}
		}

		public int clear() {
			TestCommand.stopTests();
			CommandSourceStack commandSourceStack = this.finder.source();
			ServerLevel serverLevel = commandSourceStack.getLevel();
			GameTestRunner.clearMarkers(serverLevel);
			this.logAndRun(
				this.finder.findStructureBlockPos(),
				blockPos -> {
					StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
					if (structureBlockEntity == null) {
						return 0;
					} else {
						BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
						StructureUtils.clearSpaceForStructure(boundingBox, serverLevel);
						return 1;
					}
				},
				() -> TestCommand.say(serverLevel, "Could not find any structures to clear", ChatFormatting.RED),
				integer -> TestCommand.say(commandSourceStack, "Cleared " + integer + " structures")
			);
			return 1;
		}

		public int export() {
			MutableBoolean mutableBoolean = new MutableBoolean(true);
			CommandSourceStack commandSourceStack = this.finder.source();
			ServerLevel serverLevel = commandSourceStack.getLevel();
			this.logAndRun(
				this.finder.findStructureBlockPos(),
				blockPos -> {
					StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
					if (structureBlockEntity == null) {
						TestCommand.say(serverLevel, "Structure block entity could not be found", ChatFormatting.RED);
						mutableBoolean.setFalse();
						return 0;
					} else {
						if (TestCommand.saveAndExportTestStructure(commandSourceStack, structureBlockEntity) != 0) {
							mutableBoolean.setFalse();
						}

						return 1;
					}
				},
				() -> TestCommand.say(serverLevel, "Could not find any structures to export", ChatFormatting.RED),
				integer -> TestCommand.say(commandSourceStack, "Exported " + integer + " structures")
			);
			return mutableBoolean.getValue() ? 0 : 1;
		}

		public int run(RetryOptions retryOptions, int i, int j) {
			TestCommand.stopTests();
			CommandSourceStack commandSourceStack = this.finder.source();
			ServerLevel serverLevel = commandSourceStack.getLevel();
			BlockPos blockPos = TestCommand.createTestPositionAround(commandSourceStack);
			Collection<GameTestInfo> collection = Stream.concat(
					TestCommand.toGameTestInfos(commandSourceStack, retryOptions, this.finder), TestCommand.toGameTestInfo(commandSourceStack, retryOptions, this.finder, i)
				)
				.toList();
			if (collection.isEmpty()) {
				TestCommand.say(commandSourceStack, "No tests found");
				return 0;
			} else {
				GameTestRunner.clearMarkers(serverLevel);
				GameTestRegistry.forgetFailedTests();
				TestCommand.say(commandSourceStack, "Running " + collection.size() + " tests...");
				GameTestRunner gameTestRunner = GameTestRunner.Builder.fromInfo(collection, serverLevel).newStructureSpawner(new StructureGridSpawner(blockPos, j)).build();
				return TestCommand.trackAndStartRunner(commandSourceStack, serverLevel, gameTestRunner);
			}
		}

		public int run(int i, int j) {
			return this.run(RetryOptions.noRetries(), i, j);
		}

		public int run(int i) {
			return this.run(RetryOptions.noRetries(), i, 8);
		}

		public int run(RetryOptions retryOptions, int i) {
			return this.run(retryOptions, i, 8);
		}

		public int run(RetryOptions retryOptions) {
			return this.run(retryOptions, 0, 8);
		}

		public int run() {
			return this.run(RetryOptions.noRetries());
		}
	}

	static record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener {
		@Override
		public void testBatchStarting(GameTestBatch gameTestBatch) {
			TestCommand.say(this.source, "Starting batch: " + gameTestBatch.name());
		}

		@Override
		public void testBatchFinished(GameTestBatch gameTestBatch) {
		}
	}

	public static record TestSummaryDisplayer(ServerLevel level, MultipleTestTracker tracker) implements GameTestListener {
		@Override
		public void testStructureLoaded(GameTestInfo gameTestInfo) {
		}

		@Override
		public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
			showTestSummaryIfAllDone(this.level, this.tracker);
		}

		@Override
		public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
			showTestSummaryIfAllDone(this.level, this.tracker);
		}

		@Override
		public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
			this.tracker.addTestToTrack(gameTestInfo2);
		}

		private static void showTestSummaryIfAllDone(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
			if (multipleTestTracker.isDone()) {
				TestCommand.say(serverLevel, "GameTest done! " + multipleTestTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
				if (multipleTestTracker.hasFailedRequired()) {
					TestCommand.say(serverLevel, multipleTestTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
				} else {
					TestCommand.say(serverLevel, "All required tests passed :)", ChatFormatting.GREEN);
				}

				if (multipleTestTracker.hasFailedOptional()) {
					TestCommand.say(serverLevel, multipleTestTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
				}
			}
		}
	}
}
