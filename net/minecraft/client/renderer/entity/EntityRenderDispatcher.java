/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.AreaEffectCloudRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.ChestedHorseRenderer;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.DragonFireballRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.FireworkEntityRenderer;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.LlamaSpitRenderer;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.ShulkerBulletRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.client.renderer.entity.SpectralArrowRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EntityRenderDispatcher {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
    private final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();
    private final Map<String, PlayerRenderer> playerRenderers = Maps.newHashMap();
    private final PlayerRenderer defaultPlayerRenderer;
    private final Font font;
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternion cameraOrientation;
    public Entity crosshairPickEntity;
    public final Options options;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    public <E extends Entity> int getPackedLightCoords(E entity, float f) {
        return this.getRenderer(entity).getPackedLightCoords(entity, f);
    }

    private <T extends Entity> void register(EntityType<T> entityType, EntityRenderer<? super T> entityRenderer) {
        this.renderers.put(entityType, entityRenderer);
    }

    private void registerRenderers(ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager) {
        this.register(EntityType.AREA_EFFECT_CLOUD, new AreaEffectCloudRenderer(this));
        this.register(EntityType.ARMOR_STAND, new ArmorStandRenderer(this));
        this.register(EntityType.ARROW, new TippableArrowRenderer(this));
        this.register(EntityType.BAT, new BatRenderer(this));
        this.register(EntityType.BEE, new BeeRenderer(this));
        this.register(EntityType.BLAZE, new BlazeRenderer(this));
        this.register(EntityType.BOAT, new BoatRenderer(this));
        this.register(EntityType.CAT, new CatRenderer(this));
        this.register(EntityType.CAVE_SPIDER, new CaveSpiderRenderer(this));
        this.register(EntityType.CHEST_MINECART, new MinecartRenderer(this));
        this.register(EntityType.CHICKEN, new ChickenRenderer(this));
        this.register(EntityType.COD, new CodRenderer(this));
        this.register(EntityType.COMMAND_BLOCK_MINECART, new MinecartRenderer(this));
        this.register(EntityType.COW, new CowRenderer(this));
        this.register(EntityType.CREEPER, new CreeperRenderer(this));
        this.register(EntityType.DOLPHIN, new DolphinRenderer(this));
        this.register(EntityType.DONKEY, new ChestedHorseRenderer(this, 0.87f));
        this.register(EntityType.DRAGON_FIREBALL, new DragonFireballRenderer(this));
        this.register(EntityType.DROWNED, new DrownedRenderer(this));
        this.register(EntityType.EGG, new ThrownItemRenderer(this, itemRenderer));
        this.register(EntityType.ELDER_GUARDIAN, new ElderGuardianRenderer(this));
        this.register(EntityType.END_CRYSTAL, new EndCrystalRenderer(this));
        this.register(EntityType.ENDER_DRAGON, new EnderDragonRenderer(this));
        this.register(EntityType.ENDERMAN, new EndermanRenderer(this));
        this.register(EntityType.ENDERMITE, new EndermiteRenderer(this));
        this.register(EntityType.ENDER_PEARL, new ThrownItemRenderer(this, itemRenderer));
        this.register(EntityType.EVOKER_FANGS, new EvokerFangsRenderer(this));
        this.register(EntityType.EVOKER, new EvokerRenderer(this));
        this.register(EntityType.EXPERIENCE_BOTTLE, new ThrownItemRenderer(this, itemRenderer));
        this.register(EntityType.EXPERIENCE_ORB, new ExperienceOrbRenderer(this));
        this.register(EntityType.EYE_OF_ENDER, new ThrownItemRenderer(this, itemRenderer, 1.0f, true));
        this.register(EntityType.FALLING_BLOCK, new FallingBlockRenderer(this));
        this.register(EntityType.FIREBALL, new ThrownItemRenderer(this, itemRenderer, 3.0f, true));
        this.register(EntityType.FIREWORK_ROCKET, new FireworkEntityRenderer(this, itemRenderer));
        this.register(EntityType.FISHING_BOBBER, new FishingHookRenderer(this));
        this.register(EntityType.FOX, new FoxRenderer(this));
        this.register(EntityType.FURNACE_MINECART, new MinecartRenderer(this));
        this.register(EntityType.GHAST, new GhastRenderer(this));
        this.register(EntityType.GIANT, new GiantMobRenderer(this, 6.0f));
        this.register(EntityType.GUARDIAN, new GuardianRenderer(this));
        this.register(EntityType.HOGLIN, new HoglinRenderer(this));
        this.register(EntityType.HOPPER_MINECART, new MinecartRenderer(this));
        this.register(EntityType.HORSE, new HorseRenderer(this));
        this.register(EntityType.HUSK, new HuskRenderer(this));
        this.register(EntityType.ILLUSIONER, new IllusionerRenderer(this));
        this.register(EntityType.IRON_GOLEM, new IronGolemRenderer(this));
        this.register(EntityType.ITEM, new ItemEntityRenderer(this, itemRenderer));
        this.register(EntityType.ITEM_FRAME, new ItemFrameRenderer(this, itemRenderer));
        this.register(EntityType.LEASH_KNOT, new LeashKnotRenderer(this));
        this.register(EntityType.LIGHTNING_BOLT, new LightningBoltRenderer(this));
        this.register(EntityType.LLAMA, new LlamaRenderer(this));
        this.register(EntityType.LLAMA_SPIT, new LlamaSpitRenderer(this));
        this.register(EntityType.MAGMA_CUBE, new MagmaCubeRenderer(this));
        this.register(EntityType.MINECART, new MinecartRenderer(this));
        this.register(EntityType.MOOSHROOM, new MushroomCowRenderer(this));
        this.register(EntityType.MULE, new ChestedHorseRenderer(this, 0.92f));
        this.register(EntityType.OCELOT, new OcelotRenderer(this));
        this.register(EntityType.PAINTING, new PaintingRenderer(this));
        this.register(EntityType.PANDA, new PandaRenderer(this));
        this.register(EntityType.PARROT, new ParrotRenderer(this));
        this.register(EntityType.PHANTOM, new PhantomRenderer(this));
        this.register(EntityType.PIG, new PigRenderer(this));
        this.register(EntityType.PIGLIN, new PiglinRenderer(this, false));
        this.register(EntityType.PILLAGER, new PillagerRenderer(this));
        this.register(EntityType.POLAR_BEAR, new PolarBearRenderer(this));
        this.register(EntityType.POTION, new ThrownItemRenderer(this, itemRenderer));
        this.register(EntityType.PUFFERFISH, new PufferfishRenderer(this));
        this.register(EntityType.RABBIT, new RabbitRenderer(this));
        this.register(EntityType.RAVAGER, new RavagerRenderer(this));
        this.register(EntityType.SALMON, new SalmonRenderer(this));
        this.register(EntityType.SHEEP, new SheepRenderer(this));
        this.register(EntityType.SHULKER_BULLET, new ShulkerBulletRenderer(this));
        this.register(EntityType.SHULKER, new ShulkerRenderer(this));
        this.register(EntityType.SILVERFISH, new SilverfishRenderer(this));
        this.register(EntityType.SKELETON_HORSE, new UndeadHorseRenderer(this));
        this.register(EntityType.SKELETON, new SkeletonRenderer(this));
        this.register(EntityType.SLIME, new SlimeRenderer(this));
        this.register(EntityType.SMALL_FIREBALL, new ThrownItemRenderer(this, itemRenderer, 0.75f, true));
        this.register(EntityType.SNOWBALL, new ThrownItemRenderer(this, itemRenderer));
        this.register(EntityType.SNOW_GOLEM, new SnowGolemRenderer(this));
        this.register(EntityType.SPAWNER_MINECART, new MinecartRenderer(this));
        this.register(EntityType.SPECTRAL_ARROW, new SpectralArrowRenderer(this));
        this.register(EntityType.SPIDER, new SpiderRenderer(this));
        this.register(EntityType.SQUID, new SquidRenderer(this));
        this.register(EntityType.STRAY, new StrayRenderer(this));
        this.register(EntityType.TNT_MINECART, new TntMinecartRenderer(this));
        this.register(EntityType.TNT, new TntRenderer(this));
        this.register(EntityType.TRADER_LLAMA, new LlamaRenderer(this));
        this.register(EntityType.TRIDENT, new ThrownTridentRenderer(this));
        this.register(EntityType.TROPICAL_FISH, new TropicalFishRenderer(this));
        this.register(EntityType.TURTLE, new TurtleRenderer(this));
        this.register(EntityType.VEX, new VexRenderer(this));
        this.register(EntityType.VILLAGER, new VillagerRenderer(this, reloadableResourceManager));
        this.register(EntityType.VINDICATOR, new VindicatorRenderer(this));
        this.register(EntityType.WANDERING_TRADER, new WanderingTraderRenderer(this));
        this.register(EntityType.WITCH, new WitchRenderer(this));
        this.register(EntityType.WITHER, new WitherBossRenderer(this));
        this.register(EntityType.WITHER_SKELETON, new WitherSkeletonRenderer(this));
        this.register(EntityType.WITHER_SKULL, new WitherSkullRenderer(this));
        this.register(EntityType.WOLF, new WolfRenderer(this));
        this.register(EntityType.ZOGLIN, new ZoglinRenderer(this));
        this.register(EntityType.ZOMBIE_HORSE, new UndeadHorseRenderer(this));
        this.register(EntityType.ZOMBIE, new ZombieRenderer(this));
        this.register(EntityType.ZOMBIFIED_PIGLIN, new PiglinRenderer(this, true));
        this.register(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerRenderer(this, reloadableResourceManager));
        this.register(EntityType.STRIDER, new StriderRenderer(this));
    }

    public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager, Font font, Options options) {
        this.textureManager = textureManager;
        this.font = font;
        this.options = options;
        this.registerRenderers(itemRenderer, reloadableResourceManager);
        this.defaultPlayerRenderer = new PlayerRenderer(this);
        this.playerRenderers.put("default", this.defaultPlayerRenderer);
        this.playerRenderers.put("slim", new PlayerRenderer(this, true));
        for (EntityType entityType : Registry.ENTITY_TYPE) {
            if (entityType == EntityType.PLAYER || this.renderers.containsKey(entityType)) continue;
            throw new IllegalStateException("No renderer registered for " + Registry.ENTITY_TYPE.getKey(entityType));
        }
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
        if (entity instanceof AbstractClientPlayer) {
            String string = ((AbstractClientPlayer)entity).getModelName();
            PlayerRenderer playerRenderer = this.playerRenderers.get(string);
            if (playerRenderer != null) {
                return playerRenderer;
            }
            return this.defaultPlayerRenderer;
        }
        return this.renderers.get(entity.getType());
    }

    public void prepare(Level level, Camera camera, Entity entity) {
        this.level = level;
        this.camera = camera;
        this.cameraOrientation = camera.rotation();
        this.crosshairPickEntity = entity;
    }

    public void overrideCameraOrientation(Quaternion quaternion) {
        this.cameraOrientation = quaternion;
    }

    public void setRenderShadow(boolean bl) {
        this.shouldRenderShadow = bl;
    }

    public void setRenderHitBoxes(boolean bl) {
        this.renderHitBoxes = bl;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double d, double e, double f) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        return entityRenderer.shouldRender(entity, frustum, d, e, f);
    }

    public <E extends Entity> void render(E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        EntityRenderer<E> entityRenderer = this.getRenderer(entity);
        try {
            double m;
            float n;
            Vec3 vec3 = entityRenderer.getRenderOffset(entity, h);
            double j = d + vec3.x();
            double k = e + vec3.y();
            double l = f + vec3.z();
            poseStack.pushPose();
            poseStack.translate(j, k, l);
            entityRenderer.render(entity, g, h, poseStack, multiBufferSource, i);
            if (entity.displayFireAnimation()) {
                this.renderFlame(poseStack, multiBufferSource, entity);
            }
            poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            if (this.options.entityShadows && this.shouldRenderShadow && entityRenderer.shadowRadius > 0.0f && !entity.isInvisible() && (n = (float)((1.0 - (m = this.distanceToSqr(entity.getX(), entity.getY(), entity.getZ())) / 256.0) * (double)entityRenderer.shadowStrength)) > 0.0f) {
                EntityRenderDispatcher.renderShadow(poseStack, multiBufferSource, entity, n, h, this.level, entityRenderer.shadowRadius);
            }
            if (this.renderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                this.renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), entity, h);
            }
            poseStack.popPose();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(crashReportCategory);
            CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
            crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
            crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(d, e, f));
            crashReportCategory2.setDetail("Rotation", Float.valueOf(g));
            crashReportCategory2.setDetail("Delta", Float.valueOf(h));
            throw new ReportedException(crashReport);
        }
    }

    private void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f) {
        float g = entity.getBbWidth() / 2.0f;
        this.renderBox(poseStack, vertexConsumer, entity, 1.0f, 1.0f, 1.0f);
        if (entity instanceof EnderDragon) {
            double d = -Mth.lerp((double)f, entity.xOld, entity.getX());
            double e = -Mth.lerp((double)f, entity.yOld, entity.getY());
            double h = -Mth.lerp((double)f, entity.zOld, entity.getZ());
            for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
                poseStack.pushPose();
                double i = d + Mth.lerp((double)f, enderDragonPart.xOld, enderDragonPart.getX());
                double j = e + Mth.lerp((double)f, enderDragonPart.yOld, enderDragonPart.getY());
                double k = h + Mth.lerp((double)f, enderDragonPart.zOld, enderDragonPart.getZ());
                poseStack.translate(i, j, k);
                this.renderBox(poseStack, vertexConsumer, enderDragonPart, 0.25f, 1.0f, 0.0f);
                poseStack.popPose();
            }
        }
        if (entity instanceof LivingEntity) {
            float l = 0.01f;
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, -g, entity.getEyeHeight() - 0.01f, -g, g, entity.getEyeHeight() + 0.01f, g, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        Vec3 vec3 = entity.getViewVector(f);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.vertex(matrix4f, 0.0f, entity.getEyeHeight(), 0.0f).color(0, 0, 255, 255).endVertex();
        vertexConsumer.vertex(matrix4f, (float)(vec3.x * 2.0), (float)((double)entity.getEyeHeight() + vec3.y * 2.0), (float)(vec3.z * 2.0)).color(0, 0, 255, 255).endVertex();
    }

    private void renderBox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f, float g, float h) {
        AABB aABB = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB, f, g, h, 1.0f);
    }

    private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity) {
        TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
        poseStack.pushPose();
        float f = entity.getBbWidth() * 1.4f;
        poseStack.scale(f, f, f);
        float g = 0.5f;
        float h = 0.0f;
        float i = entity.getBbHeight() / f;
        float j = 0.0f;
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.camera.getYRot()));
        poseStack.translate(0.0, 0.0, -0.3f + (float)((int)i) * 0.02f);
        float k = 0.0f;
        int l = 0;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poseStack.last();
        while (i > 0.0f) {
            TextureAtlasSprite textureAtlasSprite3 = l % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
            float m = textureAtlasSprite3.getU0();
            float n = textureAtlasSprite3.getV0();
            float o = textureAtlasSprite3.getU1();
            float p = textureAtlasSprite3.getV1();
            if (l / 2 % 2 == 0) {
                float q = o;
                o = m;
                m = q;
            }
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, g - 0.0f, 0.0f - j, k, o, p);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -g - 0.0f, 0.0f - j, k, m, p);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -g - 0.0f, 1.4f - j, k, m, n);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, g - 0.0f, 1.4f - j, k, o, n);
            i -= 0.45f;
            j -= 0.45f;
            g *= 0.9f;
            k += 0.03f;
            ++l;
        }
        poseStack.popPose();
    }

    private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j) {
        vertexConsumer.vertex(pose.pose(), f, g, h).color(255, 255, 255, 255).uv(i, j).overlayCoords(0, 10).uv2(240).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static void renderShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, LevelReader levelReader, float h) {
        Mob mob;
        float i = h;
        if (entity instanceof Mob && (mob = (Mob)entity).isBaby()) {
            i *= 0.5f;
        }
        double d = Mth.lerp((double)g, entity.xOld, entity.getX());
        double e = Mth.lerp((double)g, entity.yOld, entity.getY());
        double j = Mth.lerp((double)g, entity.zOld, entity.getZ());
        int k = Mth.floor(d - (double)i);
        int l = Mth.floor(d + (double)i);
        int m = Mth.floor(e - (double)i);
        int n = Mth.floor(e);
        int o = Mth.floor(j - (double)i);
        int p = Mth.floor(j + (double)i);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
        for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(k, m, o), new BlockPos(l, n, p))) {
            EntityRenderDispatcher.renderBlockShadow(pose, vertexConsumer, levelReader, blockPos, d, e, j, i, f);
        }
    }

    private static void renderBlockShadow(PoseStack.Pose pose, VertexConsumer vertexConsumer, LevelReader levelReader, BlockPos blockPos, double d, double e, double f, float g, float h) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = levelReader.getBlockState(blockPos2);
        if (blockState.getRenderShape() == RenderShape.INVISIBLE || levelReader.getMaxLocalRawBrightness(blockPos) <= 3) {
            return;
        }
        if (!blockState.isCollisionShapeFullBlock(levelReader, blockPos2)) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(levelReader, blockPos.below());
        if (voxelShape.isEmpty()) {
            return;
        }
        float i = (float)(((double)h - (e - (double)blockPos.getY()) / 2.0) * 0.5 * (double)levelReader.getBrightness(blockPos));
        if (i >= 0.0f) {
            if (i > 1.0f) {
                i = 1.0f;
            }
            AABB aABB = voxelShape.bounds();
            double j = (double)blockPos.getX() + aABB.minX;
            double k = (double)blockPos.getX() + aABB.maxX;
            double l = (double)blockPos.getY() + aABB.minY;
            double m = (double)blockPos.getZ() + aABB.minZ;
            double n = (double)blockPos.getZ() + aABB.maxZ;
            float o = (float)(j - d);
            float p = (float)(k - d);
            float q = (float)(l - e);
            float r = (float)(m - f);
            float s = (float)(n - f);
            float t = -o / 2.0f / g + 0.5f;
            float u = -p / 2.0f / g + 0.5f;
            float v = -r / 2.0f / g + 0.5f;
            float w = -s / 2.0f / g + 0.5f;
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, i, o, q, r, t, v);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, i, o, q, s, t, w);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, i, p, q, s, u, w);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, i, p, q, r, u, v);
        }
    }

    private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k) {
        vertexConsumer.vertex(pose.pose(), g, h, i).color(1.0f, 1.0f, 1.0f, f).uv(j, k).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
    }

    public void setLevel(@Nullable Level level) {
        this.level = level;
        if (level == null) {
            this.camera = null;
        }
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.getPosition().distanceToSqr(entity.position());
    }

    public double distanceToSqr(double d, double e, double f) {
        return this.camera.getPosition().distanceToSqr(d, e, f);
    }

    public Quaternion cameraOrientation() {
        return this.cameraOrientation;
    }

    public Font getFont() {
        return this.font;
    }
}

