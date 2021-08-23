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

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import kotlin.math.sqrt

const val SQRT2 = 1.41421356237309505f

fun clamp(value: Int, min: Int, max: Int) = MathUtils.clamp(value, min, max)

fun clamp(value: Long, min: Long, max: Long) = MathUtils.clamp(value, min, max)

fun clamp(value: Float, min: Float, max: Float) = MathUtils.clamp(value, min, max)

fun clamp(value: Double, min: Double, max: Double) = MathUtils.clamp(value, min, max)

fun safeMod(x: Int, mod: Int): Int {
    return if (x >= 0) {
        x % mod
    } else {
        (x % mod + mod) % mod
    }
}

fun safeMod(x: Float, mod: Float): Float {
    return if (x >= 0) {
        x % mod
    } else {
        (x % mod + mod) % mod
    }
}

fun normalizeAngle(angle: Float): Float = safeMod(angle, 360f)

@Suppress("ConvertTwoComparisonsToRangeCheck")
fun angleInRange(angle: Float, min: Float, max: Float): Boolean {
    val angle0 = normalizeAngle(angle)
    val min0 = normalizeAngle(min)
    val max0 = normalizeAngle(max)
    return if (min0 < max0) {
        min0 <= angle0 && angle0 <= max0
    } else {
        min0 <= angle0 || angle0 <= max0
    }
}

fun min(vararg x: Float): Float = x.minOrNull()!!

fun min(vararg x: Double): Double = x.minOrNull()!!

fun min(vararg x: Int): Int = x.minOrNull()!!

fun min(vararg x: Long): Long = x.minOrNull()!!

fun max(vararg x: Float): Float = x.maxOrNull()!!

fun max(vararg x: Double): Double = x.maxOrNull()!!

fun max(vararg x: Int): Int = x.maxOrNull()!!

fun max(vararg x: Long): Long = x.maxOrNull()!!

fun sqrt(x: Float): Float =
    sqrt(x.toDouble()).toFloat()

fun len2(x: Float, y: Float): Float =
    x * x + y * y

fun len(x: Float, y: Float): Float =
    sqrt(x * x + y * y)

fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val deltaX = x1 - x2
    val deltaY = y1 - y2
    return sqrt(deltaX * deltaX + deltaY * deltaY)
}

fun dist2(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val deltaX = x1 - x2
    val deltaY = y1 - y2
    return deltaX * deltaX + deltaY * deltaY
}

fun atan2(y: Float, x: Float): Float =
    normalizeAngle(MathUtils.atan2(y, x) * MathUtils.radiansToDegrees)

fun atan2(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    normalizeAngle(MathUtils.atan2(y2 - y1, x2 - x1) * MathUtils.radiansToDegrees)

fun sin(degrees: Float): Float = MathUtils.sinDeg(degrees)

fun cos(degrees: Float): Float = MathUtils.cosDeg(degrees)

fun tan(degrees: Float): Float = sin(degrees) / cos(degrees)

fun lerp(start: Float, end: Float, a: Float): Float {
    if (a < 0f) return start
    if (a > 1f) return end
    return (end - start) * a + start
}


fun smoothstep(start: Float, end: Float, a: Float): Float {
    if (a < 0f) return start
    if (a > 1f) return end
    return (end - start) * a * a * (3 - 2 * a) + start
}

private val tmpRectangle = Rectangle()
fun Rectangle.contains(x: Float, y: Float, rx: Float, ry: Float): Boolean {
    tmpRectangle.set(x - rx, y - ry, rx * 2, ry * 2)
    return contains(tmpRectangle)
}

fun Rectangle.overlaps(x: Float, y: Float, rx: Float, ry: Float): Boolean {
    tmpRectangle.set(x - rx, y - ry, rx * 2, ry * 2)
    return overlaps(tmpRectangle)
}

fun Rectangle.distanceTo(x: Float, y: Float): Float {
    val deltaX = max(this.x - x, 0f, x - this.x - width)
    val deltaY = max(this.y - y, 0f, y - this.y - height)
    return sqrt(deltaX * deltaX + deltaY * deltaY)
}