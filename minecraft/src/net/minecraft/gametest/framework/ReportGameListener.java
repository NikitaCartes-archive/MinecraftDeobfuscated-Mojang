package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
	private final GameTestInfo originalTestInfo;
	private final GameTestTicker testTicker;
	private final BlockPos structurePos;
	int attempts;
	int successes;

	public ReportGameListener(GameTestInfo gameTestInfo, GameTestTicker gameTestTicker, BlockPos blockPos) {
		this.originalTestInfo = gameTestInfo;
		this.testTicker = gameTestTicker;
		this.structurePos = blockPos;
		this.attempts = 0;
		this.successes = 0;
	}

	@Override
	public void testStructureLoaded(GameTestInfo gameTestInfo) {
		spawnBeacon(this.originalTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
		this.attempts++;
	}

	@Override
	public void testPassed(GameTestInfo gameTestInfo) {
		this.successes++;
		if (!gameTestInfo.isFlaky()) {
			reportPassed(gameTestInfo, gameTestInfo.getTestName() + " passed!");
		} else {
			if (this.successes >= gameTestInfo.requiredSuccesses()) {
				reportPassed(gameTestInfo, gameTestInfo + " passed " + this.successes + " times of " + this.attempts + " attempts.");
			} else {
				say(
					this.originalTestInfo.getLevel(),
					ChatFormatting.GREEN,
					"Flaky test " + this.originalTestInfo + " succeeded, attempt: " + this.attempts + " successes: " + this.successes
				);
				this.rerunTest();
			}
		}
	}

	@Override
	public void testFailed(GameTestInfo gameTestInfo) {
		if (!gameTestInfo.isFlaky()) {
			reportFailure(gameTestInfo, gameTestInfo.getError());
		} else {
			TestFunction testFunction = this.originalTestInfo.getTestFunction();
			String string = "Flaky test " + this.originalTestInfo + " failed, attempt: " + this.attempts + "/" + testFunction.getMaxAttempts();
			if (testFunction.getRequiredSuccesses() > 1) {
				string = string + ", successes: " + this.successes + " (" + testFunction.getRequiredSuccesses() + " required)";
			}

			say(this.originalTestInfo.getLevel(), ChatFormatting.YELLOW, string);
			if (gameTestInfo.maxAttempts() - this.attempts + this.successes >= gameTestInfo.requiredSuccesses()) {
				this.rerunTest();
			} else {
				reportFailure(gameTestInfo, new ExhaustedAttemptsException(this.attempts, this.successes, gameTestInfo));
			}
		}
	}

	public static void reportPassed(GameTestInfo gameTestInfo, String string) {
		spawnBeacon(gameTestInfo, Blocks.LIME_STAINED_GLASS);
		visualizePassedTest(gameTestInfo, string);
	}

	private static void visualizePassedTest(GameTestInfo gameTestInfo, String string) {
		say(gameTestInfo.getLevel(), ChatFormatting.GREEN, string);
		GlobalTestReporter.onTestSuccess(gameTestInfo);
	}

	protected static void reportFailure(GameTestInfo gameTestInfo, Throwable throwable) {
		spawnBeacon(gameTestInfo, gameTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
		spawnLectern(gameTestInfo, Util.describeError(throwable));
		visualizeFailedTest(gameTestInfo, throwable);
	}

	protected static void visualizeFailedTest(GameTestInfo gameTestInfo, Throwable throwable) {
		String string = throwable.getMessage() + (throwable.getCause() == null ? "" : " cause: " + Util.describeError(throwable.getCause()));
		String string2 = (gameTestInfo.isRequired() ? "" : "(optional) ") + gameTestInfo.getTestName() + " failed! " + string;
		say(gameTestInfo.getLevel(), gameTestInfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, string2);
		Throwable throwable2 = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(throwable), throwable);
		if (throwable2 instanceof GameTestAssertPosException gameTestAssertPosException) {
			showRedBox(gameTestInfo.getLevel(), gameTestAssertPosException.getAbsolutePos(), gameTestAssertPosException.getMessageToShowAtBlock());
		}

		GlobalTestReporter.onTestFailed(gameTestInfo);
	}

	private void rerunTest() {
		this.originalTestInfo.clearStructure();
		GameTestInfo gameTestInfo = new GameTestInfo(this.originalTestInfo.getTestFunction(), this.originalTestInfo.getRotation(), this.originalTestInfo.getLevel());
		gameTestInfo.startExecution();
		this.testTicker.add(gameTestInfo);
		gameTestInfo.addListener(this);
		gameTestInfo.spawnStructure(this.structurePos, 2);
	}

	protected static void spawnBeacon(GameTestInfo gameTestInfo, Block block) {
		ServerLevel serverLevel = gameTestInfo.getLevel();
		BlockPos blockPos = gameTestInfo.getStructureBlockPos();
		BlockPos blockPos2 = new BlockPos(-1, -1, -1);
		BlockPos blockPos3 = StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, gameTestInfo.getRotation(), blockPos);
		serverLevel.setBlockAndUpdate(blockPos3, Blocks.BEACON.defaultBlockState().rotate(gameTestInfo.getRotation()));
		BlockPos blockPos4 = blockPos3.offset(0, 1, 0);
		serverLevel.setBlockAndUpdate(blockPos4, block.defaultBlockState());

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				BlockPos blockPos5 = blockPos3.offset(i, -1, j);
				serverLevel.setBlockAndUpdate(blockPos5, Blocks.IRON_BLOCK.defaultBlockState());
			}
		}
	}

	private static void spawnLectern(GameTestInfo gameTestInfo, String string) {
		ServerLevel serverLevel = gameTestInfo.getLevel();
		BlockPos blockPos = gameTestInfo.getStructureBlockPos();
		BlockPos blockPos2 = new BlockPos(-1, 1, -1);
		BlockPos blockPos3 = StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, gameTestInfo.getRotation(), blockPos);
		serverLevel.setBlockAndUpdate(blockPos3, Blocks.LECTERN.defaultBlockState().rotate(gameTestInfo.getRotation()));
		BlockState blockState = serverLevel.getBlockState(blockPos3);
		ItemStack itemStack = createBook(gameTestInfo.getTestName(), gameTestInfo.isRequired(), string);
		LecternBlock.tryPlaceBook(null, serverLevel, blockPos3, blockState, itemStack);
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
		listTag.add(StringTag.valueOf(stringBuffer + string2));
		itemStack.addTagElement("pages", listTag);
		return itemStack;
	}

	protected static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
		serverLevel.getPlayers(serverPlayer -> true)
			.forEach(serverPlayer -> serverPlayer.sendMessage(new TextComponent(string).withStyle(chatFormatting), Util.NIL_UUID));
	}

	private static void showRedBox(ServerLevel serverLevel, BlockPos blockPos, String string) {
		DebugPackets.sendGameTestAddMarker(serverLevel, blockPos, string, -2130771968, Integer.MAX_VALUE);
	}
}
