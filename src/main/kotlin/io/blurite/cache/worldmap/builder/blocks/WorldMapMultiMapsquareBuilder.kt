package io.blurite.cache.worldmap.builder.blocks

import io.blurite.cache.worldmap.MapsquareMultiSection
import io.blurite.cache.worldmap.WorldMapMapsquare
import io.blurite.cache.worldmap.providers.MapProvider
import io.blurite.cache.worldmap.providers.ObjectProvider

/**
 * @author Kris | 21/08/2022
 */
class WorldMapMultiMapsquareBuilder(private val section: MapsquareMultiSection) : WorldMapBlockBuilder<WorldMapMapsquare> {
    override fun build(mapProvider: MapProvider, objectProvider: ObjectProvider): List<WorldMapMapsquare> {
        val initial = (section.mapsquareSourceMaxX.inc() - section.mapsquareSourceMinX) * (section.mapsquareSourceMaxY.inc() - section.mapsquareSourceMinY)
        val list = ArrayList<WorldMapMapsquare>(initial)
        for (x in section.mapsquareSourceMinX..section.mapsquareSourceMaxX) {
            for (y in section.mapsquareSourceMinY..section.mapsquareSourceMaxY) {
                val mapsquare = generateMapsquare(
                    mapProvider,
                    objectProvider,
                    section.level,
                    section.levelsCount,
                    x,
                    y,
                    section.mapsquareDestinationMinX + (x - section.mapsquareSourceMinX),
                    section.mapsquareDestinationMinY + (y - section.mapsquareSourceMinY),
                )
                if (mapsquare != null) list += mapsquare
            }
        }
        return list
    }
}
