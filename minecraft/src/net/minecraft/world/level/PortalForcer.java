package net.minecraft.world.level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.Vec3;

public class PortalForcer {
	private final ServerLevel level;
	private final Random random;

	public PortalForcer(ServerLevel serverLevel) {
		this.level = serverLevel;
		this.random = new Random(serverLevel.getSeed());
	}

	public boolean findAndMoveToPortal(Entity entity, float f) {
		Vec3 vec3 = entity.getPortalEntranceOffset();
		Direction direction = entity.getPortalEntranceForwards();
		BlockPattern.PortalInfo portalInfo = this.findPortal(new BlockPos(entity), entity.getDeltaMovement(), direction, vec3.x, vec3.y, entity instanceof Player);
		if (portalInfo == null) {
			return false;
		} else {
			Vec3 vec32 = portalInfo.pos;
			Vec3 vec33 = portalInfo.speed;
			entity.setDeltaMovement(vec33);
			entity.yRot = f + (float)portalInfo.angle;
			if (entity instanceof ServerPlayer) {
				((ServerPlayer)entity).connection.teleport(vec32.x, vec32.y, vec32.z, entity.yRot, entity.xRot);
				((ServerPlayer)entity).connection.resetPosition();
			} else {
				entity.moveTo(vec32.x, vec32.y, vec32.z, entity.yRot, entity.xRot);
			}

			return true;
		}
	}

	@Nullable
	public BlockPattern.PortalInfo findPortal(BlockPos blockPos, Vec3 vec3, Direction direction, double d, double e, boolean bl) {
		List<PoiRecord> list = (List<PoiRecord>)this.level
			.getPoiManager()
			.getInSquare(poiType -> poiType == PoiType.NETHER_PORTAL, blockPos, 128, PoiManager.Occupancy.ANY)
			.collect(Collectors.toList());
		Optional<PoiRecord> optional = list.stream()
			.min(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos)).thenComparingInt(poiRecord -> poiRecord.getPos().getY()));
		return (BlockPattern.PortalInfo)optional.map(poiRecord -> {
			BlockPos blockPosx = poiRecord.getPos();
			this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPosx), 3, blockPosx);
			BlockPattern.BlockPatternMatch blockPatternMatch = NetherPortalBlock.getPortalShape(this.level, blockPosx);
			return blockPatternMatch.getPortalOutput(direction, blockPosx, e, vec3, d);
		}).orElse(null);
	}

	public boolean createPortal(Entity entity) {
		int i = 16;
		double d = -1.0;
		int j = Mth.floor(entity.x);
		int k = Mth.floor(entity.y);
		int l = Mth.floor(entity.z);
		int m = j;
		int n = k;
		int o = l;
		int p = 0;
		int q = this.random.nextInt(4);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int r = j - 16; r <= j + 16; r++) {
			double e = (double)r + 0.5 - entity.x;

			for (int s = l - 16; s <= l + 16; s++) {
				double f = (double)s + 0.5 - entity.z;

				label279:
				for (int t = this.level.getHeight() - 1; t >= 0; t--) {
					if (this.level.isEmptyBlock(mutableBlockPos.set(r, t, s))) {
						while (t > 0 && this.level.isEmptyBlock(mutableBlockPos.set(r, t - 1, s))) {
							t--;
						}

						for (int u = q; u < q + 4; u++) {
							int v = u % 2;
							int w = 1 - v;
							if (u % 4 >= 2) {
								v = -v;
								w = -w;
							}

							for (int x = 0; x < 3; x++) {
								for (int y = 0; y < 4; y++) {
									for (int z = -1; z < 4; z++) {
										int aa = r + (y - 1) * v + x * w;
										int ab = t + z;
										int ac = s + (y - 1) * w - x * v;
										mutableBlockPos.set(aa, ab, ac);
										if (z < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid() || z >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) {
											continue label279;
										}
									}
								}
							}

							double g = (double)t + 0.5 - entity.y;
							double h = e * e + g * g + f * f;
							if (d < 0.0 || h < d) {
								d = h;
								m = r;
								n = t;
								o = s;
								p = u % 4;
							}
						}
					}
				}
			}
		}

		if (d < 0.0) {
			for (int r = j - 16; r <= j + 16; r++) {
				double e = (double)r + 0.5 - entity.x;

				for (int s = l - 16; s <= l + 16; s++) {
					double f = (double)s + 0.5 - entity.z;

					label216:
					for (int tx = this.level.getHeight() - 1; tx >= 0; tx--) {
						if (this.level.isEmptyBlock(mutableBlockPos.set(r, tx, s))) {
							while (tx > 0 && this.level.isEmptyBlock(mutableBlockPos.set(r, tx - 1, s))) {
								tx--;
							}

							for (int u = q; u < q + 2; u++) {
								int vx = u % 2;
								int wx = 1 - vx;

								for (int x = 0; x < 4; x++) {
									for (int y = -1; y < 4; y++) {
										int zx = r + (x - 1) * vx;
										int aa = tx + y;
										int ab = s + (x - 1) * wx;
										mutableBlockPos.set(zx, aa, ab);
										if (y < 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().isSolid() || y >= 0 && !this.level.isEmptyBlock(mutableBlockPos)) {
											continue label216;
										}
									}
								}

								double g = (double)tx + 0.5 - entity.y;
								double h = e * e + g * g + f * f;
								if (d < 0.0 || h < d) {
									d = h;
									m = r;
									n = tx;
									o = s;
									p = u % 2;
								}
							}
						}
					}
				}
			}
		}

		int ad = m;
		int ae = n;
		int s = o;
		int af = p % 2;
		int ag = 1 - af;
		if (p % 4 >= 2) {
			af = -af;
			ag = -ag;
		}

		if (d < 0.0) {
			n = Mth.clamp(n, 70, this.level.getHeight() - 10);
			ae = n;

			for (int txx = -1; txx <= 1; txx++) {
				for (int u = 1; u < 3; u++) {
					for (int vx = -1; vx < 3; vx++) {
						int wx = ad + (u - 1) * af + txx * ag;
						int x = ae + vx;
						int yx = s + (u - 1) * ag - txx * af;
						boolean bl = vx < 0;
						mutableBlockPos.set(wx, x, yx);
						this.level.setBlockAndUpdate(mutableBlockPos, bl ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
					}
				}
			}
		}

		for (int txx = -1; txx < 3; txx++) {
			for (int u = -1; u < 4; u++) {
				if (txx == -1 || txx == 2 || u == -1 || u == 3) {
					mutableBlockPos.set(ad + txx * af, ae + u, s + txx * ag);
					this.level.setBlock(mutableBlockPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
				}
			}
		}

		BlockState blockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, af == 0 ? Direction.Axis.Z : Direction.Axis.X);

		for (int ux = 0; ux < 2; ux++) {
			for (int vx = 0; vx < 3; vx++) {
				mutableBlockPos.set(ad + ux * af, ae + vx, s + ux * ag);
				this.level.setBlock(mutableBlockPos, blockState, 18);
			}
		}

		return true;
	}
}
