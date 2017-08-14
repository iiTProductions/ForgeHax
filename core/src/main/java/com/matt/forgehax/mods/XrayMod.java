package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.RenderBlockInLayerEvent;
import com.matt.forgehax.asm.events.RenderBlockLayerEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class XrayMod extends ToggleMod {
    public final Setting<Integer> opacity = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("opacity")
            .description("Xray opacity")
            .defaultTo(150)
            .min(0)
            .max(255)
            .changed(cb -> {
                ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (cb.getTo().floatValue() / 255.f);
                reloadRenderers();
            })
            .build();

    private boolean previousForgeLightPipelineEnabled = false;

    public XrayMod() {
        super("Xray", false, "See blocks through walls");
    }

    public void reloadRenderers() {
        if(MC.renderGlobal != null) {
            if (!MC.isCallingFromMinecraftThread())
                MC.addScheduledTask(() -> {
                    MC.renderGlobal.loadRenderers();
                });
            else
                MC.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onEnabled() {
        previousForgeLightPipelineEnabled = ForgeModContainer.forgeLightPipelineEnabled;
        ForgeModContainer.forgeLightPipelineEnabled = false;
        ForgeHaxHooks.COLOR_MULTIPLIER_ALPHA = (this.opacity.getAsFloat() / 255.f);
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = true;
        reloadRenderers();
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable();
    }

    @Override
    public void onDisabled() {
        ForgeModContainer.forgeLightPipelineEnabled = previousForgeLightPipelineEnabled;
        ForgeHaxHooks.SHOULD_UPDATE_ALPHA = false;
        reloadRenderers();
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable();
    }

    private boolean isInternalCall = false;

    @SubscribeEvent
    public void onPreRenderBlockLayer(RenderBlockLayerEvent.Pre event) {
        if(!isInternalCall) {
            if (!event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
                event.setCanceled(true);
            } else if (event.getRenderLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
                isInternalCall = true;
                Entity renderEntity = MC.getRenderViewEntity();
                GlStateManager.disableAlpha();
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, event.getPartialTicks(), 0, renderEntity);
                GlStateManager.enableAlpha();
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), 0, renderEntity);
                MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
                MC.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT, event.getPartialTicks(), 0, renderEntity);
                MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
                GlStateManager.disableAlpha();
                isInternalCall = false;
            }
        }
    }

    @SubscribeEvent
    public void onPostRenderBlockLayer(RenderBlockLayerEvent.Post event) {}

    @SubscribeEvent
    public void onRenderBlockInLayer(RenderBlockInLayerEvent event) {
        if(event.getCompareToLayer().equals(BlockRenderLayer.TRANSLUCENT)) {
            event.setLayer(event.getCompareToLayer());
        }
    }
}