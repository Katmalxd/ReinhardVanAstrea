#!/usr/bin/env python3
"""
Extrae de TUS .jar los assets que usa el addon:
  * Tensura Blade  -> espada enfundada, espada desenvainada, vaina (OBJ/MTL/JSON) + texturas
  * Mowzie's Mobs  -> textura del Sunstrike recoloreada a blanco puro

Uso:  python tools/extract_assets.py --tensura <Tensura-Blade.jar> --mowzie <mowziesmobs.jar>
Requiere Python 3.8+ y Pillow (pip install pillow).
Solo para uso privado (Tensura Blade es "All Rights Reserved"; Mowzie's tiene licencia propia).
"""
import argparse, io, json, pathlib, sys, zipfile
from PIL import Image

MODID = "reinhard_addon"
ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets" / MODID
TB = "assets/tensura_blade/"

# modelo en Tensura Blade  ->  nombre en este addon
MODELS = {
    "training_blade":       "reinhard_sword",         # espada ENFUNDADA (sirve de modelo base)
    "training_blade_drawn": "reinhard_sword_drawn",   # espada DESENVAINADA
    "blade_sheath":         "reinhard_sheath",        # vaina (va en la otra mano mientras está desenvainada)
}


def read(z, name):
    try:
        return z.read(name)
    except KeyError:
        sys.exit(f"[ERROR] No encuentro '{name}' en el jar. ¿Versión correcta?")


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    if isinstance(data, str):
        path.write_text(data, encoding="utf-8", newline="\n")
    else:
        path.write_bytes(data)
    print(f"  + {path.relative_to(ROOT)}")


def retarget(text):
    """Cambia referencias de namespace/nombres de Tensura Blade a las de este addon."""
    for old, new in [
        ("tensura_blade:item/dragon_sword_glow", f"{MODID}:item/reinhard_sword_glow"),
        ("tensura_blade:item/dragon_sword",      f"{MODID}:item/reinhard_sword"),
        ("tensura_blade:item/training_blade_drawn", f"{MODID}:item/reinhard_sword_drawn"),
        ("tensura_blade:drawn",                  f"{MODID}:drawn"),
        ("neoforge:obj",                         "forge:obj"),
    ]:
        text = text.replace(old, new)
    return text


def tensura(jar):
    print(f"[Tensura Blade] {jar}")
    with zipfile.ZipFile(jar) as z:
        for src, dst in MODELS.items():
            base = f"{TB}models/item/{src}"
            obj = read(z, base + ".obj").decode("utf-8")
            mtl = read(z, base + ".mtl").decode("utf-8")
            mdl = read(z, base + ".json").decode("utf-8")

            obj = "\n".join(f"mtllib {dst}.mtl" if l.startswith("mtllib") else l for l in obj.splitlines()) + "\n"
            mtl, mdl = retarget(mtl), retarget(mdl)
            mdl = mdl.replace(f"tensura_blade:models/item/{src}.obj", f"{MODID}:models/item/{dst}.obj")
            if "tensura_blade" in mtl or "tensura_blade" in mdl:
                sys.exit(f"[ERROR] '{src}' aún referencia tensura_blade; formato inesperado.")
            json.loads(mdl)  # valida JSON
            write(ASSETS / f"models/item/{dst}.obj", obj)
            write(ASSETS / f"models/item/{dst}.mtl", mtl)
            write(ASSETS / f"models/item/{dst}.json", json.dumps(json.loads(mdl), indent=2) + "\n")
        write(ASSETS / "textures/item/reinhard_sword.png", read(z, f"{TB}textures/item/dragon_sword.png"))
        write(ASSETS / "textures/item/reinhard_sword_glow.png", read(z, f"{TB}textures/item/dragon_sword_glow.png"))


def mowzie(jar):
    print(f"[Mowzie's Mobs] {jar}")
    with zipfile.ZipFile(jar) as z:
        png = read(z, "assets/mowziesmobs/textures/effects/sunstrike.png")
    im = Image.open(io.BytesIO(png)).convert("RGBA")
    px, (w, h), n = im.load(), im.size, 0
    for y in range(h):
        for x in range(w):
            a = px[x, y][3]
            if a > 0:
                px[x, y] = (255, 255, 255, a)   # blanco puro, misma silueta (alfa intacto)
                n += 1
    buf = io.BytesIO(); im.save(buf, "PNG")
    write(ASSETS / "textures/effects/white_sunstrike.png", buf.getvalue())
    print(f"    {w}x{h}px, {n} píxeles recoloreados a (255,255,255)")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--tensura", required=True)
    ap.add_argument("--mowzie", required=True)
    a = ap.parse_args()
    tensura(a.tensura)
    mowzie(a.mowzie)
