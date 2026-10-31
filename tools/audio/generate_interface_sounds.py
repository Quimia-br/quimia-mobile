"""Render Quimia's original, dry interface Foley. No external recordings or licenses.

Run from any directory. --preview writes a listening reel outside Android resources.
"""
import argparse
import math
from pathlib import Path
import random
import struct
import wave

RATE = 48000
ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "app/src/main/res/raw"


def felt_hit(time, frequency, decay, variant=0):
    if time < 0:
        return 0.0
    attack = 1 - math.exp(-time / 0.0012)
    # Damped, slightly inharmonic resonances give a small wooden/ceramic body instead of a pure beep.
    modes = [(1, 1, 1), (2.73 + variant * .011, .24, .52), (5.08, .075, .24)]
    return attack * sum(
        weight * math.exp(-time / (decay * damping)) * math.sin(2 * math.pi * frequency * ratio * time)
        for ratio, weight, damping in modes
    )


def render(kind, variant):
    rng = random.Random(7210 + variant * 19 + list(DURATIONS).index(kind) * 257)
    duration = DURATIONS[kind]
    result = []
    low_noise = 0.0
    very_low_noise = 0.0
    pitch = [1.0, 1.047, .955][variant % 3]
    for frame in range(round(duration * RATE)):
        t = frame / RATE
        low_noise += .16 * (rng.uniform(-1, 1) - low_noise)
        very_low_noise += .025 * (low_noise - very_low_noise)
        brush = low_noise - very_low_noise
        if kind == "tap":
            sample = .85 * felt_hit(t, 470 * pitch, .012, variant)
            sample += brush * .55 * math.exp(-t / .005) * (1 - math.exp(-t / .0005))
        elif kind == "selection":
            sample = .58 * felt_hit(t, 740 * pitch, .014, variant)
            sample += brush * .22 * math.exp(-t / .013) * (1 - math.exp(-t / .002))
        elif kind == "navigation":
            sample = brush * .44 * math.sin(math.pi * min(t / .09, 1)) ** 2 * math.exp(-t / .05)
            sample += .45 * felt_hit(t - .014, 570 * pitch, .022, variant)
        elif kind == "success":
            sample = .58 * felt_hit(t, 784 * pitch, .045, variant)
            sample += .72 * felt_hit(t - .085, 1176 * pitch, .052, variant)
            sample += brush * .10 * math.exp(-t / .018)
        else:
            sample = .65 * felt_hit(t, 520 * pitch, .026, variant)
            sample += .55 * felt_hit(t - .073, 390 * pitch, .033, variant)
            sample += brush * .20 * math.exp(-t / .011)
        fade = min(1, t / .0008) * min(1, (duration - t) / .012)
        result.append(sample * fade)
    # Remove residual DC, then master every texture consistently. Keep silence at both boundaries.
    average = sum(result) / len(result)
    result = [(x - average) * min(1, i / 96, (len(result) - 1 - i) / 384) for i, x in enumerate(result)]
    gain = PEAKS[kind] / max(abs(x) for x in result)
    return [x * gain for x in result]


def write_wave(path, samples):
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as out:
        out.setparams((1, 2, RATE, 0, "NONE", "not compressed"))
        out.writeframes(struct.pack(f"<{len(samples)}h", *(round(x * 32767) for x in samples)))


DURATIONS = {"tap": .055, "navigation": .115, "selection": .070, "success": .290, "error": .220}
PEAKS = {"tap": .42, "navigation": .40, "selection": .36, "success": .50, "error": .44}
VARIANTS = {"tap": 3, "navigation": 3, "selection": 3, "success": 2, "error": 2}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=Path)
    args = parser.parse_args()
    preview = [0.] * (RATE // 3)
    # Reel order: taps, navigation, selection, success, then error. Mix levels match Android playback.
    levels = {"tap": .42, "navigation": .48, "selection": .38, "success": .65, "error": .60}
    for kind, count in VARIANTS.items():
        for variant in range(count):
            samples = render(kind, variant)
            suffix = f"_{variant + 1}" if variant else ""
            write_wave(OUTPUT / f"ui_{kind}{suffix}.wav", samples)
            preview.extend(x * levels[kind] for x in samples)
            preview.extend([0.] * round(RATE * .24))
        preview.extend([0.] * (RATE // 2))
    if args.preview:
        write_wave(args.preview, preview)
    print(f"Rendered {sum(VARIANTS.values())} original interface sounds at {RATE} Hz.")


if __name__ == "__main__":
    main()
