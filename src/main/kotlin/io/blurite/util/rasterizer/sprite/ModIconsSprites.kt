package io.blurite.util.rasterizer.sprite

import io.blurite.util.rasterizer.provider.GraphicsDefaultsProvider
import io.blurite.util.rasterizer.provider.SpriteProvider

/**
 * @author Kris | 22/08/2022
 */
class ModIconsSprites(val indexedSprites: List<SingleFrameSprite>) {
    companion object : IndexedSpriteGroup {
        fun build(graphicsDefaultsProvider: GraphicsDefaultsProvider, spriteProvider: SpriteProvider): ModIconsSprites {
            return ModIconsSprites(buildIndexedSprites(graphicsDefaultsProvider.getModIconsGroup(), spriteProvider))
        }
    }
}
