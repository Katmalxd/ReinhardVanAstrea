package com.reinhardaddon;

/**
 * Interruptor de cliente. Mientras ReinhardSunstrikeEntity.tick() ejecuta el tick original de Mowzie's,
 * vale true; ClientLevelMixin sustituye entonces las partículas vanilla (humo/llama anaranjados)
 * por END_ROD (blanco puro). Las partículas propias de Mowzie's (orb) ya son blancas.
 * Solo se lee/escribe en el hilo de render del cliente.
 */
public final class ParticleWhitening {
    public static boolean ACTIVE = false;

    private ParticleWhitening() {}
}
