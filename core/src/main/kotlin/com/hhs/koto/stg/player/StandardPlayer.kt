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

package com.hhs.koto.stg.player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.hhs.koto.app.Config
import com.hhs.koto.stg.CircleCollision
import com.hhs.koto.stg.Collision
import com.hhs.koto.stg.IndexedActor
import com.hhs.koto.stg.task.frame
import com.hhs.koto.stg.task.task
import com.hhs.koto.stg.task.wait
import com.hhs.koto.util.*
import kotlinx.coroutines.yield
import kotlin.math.absoluteValue

open class StandardPlayer(
    val texture: StandardPlayerTexture,
    hitboxTexture: TextureRegion,
    hitRadius: Float,
    val speedHigh: Float,
    val speedLow: Float,
    val deathbombTime: Int,
    grazeRadius: Float = hitRadius * 10f,
    itemRadius: Float = hitRadius * 15f,
    val textureOriginX: Float = texture.texture.regionWidth.toFloat() / 2,
    val textureOriginY: Float = texture.texture.regionHeight.toFloat() / 2,
    val leftMargin: Float = 10f,
    val rightMargin: Float = 10f,
    val bottomMargin: Float = 20f,
    val topMargin: Float = 20f,
    val spawnX: Float = Config.w / 2 - Config.originX,
    val spawnY: Float = -Config.originY + 30f,
    override val z: Int = -300,
) : Player, Actor(), IndexedActor {
    val hitCollision = CircleCollision(hitRadius)
    val grazeCollision = CircleCollision(grazeRadius)
    val itemCollision = CircleCollision(itemRadius)
    var playerState = PlayerState.NORMAL
    var dx: Int = 0
    var dy: Int = 0
    var speed: Float = 0f
    var invulnerable: Boolean = false
    protected val effect = PlayerDeathEffect().apply {
        game.vfx.addEffectRegistered(this)
    }
    protected var subFrameTime: Float = 0f
    protected var respawnAnimationPercentage: Float = 0f
    protected var counter: Int = 0
    protected var hitbox = Sprite(hitboxTexture).apply {
        setOriginCenter()
        setSize(64f, 64f)
        alpha = 0f
    }

    companion object {
        val tempColor: Color = Color()
    }

    enum class PlayerState {
        NORMAL, DEATHBOMBING, BOMBING, RESPAWNING, RESPAWNED
    }

    init {
        setPosition(spawnX, spawnY)
        game.st += this
    }

    override fun act(delta: Float) {
        subFrameTime += delta
        hitbox.rotate(4f * delta)
        if (hitbox.rotation >= 360f) hitbox.rotation -= 360f
        super.act(delta)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.end()

        app.normalBatch.projectionMatrix = batch.projectionMatrix
        app.normalBatch.begin()

        tempColor.set(app.normalBatch.color)
        app.normalBatch.color = color
        app.normalBatch.color.a *= parentAlpha

        val tmpX: Float = if (playerState == PlayerState.RESPAWNING) {
            Interpolation.sine.apply(spawnX, spawnX, respawnAnimationPercentage)
        } else {
            clampX(x + speed * dx * subFrameTime)
        }
        val tmpY: Float = if (playerState == PlayerState.RESPAWNING) {
            Interpolation.sine.apply(spawnY - 80f, spawnY, respawnAnimationPercentage)
        } else {
            clampY(y + speed * dy * subFrameTime)
        }
        if (rotation != 0f || scaleX != 1f || scaleY != 1f) {
            app.normalBatch.draw(
                texture.texture,
                tmpX - textureOriginX,
                tmpY - textureOriginY,
                textureOriginX,
                textureOriginY,
                texture.texture.regionWidth.toFloat(),
                texture.texture.regionHeight.toFloat(),
                scaleX,
                scaleY,
                rotation,
            )
        } else {
            app.normalBatch.draw(
                texture.texture,
                tmpX - textureOriginX,
                tmpY - textureOriginY,
                texture.texture.regionWidth.toFloat(),
                texture.texture.regionHeight.toFloat(),
            )
        }
        app.normalBatch.color = tempColor

        hitbox.setPosition(
            clampX(x + speed * dx * subFrameTime) - hitbox.width / 2,
            clampY(y + speed * dy * subFrameTime) - hitbox.height / 2,
        )
        hitbox.draw(app.normalBatch, parentAlpha)
        hitbox.rotation = -hitbox.rotation
        hitbox.draw(app.normalBatch, parentAlpha)
        hitbox.rotation = -hitbox.rotation

        app.normalBatch.end()
        batch.begin()
    }

    override fun tick() {
        subFrameTime = 0f
        if (playerState != PlayerState.RESPAWNING) {
            if (keyPressed(options.keySlow)) {
                hitbox.alpha = clamp(hitbox.alpha + 0.1f, 0f, 1f)
            } else {
                hitbox.alpha = clamp(hitbox.alpha - 0.1f, 0f, 1f)
            }
        }
        if (playerState != PlayerState.RESPAWNING) {
            move()
        }
        if (playerState == PlayerState.NORMAL) {
            if (!invulnerable && !options.invulnerable) {
                var hit = false
                game.bullets.forEach {
                    if (Collision.collide(it.collision, it.x, it.y, hitCollision, x, y)) {
                        hit = true
                    }
                }
                if (hit) {
                    hit()
                    playerState = PlayerState.DEATHBOMBING
                    counter = deathbombTime
                }
            }
        }
        if (playerState == PlayerState.DEATHBOMBING) {
            if (counter <= 0) {
                playerState = PlayerState.RESPAWNING
                death()
            } else if (keyPressed(options.keyBomb)) {
                bomb(true)
                playerState = PlayerState.BOMBING
            }
        } else if (playerState == PlayerState.NORMAL) {
            if (keyPressed(options.keyBomb)) {
                bomb(false)
                playerState = PlayerState.BOMBING
            }
        }
        if (counter > 0) counter--
    }

    open fun move() {
        speed = if (keyPressed(options.keySlow)) {
            speedLow
        } else {
            speedHigh
        }
        dx = 0
        dy = 0
        if (keyPressed(options.keyLeft)) {
            dx--
        }
        if (keyPressed(options.keyRight)) {
            dx++
        }
        if (keyPressed(options.keyDown)) {
            dy--
        }
        if (keyPressed(options.keyUp)) {
            dy++
        }
        if (dx.absoluteValue > 0 && dy.absoluteValue > 0) {
            speed /= SQRT2
        }
        x += speed * dx
        y += speed * dy
        x = clampX(x)
        y = clampY(y)
        texture.update(dx)
    }

    open fun bomb(isDeathBomb: Boolean) {
        invulnerable = true
        SE.play("bomb")
        task {
            game.bullets.forEach {
                it.destroy()
            }
            wait(290)
            playerState = PlayerState.NORMAL
            invulnerable = false
        }
    }

    open fun hit() {
        SE.play("pldead")
    }

    open fun death() {
        invulnerable = true
        task {
            respawnAnimationPercentage = 0f
            effect.start(x, y)
            wait(30)
            repeat(70) {
                respawnAnimationPercentage += 1 / 70f
                yield()
            }
            effect.end()
            x = spawnX
            y = spawnY
            respawnAnimationPercentage = 0f
            playerState = PlayerState.RESPAWNED
            repeat(190) {
                color = if (frame % 6 <= 1) {
                    Color.BLUE
                } else {
                    Color.WHITE
                }
                yield()
            }
            color = Color.WHITE
            invulnerable = false
            playerState = PlayerState.NORMAL
        }
    }

    fun clampX(x: Float): Float {
        if (playerState == PlayerState.RESPAWNING) return x
        return clamp(x, leftMargin - Config.originX, Config.w - Config.originX - rightMargin)
    }


    fun clampY(y: Float): Float {
        if (playerState == PlayerState.RESPAWNING) return y
        return clamp(y, bottomMargin - Config.originY, Config.h - Config.originY - topMargin)
    }

}