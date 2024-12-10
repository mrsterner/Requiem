/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.client;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.client.model.lib.SimpleBakedModel;
import ladysnake.requiem.client.model.lib.SimpleUnbakedModel;
import ladysnake.requiem.client.network.ClientMessageHandler;
import ladysnake.requiem.client.particle.CureParticle;
import ladysnake.requiem.client.particle.GhostParticle;
import ladysnake.requiem.client.render.block.RunestoneBlockEntityRenderer;
import ladysnake.requiem.client.render.entity.CuredPiglinEntityRenderer;
import ladysnake.requiem.client.render.entity.CuredVillagerEntityRenderer;
import ladysnake.requiem.client.render.entity.MorticianEntityRenderer;
import ladysnake.requiem.client.render.entity.ObeliskSoulEntityRenderer;
import ladysnake.requiem.client.render.entity.SoulEntityRenderer;
import ladysnake.requiem.client.render.entity.model.MorticianEntityModel;
import ladysnake.requiem.client.render.entity.model.WillOWispModel;
import ladysnake.requiem.common.block.RequiemBlockEntities;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.Map;

public final class RequiemClient {

    public static final ButtonTextures CRAFTING_BUTTON_TEXTURE = new ButtonTextures(
        Requiem.id("textures/gui/crafting_button.png"),
        Requiem.id("textures/gui/crafting_button.png"),
        Requiem.id("textures/gui/crafting_button.png"),
        Requiem.id("textures/gui/crafting_button.png")
    );
    //public static final Identifier SOULBOUND_BACKGROUND = Requiem.id("textures/gui/soulbound_background.png");
    public static final Identifier SOULBOUND_BACKGROUND_LARGE = Requiem.id("textures/gui/soulbound_background_large.png");
    public static final Identifier SOULBOUND_BACKGROUND_SMALL = Requiem.id("textures/gui/soulbound_background_small.png");
    public static final Identifier SOULBOUND_BACKGROUND_SMALL_AMBIENT = Requiem.id("textures/gui/soulbound_background_small_ambient.png");

    private static final RequiemClient INSTANCE = new RequiemClient();

    @CalledThroughReflection
    public static void onInitializeClient() {
        INSTANCE.init();
    }

    public static RequiemClient instance() {
        return INSTANCE;
    }

    private final ClientMessageHandler messageHandler;
    private final RequiemClientListener listener;
    private final RequiemTargetHandler targetHandler;
    private final RequiemEntityShaderPicker shaderPicker;
    private final RequiemStatusEffectSpriteManager statusEffectSpriteManager;

    private final RequiemFx requiemFxRenderer;
    private final ShadowPlayerFx shadowPlayerFxRenderer;
    private final ZaWorldFx worldFreezeFxRenderer;

    private RequiemClient() {
        this.messageHandler = new ClientMessageHandler(this);
        this.listener = new RequiemClientListener(this);
        this.targetHandler = new RequiemTargetHandler();
        this.requiemFxRenderer = new RequiemFx();
        this.shaderPicker = new RequiemEntityShaderPicker();
        this.statusEffectSpriteManager = new RequiemStatusEffectSpriteManager();
        this.shadowPlayerFxRenderer = new ShadowPlayerFx();
        this.worldFreezeFxRenderer = new ZaWorldFx();
    }

    public RequiemStatusEffectSpriteManager statusEffectSpriteManager() {
        return statusEffectSpriteManager;
    }

    public ShadowPlayerFx shadowPlayerFxRenderer() {
        return shadowPlayerFxRenderer;
    }

    public ZaWorldFx worldFreezeFxRenderer() {
        return worldFreezeFxRenderer;
    }

    public RequiemFx fxRenderer() {
        return requiemFxRenderer;
    }

    private void init() {
        this.registerBlockModels();
        this.registerEntityModels();
        this.registerBlockEntityRenderers();
        this.registerEntityRenderers();
        this.registerModelPredicates();
        this.registerParticleFactories();
        this.registerScreens();
        this.registerSprites();
        this.initListeners();
        FractureKeyBinding.init();
    }

    private void registerScreens() {
        //TODO HandledScreens.register(RequiemScreenHandlers.RIFT_SCREEN_HANDLER, RiftScreen::new);
    }

    private void registerBlockModels() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(RequiemBlocks.streamRunestones().map(Map.Entry::getValue).map(RunestoneBlockEntityRenderer::createRuneIdentifier).toList());
            pluginContext.resolveModel().register(context -> {
                Identifier modelId = context.id();
                ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
                if (modelId.getNamespace().equals(Requiem.MOD_ID)) {
                    if (modelId.getPath().startsWith("tachylite_rune/")) {
                        String effect = modelId.getPath().substring(modelId.getPath().lastIndexOf('/') + 1);
                        String sideRuneSpriteId = ifExistsOrElse(resourceManager, "block/%s_rune_side".formatted(effect), "block/%s_rune".formatted(effect));
                        String topRuneSpriteId = ifExistsOrElse(resourceManager, "block/%s_rune_top".formatted(effect), "block/neutral_rune");
                        return new SimpleUnbakedModel(mb -> {
                            Sprite sideRuneSprite = mb.getSprite(sideRuneSpriteId);
                            Sprite topRuneSprite = mb.getSprite(topRuneSpriteId);
                            mb.box(mb.finder().emissive(true).ambientOcclusion(TriState.FALSE).disableDiffuse(true).blendMode(BlendMode.CUTOUT).find(),
                                -1, d -> d.getAxis() == Direction.Axis.Y ? topRuneSprite : sideRuneSprite,
                                -0.0001f, -0.0001f, -0.0001f, 1.0001f, 1.0001f, 1.0001f);
                            return new SimpleBakedModel(mb.builder.build(), ModelHelper.MODEL_TRANSFORM_BLOCK, sideRuneSprite, null);
                        });
                    }
                }
                return null;
            });
        });
    }

    private String ifExistsOrElse(ResourceManager resources, String attempt, String fallback) {
        return "requiem:" + (resources.getResource(Requiem.id("textures/" + attempt + ".png")).isPresent() ? attempt : fallback);
    }

    private void registerParticleFactories() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(RequiemParticleTypes.ATTRITION, PortalParticle.Factory::new);
        registry.register(RequiemParticleTypes.ATTUNED, CureParticle.Factory::new);
        registry.register(RequiemParticleTypes.CURE, CureParticle.Factory::new);
        registry.register(RequiemParticleTypes.GHOST, GhostParticle.Factory::new);
        //TODO registry.register(RequiemParticleTypes.ENTITY_DUST, new EntityDustParticle.Factory());
        registry.register(RequiemParticleTypes.OBELISK_SOUL, SoulParticle.Factory::new);
        //registry.register(RequiemParticleTypes.SOUL_TRAIL, WispTrailParticle.Factory::new);
        //registry.register(RequiemParticleTypes.PENANCE, SpellParticle.EntityFactory::new);
    }

    private void registerModelPredicates() {
    }

    private void registerSprites() {
        this.statusEffectSpriteManager().registerAltSprites(RequiemStatusEffects.ATTRITION.value(), 4);
    }

    private void registerEntityModels() {
        EntityModelLayerRegistry.registerModelLayer(MorticianEntityModel.MODEL_LAYER, MorticianEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(WillOWispModel.BASE_MODEL_LAYER, WillOWispModel::getTexturedModelData);
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(RequiemBlockEntities.RUNIC_OBSIDIAN, RunestoneBlockEntityRenderer::new);
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(RequiemEntities.OBELISK_SOUL, ObeliskSoulEntityRenderer::new);
        EntityRendererRegistry.register(RequiemEntities.RELEASED_SOUL, SoulEntityRenderer::new);
        EntityRendererRegistry.register(RequiemEntities.CURED_VILLAGER, CuredVillagerEntityRenderer::new);
        EntityRendererRegistry.register(RequiemEntities.CURED_PIGLIN, (ctx) -> new CuredPiglinEntityRenderer(ctx, EntityModelLayers.PIGLIN, EntityModelLayers.PIGLIN_INNER_ARMOR, EntityModelLayers.PIGLIN_OUTER_ARMOR, false));
        EntityRendererRegistry.register(RequiemEntities.CURED_PIGLIN_BRUTE, (ctx) -> new CuredPiglinEntityRenderer(ctx, EntityModelLayers.PIGLIN_BRUTE, EntityModelLayers.PIGLIN_BRUTE_INNER_ARMOR, EntityModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false));
        // shh, it's fine
        @SuppressWarnings({"unchecked", "RedundantCast"}) EntityType<? extends AbstractClientPlayerEntity> playerShellType = (EntityType<? extends AbstractClientPlayerEntity>) (EntityType<?>) RequiemEntities.PLAYER_SHELL;
        EntityRendererRegistry.register(playerShellType, ctx -> new PlayerEntityRenderer(ctx, false));
        EntityRendererRegistry.register(RequiemEntities.MORTICIAN, MorticianEntityRenderer::new);
    }

    private void initListeners() {
        this.messageHandler.init();
        this.shaderPicker.registerCallbacks();
        this.requiemFxRenderer.registerCallbacks();
        this.shadowPlayerFxRenderer.registerCallbacks();
        this.worldFreezeFxRenderer.registerCallbacks();
        this.listener.registerCallbacks();
        this.targetHandler.registerCallbacks();
    }
}
