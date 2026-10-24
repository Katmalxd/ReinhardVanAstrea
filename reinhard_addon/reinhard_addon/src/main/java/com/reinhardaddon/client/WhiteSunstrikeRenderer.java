package com.reinhardaddon.client;

import com.bobmowzie.mowziesmobs.client.render.MMRenderType;
import com.bobmowzie.mowziesmobs.client.render.entity.RenderSunstrike;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.mojang.blaze3d.vertex.PoseStack;
import com.reinhardaddon.ReinhardAddon;
import com.reinhardaddon.ReinhardSunstrikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Reutiliza TODO el dibujado de Mowzie's (rayo, anillo, marca de suelo) pero:
 *  - escala la PoseStack en X/Z  -> área visual 3x (las constantes del renderer original son static final inlined)
 *  - entrega un MultiBufferSource que sustituye la textura naranja por white_sunstrike.png
 *    (el color de vértice de Mowzie's ya es (1,1,1,alfa): el color sale solo de la textura)
 */
public class WhiteSunstrikeRenderer extends RenderSunstrike {
    private static final ResourceLocation WHITE_TEXTURE =
            new ResourceLocation(ReinhardAddon.MODID, "textures/effects/white_sunstrike.png");
    private static RenderType whiteType;   // se crea una sola vez (hilo de render)

    public WhiteSunstrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntitySunstrike entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        if (whiteType == null) {
            whiteType = MMRenderType.getGlowingEffect(WHITE_TEXTURE);
        }
        pose.pushPose();
        pose.scale(ReinhardSunstrikeEntity.AREA_SCALE, 1.0F, ReinhardSunstrikeEntity.AREA_SCALE);
        // El original pide un único RenderType; lo redirigimos siempre al blanco.
        super.render(entity, yaw, partialTick, pose, ignored -> buffers.getBuffer(whiteType), packedLight);
        pose.popPose();
    }
}
