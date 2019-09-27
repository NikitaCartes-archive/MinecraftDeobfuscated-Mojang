/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchRunner;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.gametest.framework.TestReporter;
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

public class GameTestRunner {
    public static TestReporter TEST_REPORTER = new LogTestReporter();

    public static void runTest(GameTestInfo gameTestInfo, GameTestTicker gameTestTicker) {
        gameTestTicker.add(gameTestInfo);
        gameTestInfo.addListener(new GameTestListener(){

            @Override
            public void testStructureLoaded(GameTestInfo gameTestInfo) {
                GameTestRunner.spawnBeacon(gameTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
            }

            @Override
            public void testPassed(GameTestInfo gameTestInfo) {
                GameTestRunner.spawnBeacon(gameTestInfo, Blocks.LIME_STAINED_GLASS);
                GameTestRunner.visualizePassedTest(gameTestInfo);
            }

            @Override
            public void testFailed(GameTestInfo gameTestInfo) {
                GameTestRunner.spawnBeacon(gameTestInfo, gameTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
                GameTestRunner.spawnLectern(gameTestInfo, Util.describeError(gameTestInfo.getError()));
                GameTestRunner.visualizeFailedTest(gameTestInfo);
            }
        });
        gameTestInfo.spawnStructureAndRunTest(2);
    }

    public static Collection<GameTestInfo> runTestBatches(Collection<GameTestBatch> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker) {
        GameTestBatchRunner gameTestBatchRunner = new GameTestBatchRunner(collection, blockPos, serverLevel, gameTestTicker);
        gameTestBatchRunner.start();
        return gameTestBatchRunner.getTestInfos();
    }

    public static Collection<GameTestInfo> runTests(Collection<TestFunction> collection, BlockPos blockPos, ServerLevel serverLevel, GameTestTicker gameTestTicker) {
        return GameTestRunner.runTestBatches(GameTestRunner.groupTestsIntoBatches(collection), blockPos, serverLevel, gameTestTicker);
    }

    public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> collection) {
        HashMap map = Maps.newHashMap();
        collection.forEach(testFunction -> {
            String string2 = testFunction.getBatchName();
            Collection collection = map.computeIfAbsent(string2, string -> Lists.newArrayList());
            collection.add(testFunction);
        });
        return map.keySet().stream().map(string -> {
            Collection collection = (Collection)map.get(string);
            Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
            return new GameTestBatch((String)string, collection, consumer);
        }).collect(Collectors.toList());
    }

    private static void visualizeFailedTest(GameTestInfo gameTestInfo) {
        Throwable throwable = gameTestInfo.getError();
        String string = gameTestInfo.getTestName() + " failed! " + Util.describeError(throwable);
        GameTestRunner.say(gameTestInfo.getLevel(), ChatFormatting.RED, string);
        if (throwable instanceof GameTestAssertPosException) {
            GameTestAssertPosException gameTestAssertPosException = (GameTestAssertPosException)throwable;
            GameTestRunner.showRedBox(gameTestInfo.getLevel(), gameTestAssertPosException.getAbsolutePos(), gameTestAssertPosException.getMessageToShowAtBlock());
        }
        TEST_REPORTER.onTestFailed(gameTestInfo);
    }

    private static void visualizePassedTest(GameTestInfo gameTestInfo) {
        GameTestRunner.say(gameTestInfo.getLevel(), ChatFormatting.GREEN, gameTestInfo.getTestName() + " passed!");
        TEST_REPORTER.onTestSuccess(gameTestInfo);
    }

    private static void spawnBeacon(GameTestInfo gameTestInfo, Block block) {
        ServerLevel serverLevel = gameTestInfo.getLevel();
        BlockPos blockPos = gameTestInfo.getTestPos();
        BlockPos blockPos2 = blockPos.offset(-1, -1, -1);
        serverLevel.setBlockAndUpdate(blockPos2, Blocks.BEACON.defaultBlockState());
        BlockPos blockPos3 = blockPos2.offset(0, 1, 0);
        serverLevel.setBlockAndUpdate(blockPos3, block.defaultBlockState());
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
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
        ItemStack itemStack = GameTestRunner.createBook(gameTestInfo.getTestName(), gameTestInfo.isRequired(), string);
        LecternBlock.tryPlaceBook(serverLevel, blockPos2, blockState, itemStack);
    }

    private static ItemStack createBook(String string2, boolean bl, String string22) {
        ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
        ListTag listTag = new ListTag();
        StringBuffer stringBuffer = new StringBuffer();
        Arrays.stream(string2.split("\\.")).forEach(string -> stringBuffer.append((String)string).append('\n'));
        if (!bl) {
            stringBuffer.append("(optional)\n");
        }
        stringBuffer.append("-------------------\n");
        listTag.add(StringTag.valueOf(stringBuffer.toString() + string22));
        itemStack.addTagElement("pages", listTag);
        return itemStack;
    }

    private static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendMessage(new TextComponent(string).withStyle(chatFormatting)));
    }

    public static void clearMarkers(ServerLevel serverLevel) {
        DebugPackets.sendGameTestClearPacket(serverLevel);
    }

    private static void showRedBox(ServerLevel serverLevel, BlockPos blockPos, String string) {
        DebugPackets.sendGameTestAddMarker(serverLevel, blockPos, string, -2130771968, Integer.MAX_VALUE);
    }

    public static void clearAllTests(ServerLevel serverLevel, BlockPos blockPos2, GameTestTicker gameTestTicker, int i) {
        gameTestTicker.clear();
        BlockPos blockPos22 = blockPos2.offset(-i, 0, -i);
        BlockPos blockPos3 = blockPos2.offset(i, 0, i);
        BlockPos.betweenClosedStream(blockPos22, blockPos3).filter(blockPos -> serverLevel.getBlockState((BlockPos)blockPos).getBlock() == Blocks.STRUCTURE_BLOCK).forEach(blockPos -> {
            StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity((BlockPos)blockPos);
            StructureUtils.clearSpaceForStructure(structureBlockEntity.getBlockPos(), structureBlockEntity.getStructureSize(), 2, serverLevel);
        });
    }
}

