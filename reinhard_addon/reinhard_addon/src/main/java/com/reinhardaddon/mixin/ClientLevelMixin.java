package com.reinhardaddon.mixin;

import com.reinhardaddon.ParticleWhitening;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Único mixin del addon, y apunta a una clase VANILLA (no a Mowzie's) para que el refmap
 * funcione igual en entorno de desarrollo y en producción.
 *
 * Mientras ParticleWhitening.ACTIVE es true (tick de nuestro Sunstrike en el cliente), cualquier
 * partícula "simple" vanilla (humo, llama...) se cambia por END_ROD, que es blanco puro.
 * Las partículas de Mowzie's (ParticleOrb.OrbData) NO son SimpleParticleType, así que pasan intactas
 * (su textura orb.png ya es 100% blanca y se tinta con 1,1,1).
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @ModifyVariable(
            method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
            at = @At("HEAD"),
            argsOnly = true)
    private ParticleOptions reinhard_addon$whiten(ParticleOptions original) {
        if (ParticleWhitening.ACTIVE && original instanceof SimpleParticleType) {
            return ParticleTypes.END_ROD;
        }
        return original;
    }
}
