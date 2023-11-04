package io.blurite.util.rasterizer.sprite

import io.blurite.util.rasterizer.provider.GraphicsDefaultsProvider
import io.blurite.util.rasterizer.provider.SpriteProvider

/**
 * @author Kris | 22/08/2022
 */
data class MapSceneSprites(val indexedSprites: List<SingleFrameSprite>) {
    companion object : IndexedSpriteGroup {
        fun build(graphicsDefaultsProvider: GraphicsDefaultsProvider, spriteProvider: SpriteProvider): MapSceneSprites {
            return MapSceneSprites(buildIndexedSprites(graphicsDefaultsProvider.getMapScenesGroup(), spriteProvider))
        }
    }
}
