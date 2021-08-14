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

package com.hhs.koto.stg

import com.hhs.koto.stg.task.SpellBuilder
import com.hhs.koto.stg.task.StageBuilder
import com.hhs.koto.stg.task.Task
import com.hhs.koto.stg.task.TaskBuilder
import com.hhs.koto.util.KotoRuntimeException
import com.hhs.koto.util.SystemFlag
import com.hhs.koto.util.game
import ktx.collections.GdxArray
import ktx.collections.GdxMap
import ktx.collections.filter

object GameBuilder {
    lateinit var regularGame: TaskBuilder
    lateinit var extraGame: TaskBuilder
    val stages = GdxArray<StageBuilder>()
    val spells = GdxArray<SpellBuilder>()
    val players = GdxMap<String, () -> Player>()

    fun build(): KotoGame {
        game = when (SystemFlag.gamemode) {
            GameMode.STORY -> buildRegularGame()
            GameMode.EXTRA -> buildExtraGame()
            GameMode.STAGE_PRACTICE -> buildStagePractice(
                SystemFlag.name ?: throw KotoRuntimeException("name flag is null!"),
                SystemFlag.difficulty ?: throw KotoRuntimeException("difficulty flag is null!"),
            )
            GameMode.SPELL_PRACTICE -> buildSpellPractice(
                SystemFlag.name ?: throw KotoRuntimeException("name flag is null!"),
                SystemFlag.difficulty ?: throw KotoRuntimeException("difficulty flag is null!"),
            )
            null -> throw KotoRuntimeException("gameMode flag is null!")
        }
        val playerName = SystemFlag.player ?: throw KotoRuntimeException("player flag is null!")
        game.player = (players[playerName] ?: throw KotoRuntimeException("player \"$playerName\" not found!"))()
        return game
    }

    fun buildRegularGame(): KotoGame = buildGameWithTask(regularGame.build())

    fun buildExtraGame(): KotoGame = buildGameWithTask(extraGame.build())

    fun getAvailableStages(): GdxArray<StageBuilder> {
        if (SystemFlag.difficulty == null) throw KotoRuntimeException("difficulty flag is null!")
        return stages.filter { SystemFlag.difficulty in it.availableDifficulties }
    }

    fun getAvailableSpells(): GdxArray<SpellBuilder> {
        if (SystemFlag.difficulty == null) throw KotoRuntimeException("difficulty flag is null!")
        return spells.filter { SystemFlag.difficulty in it.availableDifficulties }
    }


    fun buildStagePractice(name: String, difficulty: GameDifficulty): KotoGame {
        val stageBuilder = stages.find { it.name == name } ?: throw KotoRuntimeException("Stage \"$name\" not found!")
        if (difficulty !in stageBuilder.availableDifficulties) {
            throw KotoRuntimeException("Stage \"$name\" does not support difficulty \"$difficulty\"")
        }
        return buildGameWithTask(stageBuilder.build())
    }

    fun buildSpellPractice(name: String, difficulty: GameDifficulty): KotoGame {
        val spellBuilder =
            spells.find { it.name == name } ?: throw KotoRuntimeException("Spell \"$name\" not found!")
        if (difficulty !in spellBuilder.availableDifficulties) {
            throw KotoRuntimeException("Spell \"$name\" does not support difficulty \"$difficulty\"")
        }
        return buildGameWithTask(spellBuilder.buildSpellPractice())
    }

    fun buildGameWithTask(task: Task): KotoGame {
        val game = KotoGame()
        game.tasks.addTask(task)
        return game
    }

}