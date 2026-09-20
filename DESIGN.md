# Design System: Luna Liquid Glass

## Visual World: Frosted Liquid Glass & Tactile Hardware
Luna's aesthetic is defined by physical frosted glass layers floating above rich textural backdrops. Inspired by high-end luxury audio gear and translucent optics, interfaces have physical depth, soft refraction, and glowing specular highlights.

## Color Foundation
- **Light Mode Canvas**: `#E8E8E8` (rich, warm, clean backdrop)
- **Dark Mode Canvas**: `#121214`
- **AMOLED Canvas**: `#000000` (true black with zero emissive pixel cost)
- **Surfaces**:
  - Cards: `#FFFFFF` in light mode with no harsh stroke boundaries (`Color.Transparent` border when unselected).
  - Floating Glass Elements: `Color(0xF0FFFFFF).copy(alpha = 0.82f)` in light mode; `Color(0xD91C1C22).copy(alpha = 0.85f)` in dark mode.
- **Accents**:
  - Primary Accent: Charcoal Black `#18181B` in light mode, Clean White `#FFFFFF` in dark mode.
  - Liquid Bubble Accent: Warm honey/amber glow (`Color(0xFFFFAE19)` to `Color(0xFFFFD175)`) evoking warm incandescent backlights inside frosted glass.

## Liquid Glass Physical Tokens
1. **Specular Rim Highlights**:
   - Polished edge reflection gradient:
     `Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.20f), Color.Transparent))`
   - Simulates directional overhead light catching the curved bevel of cast glass.
2. **Diffuse Ambient Shadows**:
   - Soft multi-stop elevation shadows (`12.dp - 16.dp`) with subtle warm/charcoal tint to ground floating glass bars above the content plane.
3. **Internal Refraction Sheen**:
   - Top-to-bottom subtle luminosity gradient creating volumetric optical density.
4. **Organic Morphing Bubble Indicator**:
   - Low-bouncy spring physics (`dampingRatio = 0.72f`, `stiffness = Spring.StiffnessMediumLow`).
   - Dynamic width expansion during fast sliding transitions (liquid stretch & catch-up).

## Typography & Iconography
- **Typography**: Clean humanist typography with bold geometric numerals and crisp micro-caps section headers (`letterSpacing = 1.2.sp`).
- **Icons**: Custom `UntitledIcons` suite with consistent 2.0dp stroke, round caps, round joins, and matched visual weight across 24x24 viewports.
