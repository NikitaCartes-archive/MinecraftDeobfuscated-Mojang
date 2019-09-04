package net.minecraft.gametest.framework;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class GameTestAssertPosException extends GameTestAssertException {
	private final BlockPos absolutePos;
	private final BlockPos relativePos;

	public String getMessage() {
		String string = ""
			+ this.absolutePos.getX()
			+ ","
			+ this.absolutePos.getY()
			+ ","
			+ this.absolutePos.getZ()
			+ " (relative: "
			+ this.relativePos.getX()
			+ ","
			+ this.relativePos.getY()
			+ ","
			+ this.relativePos.getZ()
			+ ")";
		return super.getMessage() + " at " + string;
	}

	@Nullable
	public String getMessageToShowAtBlock() {
		return super.getMessage() + " here";
	}

	@Nullable
	public BlockPos getAbsolutePos() {
		return this.absolutePos;
	}
}
