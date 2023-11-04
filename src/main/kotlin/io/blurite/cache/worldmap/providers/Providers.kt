package io.blurite.cache.worldmap.providers

import io.blurite.util.rasterizer.provider.FontMetricsProvider
import io.blurite.util.rasterizer.provider.GraphicsDefaultsProvider
import io.blurite.util.rasterizer.provider.SpriteProvider

/**
 * @author Kris | 21/08/2022
 */
data class Providers(
    val cacheProvider: CacheProvider,
    val textureProvider: TextureProvider,
    val spriteProvider: SpriteProvider,
    val fontMetricsProvider: FontMetricsProvider,
    val objectProvider: ObjectProvider,
    val mapProvider: MapProvider,
    val overlayProvider: OverlayProvider,
    val mapElementProvider: MapElementConfigProvider,
    val graphicsDefaultsProvider: GraphicsDefaultsProvider,
    val underlayProvider: UnderlayProvider,
)
