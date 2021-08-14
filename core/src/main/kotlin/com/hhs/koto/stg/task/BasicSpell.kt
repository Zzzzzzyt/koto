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

package com.hhs.koto.stg.task

import com.hhs.koto.stg.addEnemy
import com.hhs.koto.stg.drawable.BasicBoss
import com.hhs.koto.stg.drawable.Boss
import com.hhs.koto.util.findBoss
import com.hhs.koto.util.game
import kotlinx.coroutines.yield

abstract class BasicSpell<T : Boss>(protected val bossClass: Class<T>) : SpellBuilder {
    abstract val health: Float
    abstract val maxTime: Int
    abstract fun spell(): Task
    open fun terminate(): Task = EmptyTask()
    lateinit var boss: T
    var t: Int = 0

    override fun build(): Task {
        val spellTask = spell()
        return ParallelTask(
            CoroutineTask {
                boss = findBoss(bossClass)!!
                while (true) {
                    if (t >= maxTime || boss.healthBar.currentSegmentDepleted()) {
                        if (spellTask.alive) spellTask.kill()
                        break
                    }
                    yield()
                    t++
                }
            },
            SequenceTask(
                spellTask,
                RunnableTask {
                    game.bullets.forEach {
                        it.destroy()
                    }
                    boss.healthBar.nextSegment()
                },
                terminate(),
            ),
        )
    }

    fun <T : BasicBoss> buildSpellPractice(bossBuilder: () -> T): Task = CoroutineTask {
        val boss = bossBuilder()
        addEnemy(boss)
        boss.healthBar.addSpell(this@BasicSpell)
        attachAndWait(boss.creationTask())
        attachAndWait(boss.createSpellBackground())
        attachAndWait(this@BasicSpell.build())
        attachAndWait(boss.removeSpellBackground())
    }
}