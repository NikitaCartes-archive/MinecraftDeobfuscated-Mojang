package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class BlockStatePredictionHandler implements AutoCloseable {
	private final Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap<>();
	private int currentSequenceNr;
	private boolean isPredicting;

	public void retainKnownServerState(BlockPos blockPos, BlockState blockState, LocalPlayer localPlayer) {
		this.serverVerifiedStates
			.compute(
				blockPos.asLong(),
				(long_, serverVerifiedState) -> serverVerifiedState != null
						? serverVerifiedState.setSequence(this.currentSequenceNr)
						: new BlockStatePredictionHandler.ServerVerifiedState(this.currentSequenceNr, blockState, localPlayer.position())
			);
	}

	public boolean updateKnownServerState(BlockPos blockPos, BlockState blockState) {
		BlockStatePredictionHandler.ServerVerifiedState serverVerifiedState = this.serverVerifiedStates.get(blockPos.asLong());
		if (serverVerifiedState == null) {
			return false;
		} else {
			serverVerifiedState.setBlockState(blockState);
			return true;
		}
	}

	public void endPredictionsUpTo(int i, ClientLevel clientLevel) {
		ObjectIterator<Entry<BlockStatePredictionHandler.ServerVerifiedState>> objectIterator = this.serverVerifiedStates.long2ObjectEntrySet().iterator();

		while (objectIterator.hasNext()) {
			Entry<BlockStatePredictionHandler.ServerVerifiedState> entry = (Entry<BlockStatePredictionHandler.ServerVerifiedState>)objectIterator.next();
			BlockStatePredictionHandler.ServerVerifiedState serverVerifiedState = (BlockStatePredictionHandler.ServerVerifiedState)entry.getValue();
			if (serverVerifiedState.sequence <= i) {
				BlockPos blockPos = BlockPos.of(entry.getLongKey());
				objectIterator.remove();
				clientLevel.syncBlockState(blockPos, serverVerifiedState.blockState, serverVerifiedState.playerPos);
			}
		}
	}

	public BlockStatePredictionHandler startPredicting() {
		this.currentSequenceNr++;
		this.isPredicting = true;
		return this;
	}

	public void close() {
		this.isPredicting = false;
	}

	public int currentSequence() {
		return this.currentSequenceNr;
	}

	public boolean isPredicting() {
		return this.isPredicting;
	}

	@Environment(EnvType.CLIENT)
	static class ServerVerifiedState {
		final Vec3 playerPos;
		int sequence;
		BlockState blockState;

		ServerVerifiedState(int i, BlockState blockState, Vec3 vec3) {
			this.sequence = i;
			this.blockState = blockState;
			this.playerPos = vec3;
		}

		BlockStatePredictionHandler.ServerVerifiedState setSequence(int i) {
			this.sequence = i;
			return this;
		}

		void setBlockState(BlockState blockState) {
			this.blockState = blockState;
		}
	}
}
