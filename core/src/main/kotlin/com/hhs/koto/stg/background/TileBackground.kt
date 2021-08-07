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

package com.hhs.koto.stg.background

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils.ceil
import com.badlogic.gdx.math.MathUtils.floor
import com.hhs.koto.app.Config
import com.hhs.koto.stg.Drawable
import com.hhs.koto.util.safeMod
import com.hhs.koto.util.tmpColor

class TileBackground(
    val texture: TextureRegion,
    override val zIndex: Int,
    val speedX: Float = 0f,
    val speedY: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    val tileWidth: Float = texture.regionWidth.toFloat(),
    val tileHeight: Float = texture.regionHeight.toFloat(),
    override val x: Float = 0f,
    override val y: Float = 0f,
    val width: Float = Config.fw.toFloat(),
    val height: Float = Config.fh.toFloat(),
    val color: Color = Color.WHITE
) : Drawable {
    override var alive: Boolean = true
    val countX: Int = floor(width / tileWidth) + 1
    val countY: Int = floor(height / tileHeight) + 1

    init {
        offsetX = safeMod(offsetX, tileWidth)
        offsetY = safeMod(offsetY, tileHeight)
    }

    override fun draw(batch: Batch, parentAlpha: Float, subFrameTime: Float) {
        val tmpOffsetX = safeMod(offsetX + speedX * subFrameTime, tileWidth)
        val tmpOffsetY = safeMod(offsetY + speedY * subFrameTime, tileHeight)
        val startIndexX: Int = if (tmpOffsetX > 0f) {
            -1
        } else {
            0
        }
        val endIndexX: Int = ceil((width - tmpOffsetX) / tileWidth)
        val startIndexY: Int = if (tmpOffsetY > 0f) {
            -1
        } else {
            0
        }
        val endIndexY: Int = ceil((height - tmpOffsetY) / tileHeight)
        tmpColor.set(batch.color)
        batch.color = color
        for (xIndex in startIndexX until endIndexX) {
            for (yIndex in startIndexY until endIndexY) {
                batch.draw(
                    texture,
                    x + tmpOffsetX + xIndex * tileWidth,
                    y + tmpOffsetY + yIndex * tileHeight,
                    tileWidth,
                    tileHeight,
                )
            }
        }
        batch.color = tmpColor
    }

    override fun tick() {
        offsetX = safeMod(offsetX + speedX, tileWidth)
        offsetY = safeMod(offsetY + speedY, tileHeight)
    }
}