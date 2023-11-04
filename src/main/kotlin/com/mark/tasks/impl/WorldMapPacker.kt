package com.mark.tasks.impl

import com.github.ushort.tomlx.Toml
import io.blurite.api.utils.lookup
import io.blurite.cache.*
import io.blurite.cache.config.MapElementConfig
import io.blurite.cache.config.ObjectConfig
import io.blurite.cache.config.OverlayConfig
import io.blurite.cache.config.UnderlayConfig
import io.blurite.cache.map.FullMapDefinition
import io.blurite.cache.map.MapLocDefinition
import io.blurite.cache.worldmap.builder.test.WorldMap
import io.blurite.cache.worldmap.config.WorldMapConfig
import io.blurite.cache.worldmap.ground.MapsquareId
import io.blurite.cache.worldmap.providers.*
import io.blurite.cache.worldmap.utils.Coordinate
import io.blurite.tools.cache.packing.parser.ConfigParser
import io.blurite.tools.cache.packing.toml.worldmap.TomlWorldMap
import io.blurite.tools.cache.packing.toml.worldmap.WorldMapAreaBlock
import io.blurite.util.rasterizer.provider.*
import io.netty.buffer.ByteBuf
import java.io.IOException
import java.nio.file.Paths
import kotlinx.serialization.decodeFromString
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.openrs2.cache.Cache

/**
 * @author Kris | 22/08/2022
 */
class WorldMapPacker {
    val mapProvider = CachedMapProvider()

    fun pack(cacheWriter: CacheWriter) {
        val envVariables = System.getenv()
        val packSurface = envVariables["PACK_WORLDMAP_SURFACE"]?.toBoolean()
        val providers = Providers(
            provideCache(cacheWriter),
            provideTextures(),
            provideSprites(),
            provideFonts(),
            provideObjects(),
            mapProvider,
            provideOverlays(),
            provideMapElements(),
            provideGraphicsDefaults(),
            provideUnderlays(),
        )
        val config = WorldMapConfig()
        val worldMap = WorldMap(config)
        val blocks = try {
            readWorldMapBlocks()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        for (block in blocks.sortedBy { it.details.id }) {
            if (block.details.isMain) {
                if (packSurface != true) continue
            }
            if (worldMap.exists(providers, block.details.internalName)) {
                logger.info { "Updating ${block.details.displayName} map area." }
                worldMap.update(
                    lookup("worldmap", block.details.id),
                    providers,
                    detailsTransformer = { details ->
                        require(details.id == block.details.id)
                        require(details.internalName == block.details.internalName)
                        details.copy(
                            displayName = block.details.displayName,
                            origin = block.details.origin,
                            backgroundColour = block.details.backgroundColour,
                            zoom = block.details.zoom,
                            sections = details.sections + block.details.sections
                        )
                    },
                    labelsTransformer = { labels ->
                        labels + block.mapElements
                    }
                )
            } else {
                logger.info { "Adding ${block.details.displayName} map area." }
                worldMap.add(providers, block.details, block.mapElements)
            }
        }
    }

    private fun readWorldMapBlocks(): List<WorldMapAreaBlock> {
        val tomlMaps = mutableListOf<WorldMapAreaBlock>()
        val parentPath = Paths.get("data", "worldmap").toFile()
        val iterator = FileUtils.iterateFiles(parentPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        for (file in iterator) {
            if (file.extension != ConfigParser.CONFIG_EXTENSION) continue
            val tomlMap: TomlWorldMap = try {
                Toml.decodeFromString(file.readText())
            } catch (e: Exception) {
                println("Exception decoding ${file.name}")
                throw e
            }
            tomlMaps += tomlMap.build("worldmap." + file.nameWithoutExtension)
        }
        return tomlMaps
    }

    private fun provideMapElements(): MapElementConfigProvider {
        return object : MapElementConfigProvider {
            override fun getMapElement(id: Int): MapElement {
                val config = config<MapElementConfig>(id)
                return object : MapElement {
                    override val text: String? get() = config.text
                    override val textSize: Int get() = config.textSize
                    override val textColour: Int get() = config.textColour
                    override val graphic: Int get() = config.graphic
                    override val horizontalAlignment: Int get() = config.horizontalAlignment
                    override val verticalAlignment: Int get() = config.verticalAlignment
                }
            }
        }
    }

    private fun provideOverlays(): OverlayProvider {
        return object : OverlayProvider {
            override fun exists(id: Int): Boolean {
                return configOrNull<OverlayConfig>(id) != null
            }

            override fun getTextureId(id: Int): Int {
                return config<OverlayConfig>(id).texture
            }

            override fun getMinimapColour(id: Int): Int {
                return config<OverlayConfig>(id).minimapColour
            }

            override fun getTileColour(id: Int): Int {
                return config<OverlayConfig>(id).tileColour
            }

            override fun getHue(id: Int): Int {
                return config<OverlayConfig>(id).hue
            }

            override fun getSaturation(id: Int): Int {
                return config<OverlayConfig>(id).saturation
            }

            override fun getLightness(id: Int): Int {
                return config<OverlayConfig>(id).lightness
            }
        }
    }

    private fun provideTextures(): TextureProvider {
        val textures: TextureArchive by inject()
        return object : TextureProvider {
            override fun getHsl(textureId: Int): Int {
                return textures[textureId]?.hsl ?: return -1
            }
        }
    }

    private fun provideFonts(): FontMetricsProvider {
        val archive: FontArchive by inject()
        val fonts = archive.fontMetrics
        return object : FontMetricsProvider {
            override val verdana11FontId: Int = lookup("font.verdana_11pt_regular")
            override val verdana13FontId: Int = lookup("font.verdana_13pt_regular")
            override val verdana15FontId: Int = lookup("font.verdana_15pt_regular")
            override fun getFont(id: Int): FontMetrics? {
                val font = fonts[id] ?: return null
                return object : FontMetrics {
                    override val advances: IntArray = font.advances
                    override val kerning: ByteArray? = font.kerning
                    override val ascent: Int? = font.ascent
                }
            }
        }
    }

    private fun provideSprites(): SpriteProvider {
        val archive: SpriteArchive by inject()
        return CachedSpriteProvider(archive)
    }

    class CachedSpriteProvider(private val archive: SpriteArchive) : SpriteProvider {
        override val verdana11PtId: Int = lookup("sprite.verdana_11pt_regular")
        override val verdana13ptId: Int = lookup("sprite.verdana_13pt_regular")
        override val verdana15ptId: Int = lookup("sprite.verdana_15pt_regular")
        override fun getSpriteSheet(id: Int): SpriteSheet? {
            val element = archive.get(id) ?: return null
            val frames: Array<SpriteFrame> = Array(element.frames.size) { index ->
                val frame = element.frames[index]
                object : SpriteFrame {
                    override val xOffset: Int = frame.xOffset
                    override val yOffset: Int = frame.yOffset
                    override val innerWidth: Int = frame.innerWidth
                    override val innerHeight: Int = frame.innerHeight
                    override val pixels: IntArray = frame.pixels
                }
            }
            return object : SpriteSheet {
                override val width: Int = element.width
                override val height: Int = element.height
                override val frames: Array<SpriteFrame> = frames
            }
        }
    }

    private fun provideUnderlays(): UnderlayProvider {
        return object : UnderlayProvider {
            override fun getUnderlay(id: Int): Underlay? {
                val config = configOrNull<UnderlayConfig>(id) ?: return null
                return object : Underlay {
                    override val hue: Int get() = config.hue
                    override val hueMultiplier: Int get() = config.hueMultiplier
                    override val saturation: Int get() = config.saturation
                    override val lightness: Int get() = config.lightness
                }
            }
        }
    }

    private fun provideGraphicsDefaults(): GraphicsDefaultsProvider {
        val defaultsArchive by inject<GraphicsDefaultsArchive>()
        val default = defaultsArchive.defaults.single()
        return object : GraphicsDefaultsProvider {
            override fun getMapScenesGroup(): Int {
                return default.mapScenes
            }

            override fun getModIconsGroup(): Int {
                return default.modIcons
            }
        }
    }

    private fun provideObjects(): ObjectProvider {
        return object : ObjectProvider {
            override fun getMapSceneId(id: Int): Int {
                return config<ObjectConfig>(id).mapSceneId
            }

            override fun getMapIconId(id: Int): Int {
                return config<ObjectConfig>(id).mapIconId
            }

            override fun getBoundaryType(id: Int): Int {
                return config<ObjectConfig>(id).boundaryType
            }
        }
    }

    private fun readMapBuffers(mapsquareX: Int, mapsquareY: Int): Pair<ByteBuf, ByteBuf?>? {
        val cache by inject<Cache>()
        val mapGroupName = "m${mapsquareX}_$mapsquareY"
        val locGroupName = "l${mapsquareX}_$mapsquareY"
        if (!cache.exists(MapArchive.id, mapGroupName)) return null
        val mapFile = try {
            cache.read(MapArchive.id, mapGroupName, "")
        } catch (e: IOException) {
            return null
        }
        val locFile = try {
            if (!cache.exists(MapArchive.id, locGroupName)) null else cache.read(MapArchive.id, locGroupName, "")
        } catch (e: IOException) {
            null
        }
        return mapFile to locFile
    }

    class CachedMapProvider : MapProvider {
        data class Loc(override val id: Int, override val shape: Int, override val rotation: Int, override val coordinate: Coordinate) : WorldMapObject
        private val cache = mutableMapOf<MapsquareId, Mapsquare?>()

        fun storeBuf(mapsquareId: MapsquareId, mapBuffer: ByteBuf, locBuffer: ByteBuf?): Mapsquare {
            val fullMap = FullMapDefinition.decode(mapBuffer, mapsquareId.x, mapsquareId.y)
            val mapObjects = locBuffer?.let { MapLocDefinition.decodeBaseData(it) } ?: emptyList()
            mapBuffer.release()
            locBuffer?.release()
            val worldMapObjects = mapObjects.map { loc ->
                Loc(loc.id, loc.type, loc.orientation, Coordinate(loc.localX, loc.localY, loc.floor))
            }
            // Same thing here..
            val land = object : Landscape {
                override fun getUnderlayId(level: Int, x: Int, y: Int): Int {
                    return fullMap.underlayIds[level][x][y] - 1
                }

                override fun getOverlayId(level: Int, x: Int, y: Int): Int {
                    return fullMap.overlayIds[level][x][y] - 1
                }

                override fun getOverlayShape(level: Int, x: Int, y: Int): Int {
                    return fullMap.overlayPaths[level][x][y].toInt()
                }

                override fun getOverlayRotation(level: Int, x: Int, y: Int): Int {
                    return fullMap.overlayRotations[level][x][y].toInt()
                }

                override fun getFlags(level: Int, x: Int, y: Int): Int {
                    return fullMap.renderRules[level][x][y].toInt()
                }
            }
            val cacheMap = Mapsquare(land, worldMapObjects)
            cache[mapsquareId] = cacheMap
            return cacheMap
        }

        override fun getMap(mapsquareX: Int, mapsquareY: Int): Mapsquare? {
            val mapsquareId = MapsquareId(mapsquareX, mapsquareY)
            val cached = cache[mapsquareId]
            if (cached != null) return cached
            val (mapBuffer, locBuffer) = readMapBuffers(mapsquareX, mapsquareY) ?: return null
            return storeBuf(mapsquareId, mapBuffer, locBuffer)
        }

        private fun readMapBuffers(mapsquareX: Int, mapsquareY: Int): Pair<ByteBuf, ByteBuf?>? {
            val cache by inject<Cache>()
            val mapGroupName = "m${mapsquareX}_$mapsquareY"
            val locGroupName = "l${mapsquareX}_$mapsquareY"
            if (!cache.exists(MapArchive.id, mapGroupName)) return null
            val mapFile = try {
                cache.read(MapArchive.id, mapGroupName, "")
            } catch (e: IOException) {
                return null
            }
            val locFile = try {
                if (!cache.exists(MapArchive.id, locGroupName)) null else cache.read(MapArchive.id, locGroupName, "")
            } catch (e: IOException) {
                null
            }
            return mapFile to locFile
        }
    }

    private fun provideCache(cacheWriter: CacheWriter): CacheProvider {
        val cache = cacheWriter.serverCache
        val js5Cache = cacheWriter.js5Cache
        return object : CacheProvider() {
            override fun read(archive: Int, group: Int, file: Int): ByteBuf {
                return cache.read(archive, group, file)
            }

            override fun read(archive: Int, group: String, file: String): ByteBuf {
                return cache.read(archive, group, file)
            }

            override fun exists(archive: Int, group: Int, file: Int): Boolean {
                return cache.exists(archive, group, file)
            }

            override fun exists(archive: Int, group: String, file: String): Boolean {
                return cache.exists(archive, group, file)
            }

            override fun write(archive: Int, group: Int, file: Int, buf: ByteBuf) {
                require(buf.isReadable)
                cache.write(archive, group, file, buf)
                js5Cache.write(archive, group, file, buf)
            }

            override fun write(archive: Int, group: String, file: String, buf: ByteBuf) {
                require(buf.isReadable)
                cache.write(archive, group, file, buf)
                js5Cache.write(archive, group, file, buf)
            }

            override fun write(archive: Int, group: String, file: String, fileId: Int, buf: ByteBuf) {
                require(buf.isReadable)
                cache.write(archive, group, file, fileId, buf)
                js5Cache.write(archive, group, file, fileId, buf)
            }

            override fun list(archive: Int): List<Int> {
                return cache.list(archive).asSequence().map { it.id }.toList()
            }

            override fun list(archive: Int, group: Int): List<Int> {
                return cache.list(archive, group).asSequence().map { it.id }.toList()
            }

            override fun list(archive: Int, group: String): List<Int> {
                return cache.list(archive, group).asSequence().map { it.id }.toList()
            }
        }
    }
}