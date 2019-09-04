/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestClassNameArgument;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.gametest.framework.TestFunctionArgument;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
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
import org.jetbrains.annotations.Nullable;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("test").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("runthis").executes(commandContext -> TestCommand.runNearbyTest((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("runthese").executes(commandContext -> TestCommand.runAllNearbyTests((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("run").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes(commandContext -> TestCommand.runTest((CommandSourceStack)commandContext.getSource(), TestFunctionArgument.getTestFunction(commandContext, "testName")))))).then(((LiteralArgumentBuilder)Commands.literal("runall").executes(commandContext -> TestCommand.runAllTests((CommandSourceStack)commandContext.getSource()))).then(Commands.argument("testClassName", TestClassNameArgument.testClassName()).executes(commandContext -> TestCommand.runAllTestsInClass((CommandSourceStack)commandContext.getSource(), TestClassNameArgument.getTestClassName(commandContext, "testClassName")))))).then(Commands.literal("export").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("testName", StringArgumentType.word()).executes(commandContext -> TestCommand.exportTestStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))))).then(Commands.literal("import").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("testName", StringArgumentType.word()).executes(commandContext -> TestCommand.importTestStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "testName")))))).then(Commands.literal("pos").executes(commandContext -> TestCommand.showPos((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("create").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("testName", StringArgumentType.word()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "testName"), 5, 5, 5))).then(((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "testName"), IntegerArgumentType.getInteger(commandContext, "width"), IntegerArgumentType.getInteger(commandContext, "width"), IntegerArgumentType.getInteger(commandContext, "width")))).then(Commands.argument("height", IntegerArgumentType.integer()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("depth", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "testName"), IntegerArgumentType.getInteger(commandContext, "width"), IntegerArgumentType.getInteger(commandContext, "height"), IntegerArgumentType.getInteger(commandContext, "depth"))))))))).then(((LiteralArgumentBuilder)Commands.literal("clearall").executes(commandContext -> TestCommand.clearAllTests((CommandSourceStack)commandContext.getSource(), 200))).then(Commands.argument("radius", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.clearAllTests((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "radius"))))));
    }

    private static int createNewStructure(CommandSourceStack commandSourceStack, String string, int i, int j, int k) {
        if (i > 32 || j > 32 || k > 32) {
            throw new IllegalArgumentException("The structure must be less than 32 blocks big in each axis");
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3);
        StructureUtils.createNewEmptyStructureBlock(string.toLowerCase(), blockPos2, new BlockPos(i, j, k), 2, serverLevel);
        for (int l = 0; l < i; ++l) {
            for (int m = 0; m < k; ++m) {
                BlockPos blockPos3 = new BlockPos(blockPos2.getX() + l, blockPos2.getY() + 1, blockPos2.getZ() + m);
                Block block = Blocks.POLISHED_ANDESITE;
                BlockInput blockInput = new BlockInput(block.defaultBlockState(), Collections.EMPTY_SET, null);
                blockInput.place(serverLevel, blockPos3, 2);
            }
        }
        StructureUtils.addCommandBlockAndButtonToStartTest(blockPos2.offset(1, 0, -1), serverLevel);
        return 0;
    }

    private static int showPos(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        ServerLevel serverLevel;
        BlockHitResult blockHitResult = (BlockHitResult)commandSourceStack.getPlayerOrException().pick(10.0, 1.0f, false);
        BlockPos blockPos = blockHitResult.getBlockPos();
        Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockPos, 15, serverLevel = commandSourceStack.getLevel());
        if (!optional.isPresent()) {
            optional = StructureUtils.findStructureBlockContainingPos(blockPos, 200, serverLevel);
        }
        if (!optional.isPresent()) {
            commandSourceStack.sendFailure(new TextComponent("Can't find a structure block that contains the targeted pos " + blockPos));
            return 0;
        }
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(optional.get());
        BlockPos blockPos2 = blockPos.subtract(optional.get());
        String string = blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ();
        String string2 = structureBlockEntity.getStructurePath();
        TestCommand.say(commandSourceStack, "Position relative to " + string2 + ":");
        TestCommand.say(commandSourceStack, string);
        DebugPackets.sendGameTestAddMarker(serverLevel, new BlockPos(blockPos), string, -2147418368, 10000);
        return 1;
    }

    private static int runNearbyTest(CommandSourceStack commandSourceStack) {
        ServerLevel serverLevel;
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = StructureUtils.findNearestStructureBlock(blockPos, 15, serverLevel = commandSourceStack.getLevel());
        if (blockPos2 == null) {
            TestCommand.say(serverLevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
            return 0;
        }
        GameTestRunner.clearMarkers(serverLevel);
        TestCommand.runTest(serverLevel, blockPos2, null);
        return 1;
    }

    private static int runAllNearbyTests(CommandSourceStack commandSourceStack) {
        ServerLevel serverLevel;
        BlockPos blockPos2 = new BlockPos(commandSourceStack.getPosition());
        Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockPos2, 200, serverLevel = commandSourceStack.getLevel());
        if (collection.isEmpty()) {
            TestCommand.say(serverLevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
            return 1;
        }
        GameTestRunner.clearMarkers(serverLevel);
        TestCommand.say(commandSourceStack, "Running " + collection.size() + " tests...");
        MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
        collection.forEach(blockPos -> TestCommand.runTest(serverLevel, blockPos, multipleTestTracker));
        return 1;
    }

    private static void runTest(ServerLevel serverLevel, BlockPos blockPos, @Nullable MultipleTestTracker multipleTestTracker) {
        StructureBlockEntity structureBlockEntity = (StructureBlockEntity)serverLevel.getBlockEntity(blockPos);
        String string = structureBlockEntity.getStructurePath();
        TestFunction testFunction = GameTestRegistry.getTestFunction(string);
        GameTestInfo gameTestInfo = new GameTestInfo(testFunction, blockPos, serverLevel);
        if (multipleTestTracker != null) {
            multipleTestTracker.add(gameTestInfo);
            gameTestInfo.addListener(new TestSummaryDisplayer(serverLevel, multipleTestTracker));
        }
        GameTestRunner.runTest(gameTestInfo, GameTestTicker.singleton);
    }

    private static void showTestSummaryIfAllDone(ServerLevel serverLevel, MultipleTestTracker multipleTestTracker) {
        if (multipleTestTracker.isDone()) {
            TestCommand.say(serverLevel, "GameTest done! " + multipleTestTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
            if (multipleTestTracker.hasFailedRequired()) {
                TestCommand.say(serverLevel, "" + multipleTestTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
            } else {
                TestCommand.say(serverLevel, "All required tests passed :)", ChatFormatting.GREEN);
            }
            if (multipleTestTracker.hasFailedOptional()) {
                TestCommand.say(serverLevel, "" + multipleTestTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
            }
        }
    }

    private static int clearAllTests(CommandSourceStack commandSourceStack, int i) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        GameTestRunner.clearMarkers(serverLevel);
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition().x, (double)commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(commandSourceStack.getPosition())).getY(), commandSourceStack.getPosition().z);
        GameTestRunner.clearAllTests(serverLevel, blockPos, GameTestTicker.singleton, Mth.clamp(i, 0, 1024));
        return 1;
    }

    private static int runTest(CommandSourceStack commandSourceStack, TestFunction testFunction) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3);
        GameTestRunner.clearMarkers(serverLevel);
        GameTestInfo gameTestInfo = new GameTestInfo(testFunction, blockPos2, serverLevel);
        GameTestRunner.runTest(gameTestInfo, GameTestTicker.singleton);
        return 1;
    }

    private static int runAllTests(CommandSourceStack commandSourceStack) {
        GameTestRunner.clearMarkers(commandSourceStack.getLevel());
        TestCommand.runTests(commandSourceStack, GameTestRegistry.getAllTestFunctions());
        return 1;
    }

    private static int runAllTestsInClass(CommandSourceStack commandSourceStack, String string) {
        Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(string);
        GameTestRunner.clearMarkers(commandSourceStack.getLevel());
        TestCommand.runTests(commandSourceStack, collection);
        return 1;
    }

    private static void runTests(CommandSourceStack commandSourceStack, Collection<TestFunction> collection) {
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY(), blockPos.getZ() + 3);
        ServerLevel serverLevel = commandSourceStack.getLevel();
        TestCommand.say(commandSourceStack, "Running " + collection.size() + " tests...");
        Collection<GameTestInfo> collection2 = GameTestRunner.runTests(collection, blockPos2, serverLevel, GameTestTicker.singleton);
        MultipleTestTracker multipleTestTracker = new MultipleTestTracker(collection2);
        multipleTestTracker.setListener(new TestSummaryDisplayer(serverLevel, multipleTestTracker));
    }

    private static void say(CommandSourceStack commandSourceStack, String string) {
        commandSourceStack.sendSuccess(new TextComponent(string), false);
    }

    private static int exportTestStructure(CommandSourceStack commandSourceStack, String string) {
        Path path = Paths.get(StructureUtils.testStructuresDir, new String[0]);
        ResourceLocation resourceLocation = new ResourceLocation("minecraft", string);
        Path path2 = commandSourceStack.getLevel().getStructureManager().createPathToStructure(resourceLocation, ".nbt");
        Path path3 = NbtToSnbt.convertStructure(path2, string, path);
        if (path3 == null) {
            TestCommand.say(commandSourceStack, "Failed to export " + path2);
            return 1;
        }
        try {
            Files.createDirectories(path3.getParent(), new FileAttribute[0]);
        } catch (IOException iOException) {
            TestCommand.say(commandSourceStack, "Could not create folder " + path3.getParent());
            iOException.printStackTrace();
            return 1;
        }
        TestCommand.say(commandSourceStack, "Exported to " + path3.toAbsolutePath());
        return 0;
    }

    private static int importTestStructure(CommandSourceStack commandSourceStack, String string) {
        Path path = Paths.get(StructureUtils.testStructuresDir, string + ".snbt");
        ResourceLocation resourceLocation = new ResourceLocation("minecraft", string);
        Path path2 = commandSourceStack.getLevel().getStructureManager().createPathToStructure(resourceLocation, ".nbt");
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string2 = IOUtils.toString(bufferedReader);
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            OutputStream outputStream = Files.newOutputStream(path2, new OpenOption[0]);
            NbtIo.writeCompressed(TagParser.parseTag(string2), outputStream);
            TestCommand.say(commandSourceStack, "Imported to " + path2.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException exception) {
            System.err.println("Failed to load structure " + string);
            exception.printStackTrace();
            return 1;
        }
    }

    private static void say(ServerLevel serverLevel, String string, ChatFormatting chatFormatting) {
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendMessage(new TextComponent((Object)((Object)chatFormatting) + string)));
    }

    static class TestSummaryDisplayer
    implements GameTestListener {
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

