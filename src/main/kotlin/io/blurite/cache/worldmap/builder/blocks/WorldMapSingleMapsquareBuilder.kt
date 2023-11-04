package io.blurite.cache.worldmap.builder.blocks

import io.blurite.cache.worldmap.MapsquareSingleSection
import io.blurite.cache.worldmap.WorldMapMapsquare
import io.blurite.cache.worldmap.providers.MapProvider
import io.blurite.cache.worldmap.providers.ObjectProvider

/**
 * @author Kris | 21/08/2022
 */
class WorldMapSingleMapsquareBuilder(private val section: MapsquareSingleSection) : WorldMapBlockBuilder<WorldMapMapsquare> {
    override fun build(mapProvider: MapProvider, objectProvider: ObjectProvider): List<WorldMapMapsquare> {
        val mapsquare = generateMapsquare(
            mapProvider,
            objectProvider,
            section.level,
            section.levelsCount,
            section.mapsquareSourceX,
            section.mapsquareSourceY,
            section.mapsquareDestinationX,
            section.mapsquareDestinationY
        )
        return if (mapsquare == null) emptyList() else listOf(mapsquare)
    }
}
