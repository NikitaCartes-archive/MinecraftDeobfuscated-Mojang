package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;

public class TestCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("test")
				.then(Commands.literal("runthis").executes(commandContext -> runNearbyTest(commandContext.getSource())))
				.then(Commands.literal("runthese").executes(commandContext -> runAllNearbyTests(commandContext.getSource())))
				.then(
					Commands.literal("run")
						.then(
							Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
								.executes(commandContext -> runTest(commandContext.getSource(), TestFunctionArgument.getTestFunction(commandContext, "testName")))
						)
				)
				.then(
					Commands.literal("runall")
						.executes(commandContext -> runAllTests(commandContext.getSource()))
						.then(
							Commands.argument("testClassName", TestClassNameArgument.testClassName())
								.executes(commandContext -> runAllTestsInClass(commandContext.getSource(), TestClassNameArgument.getTestClassName(commandContext, "testClassName")))
						)
				)
				.then(
					Commands.literal("export")
						.then(
							Commands.argument("testName", StringArgumentType.word())
								.executes(commandContext -> exportTestStructure(commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))
						)
				)
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
		if (i <= 32 && j <= 32 && k <= 32) {
			ServerLevel serverLevel = commandSourceStack.getLevel();
			BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
			BlockPos blockPos2 = new BlockPos(
				blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3
			);
			StructureUtils.createNewEmptyStructureBlock(string.toLowerCase(), blockPos2, new BlockPos(i, j, k), 2, serverLevel);

			for (int l = 0; l < i; l++) {
				for (int m = 0; m < k; m++) {
					BlockPos blockPos3 = new BlockPos(blockPos2.getX() + l, blockPos2.getY() + 1, blockPos2.getZ() + m);
					Block block = Blocks.POLISHED_ANDESITE;
					BlockInput blockInput = new BlockInput(block.defaultBlockState(), Collections.EMPTY_SET, null);
					blockInput.place(serverLevel, blockPos3, 2);
				}
			}

			StructureUtils.addCommandBlockAndButtonToStartTest(blockPos2.offset(1, 0, -1), serverLevel);
			return 0;
		} else {
			throw new IllegalArgumentException("The structure must be less than 32 blocks big in each axis");
		}
	}

	private static int showPos(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		BlockHitResult blockHitResult = (BlockHitResult)commandSourceStack.getPlayerOrException().pick(10.0, 1.0F, false);
		BlockPos blockPos = blockHitResult.getBlockPos();
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockPos, 15, serverLevel);
		if (!optional.isPresent()) {
			optional = StructureUtils.findStructureBlockContainingPos(blockPos, 200, serverLevel);
		}

		if (!optional.isPresent()) {
			commandSourceStack.sendFailure(new TextComponent("Can't find a structure block that contains the targeted pos " + blockPos));
			return 0;
		} else {
			StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)optional.get());
			BlockPos blockPos2 = blockPos.subtract((Vec3i)optional.get());
			String string2 = blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ();
			String string3 = structureBlockEntity.getStructurePath();
			Component component = new TextComponent(string2)
				.setStyle(
					new Style()
						.setBold(true)
						.setColor(ChatFormatting.GREEN)
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to copy to clipboard")))
						.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + string + " = new BlockPos(" + string2 + ");"))
				);
			commandSourceStack.sendSuccess(new TextComponent("Position relative to " + string3 + ": ").append(component), false);
			DebugPackets.sendGameTestAddMarker(serverLevel, new BlockPos(blockPos), string2, -2147418368, 10000);
			return 1;
		}
	}

	private static int runNearbyTest(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos2 = StructureUtils.findNearestStructureBlock(blockPos, 15, serverLevel);
		if (blockPos2 == null) {
			say(serverLevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
			return 0;
		} else {
			GameTestRunner.clearMarkers(serverLevel);
			runTest(serverLevel, blockPos2, null);
			return 1;
		}
	}

	private static int runAllNearbyTests(CommandSourceStack commandSourceStack) {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockPos, 200, serverLevel);
		if (collection.isEmpty()) {
			say(serverLevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
			return 1;
		} else {
			GameTestRunner.clearMarkers(serverLevel);
			say(commandSourceStack, "Running " + collection.size() + " tests...");
			MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
			collection.forEach(blockPosx -> runTest(serverLevel, blockPosx, multipleTestTracker));
			return 1;
		}
	}

	private static void runTest(ServerLevel serverLevel, BlockPos blockPos, @Nullable MultipleTestTracker multipleTestTracker) {
		StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
		String string = structureBlockEntity.getStructurePath();
		TestFunction testFunction = GameTestRegistry.getTestFunction(string);
		GameTestInfo gameTestInfo = new GameTestInfo(testFunction, blockPos, serverLevel);
		if (multipleTestTracker != null) {
			multipleTestTracker.add(gameTestInfo);
			gameTestInfo.addListener(new TestCommand.TestSummaryDisplayer(serverLevel, multipleTestTracker));
		}

		runTestPreparation(testFunction, serverLevel);
		GameTestRunner.runTest(gameTestInfo, GameTestTicker.singleton);
	}

	private static void showTestSummaryIfAllDone(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
		if (multipleTestTracker.isDone()) {
			say(serverLevel, "GameTest done! " + multipleTestTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
			if (multipleTestTracker.hasFailedRequired()) {
				say(serverLevel, "" + multipleTestTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
			} else {
				say(serverLevel, "All required tests passed :)", ChatFormatting.GREEN);
			}

			if (multipleTestTracker.hasFailedOptional()) {
				say(serverLevel, "" + multipleTestTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
			}
		}
	}

	private static int clearAllTests(CommandSourceStack commandSourceStack, int i) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		GameTestRunner.clearMarkers(serverLevel);
		BlockPos blockPos = new BlockPos(
			commandSourceStack.getPosition().x,
			(double)commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(commandSourceStack.getPosition())).getY(),
			commandSourceStack.getPosition().z
		);
		GameTestRunner.clearAllTests(serverLevel, blockPos, GameTestTicker.singleton, Mth.clamp(i, 0, 1024));
		return 1;
	}

	private static int runTest(CommandSourceStack commandSourceStack, TestFunction testFunction) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		BlockPos blockPos2 = new BlockPos(
			blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3
		);
		GameTestRunner.clearMarkers(serverLevel);
		runTestPreparation(testFunction, serverLevel);
		GameTestInfo gameTestInfo = new GameTestInfo(testFunction, blockPos2, serverLevel);
		GameTestRunner.runTest(gameTestInfo, GameTestTicker.singleton);
		return 1;
	}

	private static void runTestPreparation(TestFunction testFunction, ServerLevel serverLevel) {
		Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(testFunction.getBatchName());
		if (consumer != null) {
			consumer.accept(serverLevel);
		}
	}

	private static int runAllTests(CommandSourceStack commandSourceStack) {
		GameTestRunner.clearMarkers(commandSourceStack.getLevel());
		runTests(commandSourceStack, GameTestRegistry.getAllTestFunctions());
		return 1;
	}

	private static int runAllTestsInClass(CommandSourceStack commandSourceStack, String string) {
		Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(string);
		GameTestRunner.clearMarkers(commandSourceStack.getLevel());
		runTests(commandSourceStack, collection);
		return 1;
	}

	private static void runTests(CommandSourceStack commandSourceStack, Collection<TestFunction> collection) {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		BlockPos blockPos2 = new BlockPos(
			blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3
		);
		ServerLevel serverLevel = commandSourceStack.getLevel();
		say(commandSourceStack, "Running " + collection.size() + " tests...");
		Collection<GameTestInfo> collection2 = GameTestRunner.runTests(collection, blockPos2, serverLevel, GameTestTicker.singleton);
		MultipleTestTracker multipleTestTracker = new MultipleTestTracker(collection2);
		multipleTestTracker.setListener(new TestCommand.TestSummaryDisplayer(serverLevel, multipleTestTracker));
	}

	private static void say(CommandSourceStack commandSourceStack, String string) {
		commandSourceStack.sendSuccess(new TextComponent(string), false);
	}

	private static int exportTestStructure(CommandSourceStack commandSourceStack, String string) {
		Path path = Paths.get(StructureUtils.testStructuresDir);
		ResourceLocation resourceLocation = new ResourceLocation("minecraft", string);
		Path path2 = commandSourceStack.getLevel().getStructureManager().createPathToStructure(resourceLocation, ".nbt");
		Path path3 = NbtToSnbt.convertStructure(path2, string, path);
		if (path3 == null) {
			say(commandSourceStack, "Failed to export " + path2);
			return 1;
		} else {
			try {
				Files.createDirectories(path3.getParent());
			} catch (IOException var7) {
				say(commandSourceStack, "Could not create folder " + path3.getParent());
				var7.printStackTrace();
				return 1;
			}

			say(commandSourceStack, "Exported to " + path3.toAbsolutePath());
			return 0;
		}
	}

	private static int importTestStructure(CommandSourceStack commandSourceStack, String string) {
		Path path = Paths.get(StructureUtils.testStructuresDir, string + ".snbt");
		ResourceLocation resourceLocation = new ResourceLocation("minecraft", string);
		Path path2 = commandSourceStack.getLevel().getStructureManager().createPathToStructure(resourceLocation, ".nbt");

		try {
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			String string2 = IOUtils.toString(bufferedReader);
			Files.createDirectories(path2.getParent());
			OutputStream outputStream = Files.newOutputStream(path2);
			NbtIo.writeCompressed(TagParser.parseTag(string2), outputStream);
			say(commandSourceStack, "Imported to " + path2.toAbsolutePath());
			return 0;
		} catch (CommandSyntaxException | IOException var8) {
			System.err.println("Failed to load structure " + string);
			var8.printStackTrace();
			return 1;
		}
	}

	private static void say(ServerLevel serverLevel, String string, ChatFormatting chatFormatting) {
		serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendMessage(new TextComponent(chatFormatting + string)));
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
		public void testFailed(GameTestInfo gameTestInfo) {
			TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
		}
	}
}
