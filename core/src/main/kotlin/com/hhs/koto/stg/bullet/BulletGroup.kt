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

package com.hhs.koto.stg.bullet

import com.hhs.koto.stg.task.CoroutineTask
import com.hhs.koto.util.game
import kotlinx.coroutines.CoroutineScope
import ktx.collections.GdxArray

class BulletGroup {
    val bullets = GdxArray<Bullet>()

    fun task(block: suspend CoroutineScope.() -> Unit): CoroutineTask {
        val task = CoroutineTask(bulletGroup = this, block = block)
        game.tasks.addTask(task)
        return task
    }

    inline fun forEach(action: (Bullet) -> Unit) {
        for (i in 0 until bullets.size) {
            if (bullets[i].alive) {
                action(bullets[i])
            } else {
                bullets[i] = null
            }
        }
    }

    fun taskEach(block: suspend CoroutineScope.() -> Unit) {
        for (i in 0 until bullets.size) {
            if (bullets[i].alive) {
                bullets[i].task(i, block)
            } else {
                bullets[i] = null
            }
        }
    }
}