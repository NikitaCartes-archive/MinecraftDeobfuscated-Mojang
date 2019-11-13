package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugMobNameGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

@Environment(EnvType.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final Map<BlockPos, BeeDebugRenderer.HiveInfo> hives = Maps.<BlockPos, BeeDebugRenderer.HiveInfo>newHashMap();
	private final Map<UUID, BeeDebugRenderer.BeeInfo> beeInfosPerEntity = Maps.<UUID, BeeDebugRenderer.BeeInfo>newHashMap();
	private UUID lastLookedAtUuid;

	public BeeDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void clear() {
		this.hives.clear();
		this.beeInfosPerEntity.clear();
		this.lastLookedAtUuid = null;
	}

	public void addOrUpdateHiveInfo(BeeDebugRenderer.HiveInfo hiveInfo) {
		this.hives.put(hiveInfo.pos, hiveInfo);
	}

	public void addOrUpdateBeeInfo(BeeDebugRenderer.BeeInfo beeInfo) {
		this.beeInfosPerEntity.put(beeInfo.uuid, beeInfo);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, long l) {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		this.clearRemovedHives();
		this.clearRemovedBees();
		this.doRender();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
		if (!this.minecraft.player.isSpectator()) {
			this.updateLastLookedAtUuid();
		}
	}

	private void clearRemovedBees() {
		this.beeInfosPerEntity.entrySet().removeIf(entry -> this.minecraft.level.getEntity(((BeeDebugRenderer.BeeInfo)entry.getValue()).id) == null);
	}

	private void clearRemovedHives() {
		long l = this.minecraft.level.getGameTime() - 20L;
		this.hives.entrySet().removeIf(entry -> ((BeeDebugRenderer.HiveInfo)entry.getValue()).lastSeen < l);
	}

	private void doRender() {
		BlockPos blockPos = this.getCamera().getBlockPosition();
		this.beeInfosPerEntity.values().forEach(beeInfo -> {
			if (this.isPlayerCloseEnoughToMob(beeInfo)) {
				this.renderBeeInfo(beeInfo);
			}
		});
		this.renderFlowerInfos();

		for (BlockPos blockPos2 : this.hives.keySet()) {
			if (blockPos.closerThan(blockPos2, 30.0)) {
				highlightHive(blockPos2);
			}
		}

		this.hives.values().forEach(hiveInfo -> {
			if (blockPos.closerThan(hiveInfo.pos, 30.0)) {
				this.renderHiveInfo(hiveInfo);
			}
		});
		this.getGhostHives().forEach((blockPos2x, list) -> {
			if (blockPos.closerThan(blockPos2x, 30.0)) {
				this.renderGhostHive(blockPos2x, list);
			}
		});
	}

	private void renderFlowerInfos() {
		Map<BlockPos, Set<UUID>> map = Maps.<BlockPos, Set<UUID>>newHashMap();
		this.beeInfosPerEntity.values().stream().filter(BeeDebugRenderer.BeeInfo::hasFlower).forEach(beeInfo -> {
			Set<UUID> set = (Set<UUID>)map.get(beeInfo.flowerPos);
			if (set == null) {
				set = Sets.<UUID>newHashSet();
				map.put(beeInfo.flowerPos, set);
			}

			set.add(beeInfo.getUuid());
		});
		map.entrySet().forEach(entry -> {
			BlockPos blockPos = (BlockPos)entry.getKey();
			Set<UUID> set = (Set<UUID>)entry.getValue();
			Set<String> set2 = (Set<String>)set.stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
			renderTextOverPos(set2.toString(), blockPos, 1, -256);
			renderTextOverPos("Flower", blockPos, 2, -1);
			float f = 0.05F;
			renderTransparentFilledBox(blockPos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
		});
	}

	private static void highlightHive(BlockPos blockPos) {
		float f = 0.05F;
		renderTransparentFilledBox(blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
	}

	private void renderGhostHive(BlockPos blockPos, List<String> list) {
		float f = 0.05F;
		renderTransparentFilledBox(blockPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
		renderTextOverPos("" + list, blockPos, 0, -256);
		renderTextOverPos("Ghost Hive", blockPos, 1, -65536);
	}

	private static void renderTransparentFilledBox(BlockPos blockPos, float f, float g, float h, float i, float j) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		DebugRenderer.renderFilledBox(blockPos, f, g, h, i, j);
	}

	private void renderHiveInfo(BeeDebugRenderer.HiveInfo hiveInfo) {
		int i = 0;
		if (this.getHiveMemberNames(hiveInfo).isEmpty()) {
			renderTextOverHive("Out: -", hiveInfo, i, -3355444);
		} else if (this.getHiveMemberNames(hiveInfo).size() < 4) {
			renderTextOverHive("Out: " + this.getHiveMemberNames(hiveInfo), hiveInfo, i, -3355444);
		} else {
			renderTextOverHive("Out: " + this.getHiveMemberNames(hiveInfo).size() + " bees", hiveInfo, i, -3355444);
		}

		i++;
		if (hiveInfo.occupantCount == 0) {
			renderTextOverHive("In: -", hiveInfo, i, -256);
		} else if (hiveInfo.occupantCount == 1) {
			renderTextOverHive("In: 1 bee", hiveInfo, i, -256);
		} else {
			renderTextOverHive("In: " + hiveInfo.occupantCount + " bees", hiveInfo, i, -256);
		}

		renderTextOverHive("Honey: " + hiveInfo.honeyLevel, hiveInfo, ++i, -23296);
		renderTextOverHive(hiveInfo.hiveType + (hiveInfo.sedated ? " (sedated)" : ""), hiveInfo, ++i, -1);
	}

	private void renderPath(BeeDebugRenderer.BeeInfo beeInfo) {
		if (beeInfo.path != null) {
			PathfindingRenderer.renderPath(
				beeInfo.path, 0.5F, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z()
			);
		}
	}

	private void renderBeeInfo(BeeDebugRenderer.BeeInfo beeInfo) {
		boolean bl = this.isBeeSelected(beeInfo);
		int i = 0;
		renderTextOverMob(beeInfo.pos, i, beeInfo.toString(), -1, 0.03F);
		i++;
		if (beeInfo.hivePos == null) {
			renderTextOverMob(beeInfo.pos, i, "Homeless :(", -98404, 0.02F);
		} else {
			renderTextOverMob(beeInfo.pos, i, "Hive: " + this.getPosDescription(beeInfo, beeInfo.hivePos), -256, 0.02F);
		}

		i++;
		if (beeInfo.flowerPos == null) {
			renderTextOverMob(beeInfo.pos, i, "No flower :(", -98404, 0.02F);
		} else {
			renderTextOverMob(beeInfo.pos, i, "Flower: " + this.getPosDescription(beeInfo, beeInfo.flowerPos), -256, 0.02F);
		}

		i++;

		for (String string : beeInfo.goals) {
			renderTextOverMob(beeInfo.pos, i, string, -16711936, 0.02F);
			i++;
		}

		if (bl) {
			this.renderPath(beeInfo);
		}
	}

	private static void renderTextOverHive(String string, BeeDebugRenderer.HiveInfo hiveInfo, int i, int j) {
		BlockPos blockPos = hiveInfo.pos;
		renderTextOverPos(string, blockPos, i, j);
	}

	private static void renderTextOverPos(String string, BlockPos blockPos, int i, int j) {
		double d = 1.3;
		double e = 0.2;
		double f = (double)blockPos.getX() + 0.5;
		double g = (double)blockPos.getY() + 1.3 + (double)i * 0.2;
		double h = (double)blockPos.getZ() + 0.5;
		DebugRenderer.renderFloatingText(string, f, g, h, j, 0.02F, true, 0.0F, true);
	}

	private static void renderTextOverMob(Position position, int i, String string, int j, float f) {
		double d = 2.4;
		double e = 0.25;
		BlockPos blockPos = new BlockPos(position);
		double g = (double)blockPos.getX() + 0.5;
		double h = position.y() + 2.4 + (double)i * 0.25;
		double k = (double)blockPos.getZ() + 0.5;
		float l = 0.5F;
		DebugRenderer.renderFloatingText(string, g, h, k, j, f, false, 0.5F, true);
	}

	private Camera getCamera() {
		return this.minecraft.gameRenderer.getMainCamera();
	}

	private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo hiveInfo) {
		return (Set<String>)this.getHiveMembers(hiveInfo.pos).stream().map(DebugMobNameGenerator::getMobName).collect(Collectors.toSet());
	}

	private String getPosDescription(BeeDebugRenderer.BeeInfo beeInfo, BlockPos blockPos) {
		float f = Mth.sqrt(blockPos.distSqr(beeInfo.pos.x(), beeInfo.pos.y(), beeInfo.pos.z(), true));
		double d = (double)Math.round(f * 10.0F) / 10.0;
		return blockPos.toShortString() + " (dist " + d + ")";
	}

	private boolean isBeeSelected(BeeDebugRenderer.BeeInfo beeInfo) {
		return Objects.equals(this.lastLookedAtUuid, beeInfo.uuid);
	}

	private boolean isPlayerCloseEnoughToMob(BeeDebugRenderer.BeeInfo beeInfo) {
		Player player = this.minecraft.player;
		BlockPos blockPos = new BlockPos(player.getX(), beeInfo.pos.y(), player.getZ());
		BlockPos blockPos2 = new BlockPos(beeInfo.pos);
		return blockPos.closerThan(blockPos2, 30.0);
	}

	private Collection<UUID> getHiveMembers(BlockPos blockPos) {
		return (Collection<UUID>)this.beeInfosPerEntity
			.values()
			.stream()
			.filter(beeInfo -> beeInfo.hasHive(blockPos))
			.map(BeeDebugRenderer.BeeInfo::getUuid)
			.collect(Collectors.toSet());
	}

	private Map<BlockPos, List<String>> getGhostHives() {
		Map<BlockPos, List<String>> map = Maps.<BlockPos, List<String>>newHashMap();

		for (BeeDebugRenderer.BeeInfo beeInfo : this.beeInfosPerEntity.values()) {
			if (beeInfo.hivePos != null && !this.hives.containsKey(beeInfo.hivePos)) {
				List<String> list = (List<String>)map.get(beeInfo.hivePos);
				if (list == null) {
					list = Lists.<String>newArrayList();
					map.put(beeInfo.hivePos, list);
				}

				list.add(beeInfo.getName());
			}
		}

		return map;
	}

	private void updateLastLookedAtUuid() {
		DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(entity -> this.lastLookedAtUuid = entity.getUUID());
	}

	@Environment(EnvType.CLIENT)
	public static class BeeInfo {
		public final UUID uuid;
		public final int id;
		public final Position pos;
		@Nullable
		public final Path path;
		@Nullable
		public final BlockPos hivePos;
		@Nullable
		public final BlockPos flowerPos;
		public final List<String> goals = Lists.<String>newArrayList();

		public BeeInfo(UUID uUID, int i, Position position, Path path, BlockPos blockPos, BlockPos blockPos2) {
			this.uuid = uUID;
			this.id = i;
			this.pos = position;
			this.path = path;
			this.hivePos = blockPos;
			this.flowerPos = blockPos2;
		}

		public boolean hasHive(BlockPos blockPos) {
			return this.hivePos != null && this.hivePos.equals(blockPos);
		}

		public UUID getUuid() {
			return this.uuid;
		}

		public String getName() {
			return DebugMobNameGenerator.getMobName(this.uuid);
		}

		public String toString() {
			return this.getName();
		}

		public boolean hasFlower() {
			return this.flowerPos != null;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class HiveInfo {
		public final BlockPos pos;
		public final String hiveType;
		public final int occupantCount;
		public final int honeyLevel;
		public final boolean sedated;
		public final long lastSeen;

		public HiveInfo(BlockPos blockPos, String string, int i, int j, boolean bl, long l) {
			this.pos = blockPos;
			this.hiveType = string;
			this.occupantCount = i;
			this.honeyLevel = j;
			this.sedated = bl;
			this.lastSeen = l;
		}
	}
}
