package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class TestCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int DEFAULT_CLEAR_RADIUS = 200;
	private static final int MAX_CLEAR_RADIUS = 1024;
	private static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
	private static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
	private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
	private static final int SHOW_POS_DURATION_MS = 10000;
	private static final int DEFAULT_X_SIZE = 5;
	private static final int DEFAULT_Y_SIZE = 5;
	private static final int DEFAULT_Z_SIZE = 5;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("test")
				.then(
					Commands.literal("runthis")
						.executes(commandContext -> runNearbyTest(commandContext.getSource(), false))
						.then(Commands.literal("untilFailed").executes(commandContext -> runNearbyTest(commandContext.getSource(), true)))
				)
				.then(Commands.literal("resetthis").executes(commandContext -> resetNearbyTest(commandContext.getSource())))
				.then(Commands.literal("runthese").executes(commandContext -> runAllNearbyTests(commandContext.getSource(), false)))
				.then(
					Commands.literal("runfailed")
						.executes(commandContext -> runLastFailedTests(commandContext.getSource(), false, 0, 8))
						.then(
							Commands.argument("onlyRequiredTests", BoolArgumentType.bool())
								.executes(commandContext -> runLastFailedTests(commandContext.getSource(), BoolArgumentType.getBool(commandContext, "onlyRequiredTests"), 0, 8))
								.then(
									Commands.argument("rotationSteps", IntegerArgumentType.integer())
										.executes(
											commandContext -> runLastFailedTests(
													commandContext.getSource(),
													BoolArgumentType.getBool(commandContext, "onlyRequiredTests"),
													IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
													8
												)
										)
										.then(
											Commands.argument("testsPerRow", IntegerArgumentType.integer())
												.executes(
													commandContext -> runLastFailedTests(
															commandContext.getSource(),
															BoolArgumentType.getBool(commandContext, "onlyRequiredTests"),
															IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
															IntegerArgumentType.getInteger(commandContext, "testsPerRow")
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("run")
						.then(
							Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
								.executes(commandContext -> runTest(commandContext.getSource(), TestFunctionArgument.getTestFunction(commandContext, "testName"), 0))
								.then(
									Commands.argument("rotationSteps", IntegerArgumentType.integer())
										.executes(
											commandContext -> runTest(
													commandContext.getSource(),
													TestFunctionArgument.getTestFunction(commandContext, "testName"),
													IntegerArgumentType.getInteger(commandContext, "rotationSteps")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("runall")
						.executes(commandContext -> runAllTests(commandContext.getSource(), 0, 8))
						.then(
							Commands.argument("testClassName", TestClassNameArgument.testClassName())
								.executes(
									commandContext -> runAllTestsInClass(commandContext.getSource(), TestClassNameArgument.getTestClassName(commandContext, "testClassName"), 0, 8)
								)
								.then(
									Commands.argument("rotationSteps", IntegerArgumentType.integer())
										.executes(
											commandContext -> runAllTestsInClass(
													commandContext.getSource(),
													TestClassNameArgument.getTestClassName(commandContext, "testClassName"),
													IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
													8
												)
										)
										.then(
											Commands.argument("testsPerRow", IntegerArgumentType.integer())
												.executes(
													commandContext -> runAllTestsInClass(
															commandContext.getSource(),
															TestClassNameArgument.getTestClassName(commandContext, "testClassName"),
															IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
															IntegerArgumentType.getInteger(commandContext, "testsPerRow")
														)
												)
										)
								)
						)
						.then(
							Commands.argument("rotationSteps", IntegerArgumentType.integer())
								.executes(commandContext -> runAllTests(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "rotationSteps"), 8))
								.then(
									Commands.argument("testsPerRow", IntegerArgumentType.integer())
										.executes(
											commandContext -> runAllTests(
													commandContext.getSource(),
													IntegerArgumentType.getInteger(commandContext, "rotationSteps"),
													IntegerArgumentType.getInteger(commandContext, "testsPerRow")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("export")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.executes(commandContext -> exportTestStructure(commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))
						)
				)
				.then(Commands.literal("exportthis").executes(commandContext -> exportNearestTestStructure(commandContext.getSource())))
				.then(Commands.literal("exportthese").executes(commandContext -> exportAllNearbyTests(commandContext.getSource())))
				.then(
					Commands.literal("import")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.executes(commandContext -> importTestStructure(commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))
						)
				)
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
				.then(
					Commands.literal("clearall")
						.executes(commandContext -> clearAllTests(commandContext.getSource(), 200))
						.then(
							Commands.argument("radius", IntegerArgumentType.integer())
								.executes(commandContext -> clearAllTests(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "radius")))
						)
				)
		);
	}

	private static int createNewStructure(CommandSourceStack commandSourceStack, String string, int i, int j, int k) {
		if (i <= 48 && j <= 48 && k <= 48) {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			BlockPos blockPos = createTestPositionAround(commandSourceStack).below();
			StructureUtils.createNewEmptyStructureBlock(string.toLowerCase(), blockPos, new Vec3i(i, j, k), Rotation.NONE, serverLevel);

			for (int l = 0; l < i; l++) {
				for (int m = 0; m < k; m++) {
					BlockPos blockPos2 = new BlockPos(blockPos.getX() + l, blockPos.getY() + 1, blockPos.getZ() + m);
					Block block = Blocks.POLISHED_ANDESITE;
					BlockInput blockInput = new BlockInput(block.defaultBlockState(), Collections.emptySet(), null);
					blockInput.place(serverLevel, blockPos2, 2);
				}
			}

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

	private static int runNearbyTest(CommandSourceStack commandSourceStack, boolean bl) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos2 = StructureUtils.findNearestStructureBlock(blockPos, 15, serverLevel);
		if (blockPos2 == null) {
			say(serverLevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
			return 0;
		} else {
			GameTestRunner.clearMarkers(serverLevel);
			runTest(serverLevel, blockPos2, null, bl);
			return 1;
		}
	}

	private static int resetNearbyTest(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos2 = StructureUtils.findNearestStructureBlock(blockPos, 15, serverLevel);
		if (blockPos2 == null) {
			say(serverLevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
			return 0;
		} else {
			StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos2);
			structureBlockEntity.placeStructure(serverLevel);
			String string = structureBlockEntity.getMetaData();
			TestFunction testFunction = GameTestRegistry.getTestFunction(string);
			say(serverLevel, "Reset succeded for: " + testFunction, ChatFormatting.GREEN);
			return 1;
		}
	}

	private static int runAllNearbyTests(CommandSourceStack commandSourceStack, boolean bl) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockPos, 200, serverLevel);
		if (collection.isEmpty()) {
			say(serverLevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
			return 1;
		} else {
			GameTestRunner.clearMarkers(serverLevel);
			say(commandSourceStack, "Running " + collection.size() + " tests...");
			MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
			collection.forEach(blockPosx -> runTest(serverLevel, blockPosx, multipleTestTracker, bl));
			return 1;
		}
	}

	private static void runTest(ServerLevel serverLevel, BlockPos blockPos, @Nullable MultipleTestTracker multipleTestTracker, boolean bl) {
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		String string = structureBlockEntity.getMetaData();
		Optional<TestFunction> optional = GameTestRegistry.findTestFunction(string);
		if (optional.isEmpty()) {
			say(serverLevel, "Test function for test " + string + " could not be found", ChatFormatting.RED);
		} else {
			TestFunction testFunction = (TestFunction)optional.get();
			GameTestInfo gameTestInfo = new GameTestInfo(testFunction, structureBlockEntity.getRotation(), serverLevel);
			gameTestInfo.setRerunUntilFailed(bl);
			if (multipleTestTracker != null) {
				multipleTestTracker.addTestToTrack(gameTestInfo);
				gameTestInfo.addListener(new TestCommand.TestSummaryDisplayer(serverLevel, multipleTestTracker));
			}

			if (verifyStructureExists(serverLevel, gameTestInfo)) {
				runTestPreparation(testFunction, serverLevel);
				BoundingBox boundingBox = StructureUtils.getStructureBoundingBox(structureBlockEntity);
				BlockPos blockPos2 = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
				GameTestRunner.runTest(gameTestInfo, blockPos2, GameTestTicker.SINGLETON);
			}
		}
	}

	private static boolean verifyStructureExists(ServerLevel serverLevel, GameTestInfo gameTestInfo) {
		if (serverLevel.getStructureManager().get(new ResourceLocation(gameTestInfo.getStructureName())).isEmpty()) {
			say(serverLevel, "Test structure " + gameTestInfo.getStructureName() + " could not be found", ChatFormatting.RED);
			return false;
		} else {
			return true;
		}
	}

	static void showTestSummaryIfAllDone(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
		if (multipleTestTracker.isDone()) {
			say(serverLevel, "GameTest done! " + multipleTestTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
			if (multipleTestTracker.hasFailedRequired()) {
				say(serverLevel, multipleTestTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
			} else {
				say(serverLevel, "All required tests passed :)", ChatFormatting.GREEN);
			}

			if (multipleTestTracker.hasFailedOptional()) {
				say(serverLevel, multipleTestTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
			}
		}
	}

	private static int clearAllTests(CommandSourceStack commandSourceStack, int i) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		GameTestRunner.clearMarkers(serverLevel);
		BlockPos blockPos = BlockPos.containing(
			commandSourceStack.getPosition().x,
			(double)commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(commandSourceStack.getPosition())).getY(),
			commandSourceStack.getPosition().z
		);
		GameTestRunner.clearAllTests(serverLevel, blockPos, GameTestTicker.SINGLETON, Mth.clamp(i, 0, 1024));
		return 1;
	}

	private static int runTest(CommandSourceStack commandSourceStack, TestFunction testFunction, int i) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos = createTestPositionAround(commandSourceStack);
		GameTestRunner.clearMarkers(serverLevel);
		runTestPreparation(testFunction, serverLevel);
		Rotation rotation = StructureUtils.getRotationForRotationSteps(i);
		GameTestInfo gameTestInfo = new GameTestInfo(testFunction, rotation, serverLevel);
		if (!verifyStructureExists(serverLevel, gameTestInfo)) {
			return 0;
		} else {
			GameTestRunner.runTest(gameTestInfo, blockPos, GameTestTicker.SINGLETON);
			return 1;
		}
	}

	private static BlockPos createTestPositionAround(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		int i = commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
		return new BlockPos(blockPos.getX(), i + 1, blockPos.getZ() + 3);
	}

	private static void runTestPreparation(TestFunction testFunction, ServerLevel serverLevel) {
		Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(testFunction.getBatchName());
		if (consumer != null) {
			consumer.accept(serverLevel);
		}
	}

	private static int runAllTests(CommandSourceStack commandSourceStack, int i, int j) {
		GameTestRunner.clearMarkers(commandSourceStack.getLevel());
		Collection<TestFunction> collection = GameTestRegistry.getAllTestFunctions();
		say(commandSourceStack, "Running all " + collection.size() + " tests...");
		GameTestRegistry.forgetFailedTests();
		runTests(commandSourceStack, collection, i, j);
		return 1;
	}

	private static int runAllTestsInClass(CommandSourceStack commandSourceStack, String string, int i, int j) {
		Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(string);
		GameTestRunner.clearMarkers(commandSourceStack.getLevel());
		say(commandSourceStack, "Running " + collection.size() + " tests from " + string + "...");
		GameTestRegistry.forgetFailedTests();
		runTests(commandSourceStack, collection, i, j);
		return 1;
	}

	private static int runLastFailedTests(CommandSourceStack commandSourceStack, boolean bl, int i, int j) {
		Collection<TestFunction> collection;
		if (bl) {
			collection = (Collection<TestFunction>)GameTestRegistry.getLastFailedTests().stream().filter(TestFunction::isRequired).collect(Collectors.toList());
		} else {
			collection = GameTestRegistry.getLastFailedTests();
		}

		if (collection.isEmpty()) {
			say(commandSourceStack, "No failed tests to rerun");
			return 0;
		} else {
			GameTestRunner.clearMarkers(commandSourceStack.getLevel());
			say(commandSourceStack, "Rerunning " + collection.size() + " failed tests (" + (bl ? "only required tests" : "including optional tests") + ")");
			runTests(commandSourceStack, collection, i, j);
			return 1;
		}
	}

	private static void runTests(CommandSourceStack commandSourceStack, Collection<TestFunction> collection, int i, int j) {
		BlockPos blockPos = createTestPositionAround(commandSourceStack);
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Rotation rotation = StructureUtils.getRotationForRotationSteps(i);
		Collection<GameTestInfo> collection2 = GameTestRunner.runTests(collection, blockPos, rotation, serverLevel, GameTestTicker.SINGLETON, j);
		MultipleTestTracker multipleTestTracker = new MultipleTestTracker(collection2);
		multipleTestTracker.addListener(new TestCommand.TestSummaryDisplayer(serverLevel, multipleTestTracker));
		multipleTestTracker.addFailureListener(gameTestInfo -> GameTestRegistry.rememberFailedTest(gameTestInfo.getTestFunction()));
	}

	private static void say(CommandSourceStack commandSourceStack, String string) {
		commandSourceStack.sendSuccess(() -> Component.literal(string), false);
	}

	private static int exportNearestTestStructure(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos2 = StructureUtils.findNearestStructureBlock(blockPos, 15, serverLevel);
		if (blockPos2 == null) {
			say(serverLevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
			return 0;
		} else {
			StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos2);
			return saveAndExportTestStructure(commandSourceStack, structureBlockEntity);
		}
	}

	private static int exportAllNearbyTests(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockPos, 200, serverLevel);
		if (collection.isEmpty()) {
			say(serverLevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
			return 1;
		} else {
			boolean bl = true;

			for (BlockPos blockPos2 : collection) {
				StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos2);
				if (saveAndExportTestStructure(commandSourceStack, structureBlockEntity) != 0) {
					bl = false;
				}
			}

			return bl ? 0 : 1;
		}
	}

	private static int saveAndExportTestStructure(CommandSourceStack commandSourceStack, StructureBlockEntity structureBlockEntity) {
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

	private static int importTestStructure(CommandSourceStack commandSourceStack, String string) {
		Path path = Paths.get(StructureUtils.testStructuresDir, string + ".snbt");
		ResourceLocation resourceLocation = new ResourceLocation(string);
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

			say(commandSourceStack, "Imported to " + path2.toAbsolutePath());
			return 0;
		} catch (CommandSyntaxException | IOException var12) {
			LOGGER.error("Failed to load structure {}", string, var12);
			return 1;
		}
	}

	private static void say(ServerLevel serverLevel, String string, ChatFormatting chatFormatting) {
		serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
	}

	static class TestSummaryDisplayer implements GameTestListener {
		private final ServerLevel level;
		private final MultipleTestTracker tracker;

		public TestSummaryDisplayer(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
			this.level = serverLevel;
			this.tracker = multipleTestTracker;
		}

		@Override
		public void testStructureLoaded(GameTestInfo gameTestInfo) {
		}

		@Override
		public void testPassed(GameTestInfo gameTestInfo) {
			TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
		}

		@Override
		public void testFailed(GameTestInfo gameTestInfo) {
			TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
		}
	}
}
