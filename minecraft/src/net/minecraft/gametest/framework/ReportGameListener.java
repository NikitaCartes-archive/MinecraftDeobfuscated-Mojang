package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
	private int attempts = 0;
	private int successes = 0;

	public ReportGameListener() {
	}

	@Override
	public void testStructureLoaded(GameTestInfo gameTestInfo) {
		spawnBeacon(gameTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
		this.attempts++;
	}

	private void handleRetry(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner, boolean bl) {
		RetryOptions retryOptions = gameTestInfo.retryOptions();
		String string = String.format("[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
		if (!retryOptions.unlimitedTries()) {
			string = string + String.format(", Left: %4d", retryOptions.numberOfTries() - this.attempts);
		}

		string = string + "]";
		String string2 = gameTestInfo.getTestName() + " " + (bl ? "passed" : "failed") + "! " + gameTestInfo.getRunTime() + "ms";
		String string3 = String.format("%-53s%s", string, string2);
		if (bl) {
			reportPassed(gameTestInfo, string3);
		} else {
			say(gameTestInfo.getLevel(), ChatFormatting.RED, string3);
		}

		if (retryOptions.hasTriesLeft(this.attempts, this.successes)) {
			gameTestRunner.rerunTest(gameTestInfo);
		}
	}

	@Override
	public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
		this.successes++;
		if (gameTestInfo.retryOptions().hasRetries()) {
			this.handleRetry(gameTestInfo, gameTestRunner, true);
		} else if (!gameTestInfo.isFlaky()) {
			reportPassed(gameTestInfo, gameTestInfo.getTestName() + " passed! (" + gameTestInfo.getRunTime() + "ms)");
		} else {
			if (this.successes >= gameTestInfo.requiredSuccesses()) {
				reportPassed(gameTestInfo, gameTestInfo + " passed " + this.successes + " times of " + this.attempts + " attempts.");
			} else {
				say(gameTestInfo.getLevel(), ChatFormatting.GREEN, "Flaky test " + gameTestInfo + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
				gameTestRunner.rerunTest(gameTestInfo);
			}
		}
	}

	@Override
	public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
		if (!gameTestInfo.isFlaky()) {
			reportFailure(gameTestInfo, gameTestInfo.getError());
			if (gameTestInfo.retryOptions().hasRetries()) {
				this.handleRetry(gameTestInfo, gameTestRunner, false);
			}
		} else {
			TestFunction testFunction = gameTestInfo.getTestFunction();
			String string = "Flaky test " + gameTestInfo + " failed, attempt: " + this.attempts + "/" + testFunction.maxAttempts();
			if (testFunction.requiredSuccesses() > 1) {
				string = string + ", successes: " + this.successes + " (" + testFunction.requiredSuccesses() + " required)";
			}

			say(gameTestInfo.getLevel(), ChatFormatting.YELLOW, string);
			if (gameTestInfo.maxAttempts() - this.attempts + this.successes >= gameTestInfo.requiredSuccesses()) {
				gameTestRunner.rerunTest(gameTestInfo);
			} else {
				reportFailure(gameTestInfo, new ExhaustedAttemptsException(this.attempts, this.successes, gameTestInfo));
			}
		}
	}

	@Override
	public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
		gameTestInfo2.addListener(this);
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

	protected static void spawnBeacon(GameTestInfo gameTestInfo, Block block) {
		ServerLevel serverLevel = gameTestInfo.getLevel();
		BlockPos blockPos = gameTestInfo.getStructureBlockPos();
		BlockPos blockPos2 = new BlockPos(-1, -2, -1);
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
		BlockPos blockPos2 = new BlockPos(-1, 0, -1);
		BlockPos blockPos3 = StructureTemplate.transform(blockPos.offset(blockPos2), Mirror.NONE, gameTestInfo.getRotation(), blockPos);
		serverLevel.setBlockAndUpdate(blockPos3, Blocks.LECTERN.defaultBlockState().rotate(gameTestInfo.getRotation()));
		BlockState blockState = serverLevel.getBlockState(blockPos3);
		ItemStack itemStack = createBook(gameTestInfo.getTestName(), gameTestInfo.isRequired(), string);
		LecternBlock.tryPlaceBook(null, serverLevel, blockPos3, blockState, itemStack);
	}

	private static ItemStack createBook(String string, boolean bl, String string2) {
		StringBuffer stringBuffer = new StringBuffer();
		Arrays.stream(string.split("\\.")).forEach(stringx -> stringBuffer.append(stringx).append('\n'));
		if (!bl) {
			stringBuffer.append("(optional)\n");
		}

		stringBuffer.append("-------------------\n");
		ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
		itemStack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(Filterable.passThrough(stringBuffer + string2))));
		return itemStack;
	}

	protected static void say(ServerLevel serverLevel, ChatFormatting chatFormatting, String string) {
		serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal(string).withStyle(chatFormatting)));
	}

	private static void showRedBox(ServerLevel serverLevel, BlockPos blockPos, String string) {
		DebugPackets.sendGameTestAddMarker(serverLevel, blockPos, string, -2130771968, Integer.MAX_VALUE);
	}
}
