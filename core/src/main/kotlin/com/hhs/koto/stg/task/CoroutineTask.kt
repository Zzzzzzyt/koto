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

import com.hhs.koto.util.game
import kotlinx.coroutines.*
import ktx.collections.GdxMap
import ktx.collections.set
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineTask(block: suspend CoroutineScope.() -> Unit) : Task {
    companion object {
        val scope = CoroutineScope(CoroutineTaskDispatcher)
    }

    object CoroutineTaskDispatcher : MainCoroutineDispatcher() {
        override val immediate: MainCoroutineDispatcher = this
        private val map = GdxMap<CoroutineContext, Runnable>()

        fun tick(job: Job) {
            map[job].run()
        }

        fun remove(job: Job) {
            map.remove(job)
        }

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            map[context.job] = block
        }
    }

    class FrameCounterElement : AbstractCoroutineContextElement(FrameCounterElement) {
        companion object Key : CoroutineContext.Key<FrameCounterElement>

        var frame: Int = 0
    }

    private val frameCounter = FrameCounterElement()
    private val job = scope.launch(frameCounter, block = block)
    override val isComplete: Boolean
        get() = job.isCompleted

    override fun tick() {
        CoroutineTaskDispatcher.tick(job.job)
        if (isComplete) {
            CoroutineTaskDispatcher.remove(job.job)
        }
        frameCounter.frame++
    }
}

val CoroutineScope.frame: Int
    get() = coroutineContext[CoroutineTask.FrameCounterElement]!!.frame

suspend fun wait(frameCount: Int) {
    repeat(frameCount) {
        yield()
    }
}

suspend fun Task.waitForFinish() {
    while (!isComplete) {
        yield()
    }
}

fun task(block: suspend CoroutineScope.() -> Unit): Task {
    val task = CoroutineTask(block)
    game.tasks.addTask(task)
    return task
}