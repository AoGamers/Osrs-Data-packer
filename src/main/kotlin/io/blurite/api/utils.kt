package io.blurite.api

object utils {

    val undergroundMap = mapOf(
        "main" to 0,
        "ancient_cavern" to 1,
        "ardougne_underground" to 2,
        "asgarnia_ice_dungeon" to 3,
        "braindeath_island" to 4,
        "dorgeshkaan" to 5,
        "dwarven_mines" to 6,
        "godwars" to 7,
        "ghorrock_prison" to 8,
        "karamja_underground" to 9,
        "keldagrim" to 10,
        "miscellania_underground" to 11,
        "misthalin_underground" to 12,
        "mole" to 13,
        "morytania_underground" to 14,
        "mosleharmless_cave" to 15,
        "ourania" to 16,
        "slayer_cave" to 17,
        "sos" to 18,
        "stronghold_underground" to 19,
        "taverley_underground" to 20,
        "tolna" to 21,
        "troll_stronghold" to 22,
        "tzhaar_area" to 23,
        "undead_dungeon" to 24,
        "waterbirth" to 25,
        "wilderness_dungeons" to 26,
        "yanille_underground" to 27,
        "zanaris" to 28,
        "prifddinas" to 29,
        "fossil_underground" to 30,
        "feldip_underground" to 31,
        "kourend_underground" to 32,
        "kebos_underground" to 33,
        "prifddinas_underground" to 34,
        "grand_library" to 35,
        "br_default" to 36,
        "tutorial_2" to 37,
        "br_dark_varrock" to 38,
        "camdozaal" to 39,
        "the_abyss" to 40,
        "lassar_undercity" to 41,
        "desert_underground" to 42
    )

    fun lookup(key : String): Int {
        return undergroundMap[key] ?: 0
    }

}