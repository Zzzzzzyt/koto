/*
 * MIT License
 *
 * Copyright (c) 2021 Hell Hole Studios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.hhs.koto.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import com.esotericsoftware.kryo.Kryo
import com.hhs.koto.app.KotoApp
import com.hhs.koto.app.Options
import com.hhs.koto.stg.*
import ktx.collections.GdxArray
import ktx.collections.GdxMap
import ktx.collections.contains
import ktx.collections.set
import ktx.json.JsonSerializer
import java.util.*

var global = GdxMap<String, Any>()

object SystemFlag {
    var redirect: String? = null
    var redirectDuration: Float? = null
    var gamemode: GameMode? = null
    var replay: Replay? = null
    var checkpoint: CheckPoint? = null
    var sessionName: String? = null
    var difficulty: GameDifficulty? = null
    var player: String? = null
}

val json = Json().apply {
    setUsePrototypes(false)
    setOutputType(JsonWriter.OutputType.json)
    setSerializer(GameData::class.java, object : JsonSerializer<GameData> {
        override fun write(json: Json, value: GameData, type: Class<*>?) {
            writeObjectStart()
            writeFields(value)
            writeObjectEnd()
        }

        override fun read(json: Json, jsonValue: JsonValue, type: Class<*>?): GameData {
            val tmpMap = GdxMap<String, GdxMap<String, GameData.GameDataElement>>()
            var data: JsonValue? = jsonValue.getChild("data")
            while (data != null) {
                @Suppress("UNCHECKED_CAST")
                tmpMap[data.name] = json.readValue(
                    GdxMap::class.java,
                    GameData.GameDataElement::class.java,
                    data,
                ) as GdxMap<String, GameData.GameDataElement>
                data = data.next()
            }
            return GameData(
                jsonValue["playTime"].asDouble(),
                jsonValue["playCount"].asInt(),
                jsonValue["practiceTime"].asDouble(),
                jsonValue["practiceCount"].asInt(),
                jsonValue["deathCount"].asInt(),
                jsonValue["bombCount"].asInt(),
                jsonValue["clearCount"].asInt(),
                tmpMap,
            )
        }
    })
    setSerializer(GameData.GameDataElement::class.java, object : JsonSerializer<GameData.GameDataElement> {
        override fun write(json: Json, value: GameData.GameDataElement, type: Class<*>?) {
            writeObjectStart()
            writeFields(value)
            writeObjectEnd()
        }

        override fun read(json: Json, jsonValue: JsonValue, type: Class<*>?): GameData.GameDataElement {
            @Suppress("UNCHECKED_CAST")
            return GameData.GameDataElement(
                json.readValue(
                    GdxArray::class.java,
                    GameData.ScoreEntry::class.java,
                    jsonValue["score"],
                ) as GdxArray<GameData.ScoreEntry>,
                json.readValue(
                    GdxMap::class.java,
                    Long::class.java,
                    jsonValue["practiceHighScore"],
                ) as GdxMap<String, Long>,
                json.readValue(
                    GdxMap::class.java,
                    GameData.SpellEntry::class.java,
                    jsonValue["spell"],
                ) as GdxMap<String, GameData.SpellEntry>,
            )
        }
    })
    setSerializer(Locale::class.java, object : JsonSerializer<Locale> {
        override fun write(json: Json, value: Locale, type: Class<*>?) {
            json.writeArrayStart()
            json.writeValue(value.language)
            json.writeValue(value.country)
            json.writeValue(value.variant)
            json.writeArrayEnd()
        }

        override fun read(json: Json, jsonValue: JsonValue, type: Class<*>?): Locale {
            return Locale(
                jsonValue[0].asString(),
                jsonValue[1].asString(),
                jsonValue[2].asString(),
            )
        }
    })
    setSerializer(Date::class.java, object : JsonSerializer<Date> {
        override fun write(json: Json, value: Date, type: Class<*>?) {
            json.writeValue(value.time)
        }

        override fun read(json: Json, jsonValue: JsonValue, type: Class<*>?): Date {
            return Date(jsonValue.asLong())
        }
    })
}

private val prettyPrintSettings = JsonValue.PrettyPrintSettings().apply {
    outputType = JsonWriter.OutputType.json
    singleLineColumns = 80
    wrapNumericArrays = false
}

fun prettyPrintJson(obj: Any): String {
    return json.prettyPrint(obj, prettyPrintSettings)
}

fun prettyPrintJson(obj: Any, file: FileHandle) {
    file.writeString(prettyPrintJson(obj), false)
}

val kryo = Kryo().apply {
    register(Replay::class.java)
    register(Date::class.java)
    register(HashMap::class.java)
    register(ArrayList::class.java)
    register(BooleanArray::class.java)
    register(Replay.KeyChangeEvent::class.java)
    register(CheckPoint::class.java)
    register(FragmentCounter::class.java)
    register(GameMode::class.java)
    register(GameDifficulty::class.java)
}

lateinit var options: Options
lateinit var gameData: GameData
lateinit var app: KotoApp

fun safeDeltaTime() = clamp(Gdx.graphics.deltaTime, 0f, 0.1f)

fun exitApp() {
    app.callbacks.restartCallback(false)
    Gdx.app.exit()
}

fun restartApp() {
    app.callbacks.restartCallback(true)
    Gdx.app.exit()
}

fun loadOptions() {
    val tmpOptions = app.callbacks.loadOptions()
    if (tmpOptions != null) {
        options = tmpOptions
    } else {
        options = Options()
        ResolutionMode.findOptimal(Gdx.graphics.displayMode).apply(options)
        app.logger.info("Creating default options file...")
        saveOptions()
    }
}

fun saveOptions() {
    app.callbacks.saveOptions(options)
}

fun loadReplays(): GdxArray<Replay> {
    return app.callbacks.loadReplays()
}

fun saveReplay(replay: Replay) {
    app.callbacks.saveReplay(replay)
}

private var gameDataHash: Int = 0

fun loadGameData() {
    val tmpGameData = app.callbacks.loadGameData()
    if (tmpGameData != null) {
        gameData = tmpGameData
    } else {
        gameData = GameData()
        app.logger.info("Creating empty game data file...")
    }
    gameDataHash = gameData.hashCode()
    for (player in GameBuilder.players.safeKeys()) {
        if (!gameData.data.contains(player)) {
            gameData.data[player] = GdxMap()
        }
        val tmpMap = gameData.data[player]
        GameBuilder.usedDifficulties.forEach { difficulty ->
            if (!tmpMap.contains(difficulty.name)) {
                tmpMap[difficulty.name] = GameData.GameDataElement()
            }
            val tmpElement = tmpMap[difficulty.name]
            GameBuilder.spells.forEach { spell ->
                if (difficulty in spell.availableDifficulties) {
                    if (!tmpElement.spell.contains(spell.name)) {
                        tmpElement.spell[spell.name] = GameData.SpellEntry()
                    }
                }
            }
            GameBuilder.regularStages.forEach { stage ->
                if (difficulty in stage.availableDifficulties) {
                    if (!tmpElement.practiceHighScore.contains(stage.name)) {
                        tmpElement.practiceHighScore[stage.name] = 0L
                    }
                }
            }
        }
    }
    saveGameData()
}

fun saveGameData() {
    if (gameData.hashCode() == gameDataHash) return
    app.callbacks.saveGameData(gameData)
    gameDataHash = gameData.hashCode()
}

fun <T> GdxArray<T>.removeNull() {
    var j = 0
    for (i in 0 until size) {
        if (this[i] != null) {
            this[j] = this[i]
            j++
        }
    }
    truncate(j)
}

fun <Type> GdxArray<Type>.safeIterator() = Array.ArrayIterator(this)

fun <K, V> GdxMap<K, V>.safeEntries() = ObjectMap.Entries(this)

fun <K, V> GdxMap<K, V>.safeKeys() = ObjectMap.Keys(this)

fun <K, V> GdxMap<K, V>.safeValues() = ObjectMap.Values(this)

fun getTrueFPSMultiplier(fpsMultiplier: Int): Float {
    if (fpsMultiplier == 0) return 1f
    if (fpsMultiplier < 0) return 1f / -fpsMultiplier
    return fpsMultiplier.toFloat()
}

data class SplitDecimal(val integer: String, val fraction: String)

fun splitDecimal(value: Float, precision: Int = 2, includeDecimalPoint: Boolean = true): SplitDecimal {
    val text = String.format("%.${precision}f", value)
    return SplitDecimal(
        text.substring(0, text.length - precision - if (includeDecimalPoint) 0 else 1),
        text.substring(text.length - precision),
    )
}