# FitFlow MW — Exercise Asset Prompt Generator V3
Version: 3.0 (2026-11-07)

## Purpose
Source of truth for reusable exercise-image prompts for FitFlow MW and FitFlow GO. Ask ChatGPT: "ดึง FitFlow_Prompt_Generator.md จาก GitHub แล้วเปิด Prompt Generator V3". Markdown stores the rules; ChatGPT renders interactive controls on request.

## Reference hierarchy
1. CHARACTER IDENTITY: Use the approved standalone portrait: LACS-Banner(1).png, Confident Anime Fitness Coach Waving(2).png (Saryu), HATI-Banner(1).png.
2. EXERCISE STYLE: Use S1-SeatedBand Row.png, M3-HipFlexorStretch(3).png and the approved S16 illustration for chibi proportions, clean bold outlines, flat colors, minimal cel shading, white background and blue gradient arrows.
3. Do not copy old costume symbols from exercise guides or incidental portrait props/backgrounds.
4. If reference images are inaccessible, tell the user before generating.

## Characters
- **LACS — Strength:** Spiky dark/electric blue hair, red eyes, white headband with blue dumbbell icon, sleeveless deep blue hoodie with white drawstrings and dumbbell emblem, blue wristbands, dark shorts with white side stripe, white-blue sneakers. Do not use obsolete triangle emblem.
- **SARYU — Mobility/Recovery/Balance:** Layered golden-orange hair with front strand, gray eyes, thin round black glasses, eyebrow mark, cream hoodie with orange hood, sleeve stripes and plus emblem, charcoal pants with orange stripe, black-yellow shoes, white smartwatch. User may explicitly override outfit (e.g., M12 white sleeveless top).
- **HATI — Active Rest:** Pink messy bun (not ponytail), blue eyes, white headband with pink heartbeat-heart symbol, cropped white/pink short-sleeved zip hoodie with pink drawstrings and heartbeat logo, charcoal leggings with pink accents, white smartwatch. Do not automatically add the portrait's bottle, towel, sweat or pink glow.

## Generator controls
- Exercise code, title, dose, category/character; optional future-only status
- Camera: front 0°, three-quarter front 45°, side 90°, three-quarter rear 135°, back 180°
- Facing: left / right / viewer where compatible
- Pose count: one / two; independently editable Step 1 and Step 2
- Framing: full-body / knees-to-head / waist-up / custom
- Motion arrows: blue / minimal / none
- Expression: focused→smile / smile both / focused→closed-eye joyful / neutral both / custom
- Optional outfit override and free-text adjustments
- Strict character/exercise fidelity toggle (default ON)
- Copy Prompt and Generate Image actions where supported

## Visual defaults
Landscape PNG, 2:1, target 1774 × 887 px. Pure white background, blue mat only when relevant. Two equal poses left=start, right=end; no dividers. Same camera, character, outfit and proportions in both. Clean bold dark outlines, smooth flat color, minimal cel shading, simple arrows. Physically plausible joints, correct band anchoring, full equipment and limbs visible unless custom crop overrides. No text, captions, labels, watermark, complex lighting, fine textures or decorative base strip. Explicit user adjustments override conflicting defaults.

## Exercise registry
| ID | Exercise | Character | Prescription | Movement |
|---|---|---|---|---|
| S16 | Seated Band Plantar Flexion | LACS | 2×10–12 each side | Sit with inner leg folded and outer leg extended, band around outer forefoot; dorsiflex then point toes forward against resistance |
| S17 | Bird Dog | LACS | 2×6 each side, hold 3 sec | From all fours extend opposite arm and leg, stable spine and hips |
| S18 | Pallof Press | LACS | Future candidate only | Shoulder-width stance, soft knees, side-anchored band; press hands forward resisting rotation |
| M10 | Adductor Stretching (Butterfly) | SARYU | 2×10 slow | Seated upright, soles together, hands on knees; gently lower knees, no bouncing |
| M11 | Calf Stretching | SARYU | 20–30 sec ×2 each side | Facing wall, staggered stance, rear heel down, lean forward |
| M12 | Shoulder Stretching | SARYU | 10 reps, hold 3 sec | Clasp hands behind back, straighten down, gently open chest without back arch; approved white sleeveless top and knees-to-head framing |

## Master prompt
```text
FITFLOW MW — OFFICIAL EXERCISE ASSET PROMPT V3
EXERCISE: {code} — {name}
CHARACTER: {character}
TRAINING: {dose}
REFERENCE PRIORITY: Standalone portrait defines character identity; approved exercise guides define simplified exercise art style. Do not copy obsolete outfits or portrait props.
CHARACTER DESIGN: {character_profile}
OUTFIT OVERRIDE: {outfit_override}
CAMERA: {camera_angle}; FACING: {direction}; FRAMING: {framing}
COMPOSITION: 2:1 landscape PNG, target 1774×887, {one_or_two_poses}. Same camera, scale and identity in both poses.
STEP 1 / START: {step1}
STEP 2 / END: {step2}
ARROWS: {arrows}
EXPRESSION: {expression}
USER ADJUSTMENTS: {additional_instructions}
STYLE: Pure white background, bold clean outlines, flat colors, minimal cel shading, physically correct form, no captions, watermark, fine textures, ornate lighting or decorative base strip.
STRICT FIDELITY: {strict}
```

## Usage
Fetch this file from `fermium39/Rehab-MWFlow/FitFlow_Prompt_Generator.md` and render a generator using these fields. For new exercises, user can provide code, name, character, dose and steps. Image generation does not automatically commit images or update the app.