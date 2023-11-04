package io.blurite.cache.worldmap.rasterizer.utils

import io.blurite.util.rasterizer.Font

/**
 * @author Kris | 22/08/2022
 */
data class WorldMapTextLabel(
    val name: String,
    val width: Int,
    val height: Int,
    val size: WorldMapLabelSize,
    val font: Font,
)
