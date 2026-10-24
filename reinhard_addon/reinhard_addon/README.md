# Reinhard van Astrea – addon para MCA (Forge 1.20.1)

Un **guardia de MCA** llamado "Reinhard" (o creado con `/reinhard summon`) lleva la espada de Tensura Blade:
**enfundada** cuando no hay enemigos y **desenvainada + vaina en la otra mano** cuando los hay. Con la espada en la
mano principal puede invocar un **Sunstrike de Mowzie's Mobs** de radio 9 (3x) y en **blanco puro**. Nadie más puede.

> Tensura Blade es NeoForge 1.21.1: no se puede cargar junto a MCA/Mowzie's (Forge 1.20.1). Solo se usó como
> fuente de modelos/texturas, ya convertidos y dentro de este proyecto. **Uso privado** (licencias de terceros).

## Obtener el .jar (elige UNA)

### A) Sin instalar nada: GitHub Actions
1. Crea un repositorio **PRIVADO** en github.com y sube TODO el contenido de esta carpeta.
2. Copia a `libs/` tus dos jars con los nombres de `libs/README.txt` (súbelos también).
3. Pestaña **Actions** → "Construir reinhard_addon.jar" → espera ~5-10 min (verde = bien).
4. Entra en esa ejecución → **Artifacts** → descarga `reinhard_addon-jar` → dentro está `reinhard_addon-1.0.0.jar`.
   Si sale en rojo, abre el paso que falló y pásame el texto del error.

### B) En tu PC
Instala JDK 17 y Gradle 8.8, copia los jars a `libs/` y ejecuta `gradle build`. El jar queda en `build/libs/`.

## Instalar en el server (y en los clientes)
Carpeta `mods/`: `reinhard_addon-1.0.0.jar` + MCA 7.7.1 + Mowzie's Mobs 1.8.2 + GeckoLib 4.4.2+ (Forge 1.20.1, Forge 47.x).
Los jugadores necesitan los mismos mods.

## Usar
- Pon a un **guardia** de MCA el nombre que contenga "Reinhard" → se convierte solo en ~2 s. 
- O como operador: `/reinhard summon` · `/reinhard convert <entidad>` · `/reinhard status <entidad>` (diagnóstico).
- Prueba: `/summon zombie ~8 ~ ~8` → desenvaina; ~1,75 s después cae el rayo blanco.
- Condición de uso: `/item replace entity <reinhard> weapon.mainhand with minecraft:stick` → no lanza más rayos.

## Ajustes
| Qué | Dónde |
|---|---|
| Radio del área (original 3.0) / daño | `ReinhardSunstrikeEntity.AREA_RADIUS` / `DAMAGE` |
| Cooldown, alcance, tiempo para enfundar | `ReinhardAbilityHandler` (constantes arriba) |
| Posición de la espada/vaina en la mano | `display` en `models/item/reinhard_sword*.json` y `reinhard_sheath.json` |
