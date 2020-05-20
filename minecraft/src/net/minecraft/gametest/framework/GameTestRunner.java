package net.minecraft.gametest.framework;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GameTestRunner {
	public static TestReporter TEST_REPORTER = new LogTestReporter();

	public static void runTest(GameTestInfo gameTestInfo, GameTestTicker gameTestTicker) {
		gameTestInfo.startExecution();
		gameTestTicker.add(gameTestInfo);
		gameTestInfo.addListener(new GameTestListener() {
			@Override
			public void testStructureLoaded(GameTestInfo gameTestInfo) {
				GameTestRunner.spawnBeacon(gameTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
			}

			@Override
			public void testFailed(GameTestInfo gameTestInfo) {
				GameTestRunner.spawnBeacon(gameTestInfo, gameTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
				GameTestRunner.spawnLectern(gameTestInfo, Util.describeError(gameTestInfo.getError()));
				GameTestRunner.visualizeFailedTest(gameTestInfo);
			}
		});
		gameTestInfo.spawnStructure(2);
	}

	public static Collection<GameTestInfo> runTestBatches(
		Collection<GameTestBatch> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker
	) {
		GameTestBatchRunner gameTestBatchRunner = new GameTestBatchRunner(collection, blockPos, serverLevel, gameTestTicker);
		gameTestBatchRunner.start();
		return gameTestBatchRunner.getTestInfos();
	}

	public static Collection<GameTestInfo> runTests(Collection<TestFunction> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker) {
		return runTestBatches(groupTestsIntoBatches(collection), blockPos, serverLevel, gameTestTicker);
	}

	public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> collection) {
		Map<String, Collection<TestFunction>> map = Maps.<String, Collection<TestFunction>>newHashMap();
		collection.forEach(testFunction -> {
			String string = testFunction.getBatchName();
			Collection<TestFunction> collectionx = (Collection<TestFunction>)map.computeIfAbsent(string, stringx -> Lists.newArrayList());
			collectionx.add(testFunction);
		});
		return (Collection<GameTestBatch>)map.keySet()
			.stream()
			.flatMap(
				string -> {
					Collection<TestFunction> collectionx = (Collection<TestFunction>)map.get(string);
					Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
					AtomicInteger atomicInteger = new AtomicInteger();
					return Streams.stream(Iterables.partition(collectionx, 100))
						.map(list -> new GameTestBatch(string + ":" + atomicInteger.incrementAndGet(), collectionx, consumer));
				}
			)
			.collect(Collectors.toList());
	}

	private static void visualizeFailedTest(GameTestInfo gameTestInfo) {
		Throwable throwable = gameTestInfo.getError();
		String string = gameTestInfo.getTestName() + " failed! " + Util.describeError(throwable);
		say(gameTestInfo.getLevel(), ChatFormatting.RED, string);
		if (throwable instanceof GameTestAssertPosException) {
			GameTestAssertPosException gameTestAssertPosException = (GameTestAssertPosException)throwable;
			showRedBox(gameTestInfo.getLevel(), gameTestAssertPosException.getAbsolutePos(), gameTestAssertPosException.getMessageToShowAtBlock());
		}

		TEST_REPORTER.onTestFailed(gameTestInfo);
	}

	private static void spawnBeacon(GameTestInfo gameTestInfo, Block block) {
		ServerLevel serverLevel = gameTestInfo.getLevel();
		BlockPos blockPos = gameTestInfo.getTestPos();
		BlockPos blockPos2 = blockPos.offset(-1, -1, -1);
		serverLevel.setBlockAndUpdate(blockPos2, Blocks.BEACON.defaultBlockState());
		BlockPos blockPos3 = blockPos2.offset(0, 1, 0);
		serverLevel.setBlockAndUpdate(blockPos3, block.defaultBlockState());

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				BlockPos blockPos4 = blockPos2.offset(i, -1, j);
				serverLevel.setBlockAndUpdate(blockPos4, Blocks.IRON_BLOCK.defaultBlockState());
			}
		}
	}

	private static void spawnLectern(GameTestInfo gameTestInfo, String string) {
		ServerLevel serverLevel = gameTestInfo.getLevel();
		BlockPos blockPos = gameTestInfo.getTestPos();
		BlockPos blockPos2 = blockPos.offset(-1, 1, -1);
		serverLevel.setBlockAndUpdate(blockPos2, Blocks.LECTERN.defaultBlockState());
		BlockState blockState = serverLevel.getBlockState(blockPos2);
		ItemStack itemStack = createBook(gameTestInfo.getTestName(), gameTestInfo.isRequired(), string);
		LecternBlock.tryPlaceBook(serverLevel, blockPos2, blockState, itemStack);
	}

	private static ItemStack createBook(String string, boolean bl, String string2) {
		ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
		ListTag listTag = new ListTag();
		StringBuffer stringBuffer = new StringBuffer();
		Arrays.stream(string.split("\\.")).forEach(stringx -> stringBuffer.append(stringx).append('\n'));
		if (!bl) {
			stringBuffer.append("(optional)\n");
		}

		stringBuffer.append("-------------------\n");
		listTag.add(StringTag.valueOf(stringBuffer.toString() + string2));
		itemStack.addTagElement("pages", listTag);
		return itemStack;
	}

	private static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
		serverLevel.getPlayers(serverPlayer -> true)
			.forEach(serverPlayer -> serverPlayer.sendMessage(new TextComponent(string).withStyle(chatFormatting), Util.NIL_UUID));
	}

	public static void clearMarkers(ServerLevel serverLevel) {
		DebugPackets.sendGameTestClearPacket(serverLevel);
	}

	private static void showRedBox(ServerLevel serverLevel, BlockPos blockPos, String string) {
		DebugPackets.sendGameTestAddMarker(serverLevel, blockPos, string, -2130771968, Integer.MAX_VALUE);
	}

	public static void clearAllTests(ServerLevel serverLevel, BlockPos blockPos, GameTestTicker gameTestTicker, int i) {
		gameTestTicker.clear();
		BlockPos blockPos2 = blockPos.offset(-i, 0, -i);
		BlockPos blockPos3 = blockPos.offset(i, 0, i);
		BlockPos.betweenClosedStream(blockPos2, blockPos3)
			.filter(blockPosx -> serverLevel.getBlockState(blockPosx).is(Blocks.STRUCTURE_BLOCK))
			.forEach(blockPosx -> {
				StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPosx);
				BlockPos blockPos2x = structureBlockEntity.getBlockPos();
				BoundingBox boundingBox = StructureUtils.createStructureBoundingBox(blockPos2x, structureBlockEntity.getStructureSize(), 2);
				StructureUtils.clearSpaceForStructure(boundingBox, blockPos2x.getY(), serverLevel);
			});
	}
}
