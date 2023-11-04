package io.blurite.cache.worldmap.builder.blocks

import io.blurite.cache.worldmap.WorldMapZone
import io.blurite.cache.worldmap.WorldMapZoneData
import io.blurite.cache.worldmap.WorldMapZoneGeography
import io.blurite.cache.worldmap.ZoneMultiSection
import io.blurite.cache.worldmap.providers.MapProvider
import io.blurite.cache.worldmap.providers.ObjectProvider

/**
 * @author Kris | 21/08/2022
 */
class WorldMapMultiZoneBuilder(private val section: ZoneMultiSection) : WorldMapBlockBuilder<WorldMapZone> {
    override fun build(mapProvider: MapProvider, objectProvider: ObjectProvider): List<WorldMapZone> {
        val map = mapProvider.getMap(section.mapsquareSourceX, section.mapsquareSourceY) ?: return emptyList()
        val initial = (section.zoneSourceMaxX.inc() - section.zoneSourceMinX) * (section.zoneSourceMaxY.inc() - section.zoneSourceMinY)
        val zones = ArrayList<WorldMapZone>(initial)
        val minX = section.zoneSourceMinX shl 3
        val minY = section.zoneSourceMinY shl 3
        val maxX = section.zoneSourceMaxX.inc() shl 3
        val maxY = section.zoneSourceMaxY.inc() shl 3
        val (underlays, overlays, shapes, rotations, decorations) = map.computeGeography(
            section.level,
            section.levelsCount,
            objectProvider,
            minX until maxX,
            minY until maxY,
            (section.zoneDestinationMinX shl 3) - minX,
            (section.zoneDestinationMinY shl 3) - minY
        )
        for (zoneSourceX in section.zoneSourceMinX..section.zoneSourceMaxX) {
            for (zoneSourceY in section.zoneSourceMinY..section.zoneSourceMaxY) {
                val zoneDestinationX = section.zoneDestinationMinX + (zoneSourceX - section.zoneSourceMinX)
                val zoneDestinationY = section.zoneDestinationMinY + (zoneSourceY - section.zoneSourceMinY)
                val data = WorldMapZoneData(
                    section.level,
                    section.levelsCount,
                    section.mapsquareSourceX,
                    section.mapsquareSourceY,
                    zoneSourceX,
                    zoneSourceY,
                    section.mapsquareDestinationX,
                    section.mapsquareDestinationY,
                    zoneDestinationX,
                    zoneDestinationY,
                    -1,
                    -1
                )
                val geography = WorldMapZoneGeography(
                    section.mapsquareDestinationX,
                    section.mapsquareDestinationY,
                    zoneDestinationX,
                    zoneDestinationY,
                    underlays,
                    overlays,
                    shapes,
                    rotations,
                    decorations,
                )
                zones += WorldMapZone(data, geography)
            }
        }
        return zones
    }
}
