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

package com.hhs.koto.app.screen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.hhs.koto.app.Options
import com.hhs.koto.app.ui.ConstrainedGrid
import com.hhs.koto.app.ui.GridButton
import com.hhs.koto.app.ui.ScrollingGrid
import com.hhs.koto.util.*
import kotlin.math.roundToInt

class OptionsScreen : BasicScreen("mus/E.0120.ogg", getRegion("bg/title.png")) {
    val grid = ConstrainedGrid(Rectangle(-2048f, 64f, 4096f, 832f))
    val musicVolume = ScrollingGrid(0f, 0f, cycle = false)
    val SEVolume = ScrollingGrid(0f, 0f, cycle = false, gridY = 1)
    val Vsync = ScrollingGrid(0f, 0f, cycle = false, gridY = 2)
    var oldOptions: Options? = null

    init {
        st.addActor(grid)
        grid.add(GridButton("Music Vol", 32, 940f, 280f, 200f, 40f, 0, 0, triggerSound = null))
        musicVolume.setPosition(1150f, 280f)
        for (i in 0..20) {
            val button: GridButton = musicVolume.add(
                GridButton(
                    (i * 5).toString() + "%",
                    32,
                    0f,
                    0f,
                    200f,
                    40f,
                    i,
                    0,
                    triggerSound = null
                )
            ) as GridButton
            button.activeAction = {
                Actions.parallel(
                    Actions.sequence(
                        Actions.show(),
                        Actions.color(Color.WHITE),
                        Actions.moveTo(button.staticX + 2, button.staticY, 0.03f, Interpolation.sine),
                        Actions.moveTo(button.staticX - 4, button.staticY, 0.06f, Interpolation.sine),
                        Actions.moveTo(button.staticX, button.staticY, 0.03f, Interpolation.sine)
                    ),
                    Actions.forever(
                        Actions.sequence(
                            Actions.color(Color(0.9f, 0.9f, 0.9f, 1f), 0.5f),
                            Actions.color(Color.WHITE, 0.5f)
                        )
                    ),
                    Actions.run {
                        options.musicVolume = i / 20f
                        BGM.setVolume(i / 20f)
                    }
                )
            }
            button.inactiveAction = { Actions.hide() }
        }
        musicVolume.select(clamp((options.musicVolume * 20).roundToInt(), 0, 20), 0, true)
        musicVolume.update()
        input.addProcessor(musicVolume)
        grid.add(musicVolume)

        val tmpButton1 = GridButton("S.E. Vol", 32, 940f, 240f, 200f, 40f, 0, 1, triggerSound = null)
        tmpButton1.activeAction = tmpButton1.getActivateAction({
            Actions.forever(
                Actions.sequence(
                    Actions.delay(1.5f),
                    Actions.run { SE.play("pldead") }
                )
            )
        })
        grid.add(tmpButton1)
        SEVolume.setPosition(1150f, 240f)
        for (i in 0..20) {
            val tmpButton2 = SEVolume
                .add(
                    GridButton(
                        (i * 5).toString() + "%",
                        32,
                        0f,
                        0f,
                        240f,
                        40f,
                        i,
                        0,
                        triggerSound = null
                    )
                ) as GridButton
            tmpButton2.activeAction = {
                Actions.parallel(
                    Actions.sequence(
                        Actions.show(),
                        Actions.color(Color.WHITE),
                        Actions.moveTo(tmpButton2.staticX + 2, tmpButton2.staticY, 0.03f, Interpolation.sine),
                        Actions.moveTo(tmpButton2.staticX - 4, tmpButton2.staticY, 0.06f, Interpolation.sine),
                        Actions.moveTo(tmpButton2.staticX, tmpButton2.staticY, 0.03f, Interpolation.sine)
                    ),
                    Actions.forever(
                        Actions.sequence(
                            Actions.color(Color(0.9f, 0.9f, 0.9f, 1f), 0.5f),
                            Actions.color(Color.WHITE, 0.5f)
                        )
                    ),
                    Actions.run { options.SEVolume = i / 20f }
                )
            }
            tmpButton2.inactiveAction = { Actions.hide() }
        }
        SEVolume.select(clamp((options.SEVolume * 20).roundToInt(), 0, 20), 0, true)
        SEVolume.update()
        input.addProcessor(SEVolume)
        grid.add(SEVolume)

        grid.add(GridButton("Vsync", 32, 940f, 200f, 200f, 40f, 0, 2, triggerSound = null))
        Vsync.setPosition(1150f, 200f)
        val VsyncDisableButton: GridButton =
            Vsync.add(GridButton("No", 32, 0f, 0f, 200f, 40f, 0, 0, triggerSound = null)) as GridButton
        VsyncDisableButton.activeAction = {
            Actions.parallel(
                Actions.sequence(
                    Actions.show(),
                    Actions.color(Color.WHITE),
                    Actions.moveTo(
                        VsyncDisableButton.staticX + 2,
                        VsyncDisableButton.staticY,
                        0.03f,
                        Interpolation.sine
                    ),
                    Actions.moveTo(
                        VsyncDisableButton.staticX - 4,
                        VsyncDisableButton.staticY,
                        0.06f,
                        Interpolation.sine
                    ),
                    Actions.moveTo(VsyncDisableButton.staticX, VsyncDisableButton.staticY, 0.03f, Interpolation.sine)
                ),
                Actions.forever(
                    Actions.sequence(
                        Actions.color(Color(0.9f, 0.9f, 0.9f, 1f), 0.5f),
                        Actions.color(Color.WHITE, 0.5f)
                    )
                ),
                Actions.run {
                    options.vsyncEnabled = false
                }
            )
        }
        val VsyncEnableButton: GridButton =
            Vsync.add(GridButton("Yes", 32, 0f, 0f, 200f, 40f, 1, 0, triggerSound = null)) as GridButton
        VsyncEnableButton.activeAction = {
            Actions.parallel(
                Actions.sequence(
                    Actions.show(),
                    Actions.color(Color.WHITE),
                    Actions.moveTo(VsyncEnableButton.staticX + 2, VsyncEnableButton.staticY, 0.03f, Interpolation.sine),
                    Actions.moveTo(VsyncEnableButton.staticX - 4, VsyncEnableButton.staticY, 0.06f, Interpolation.sine),
                    Actions.moveTo(VsyncEnableButton.staticX, VsyncEnableButton.staticY, 0.03f, Interpolation.sine)
                ),
                Actions.forever(
                    Actions.sequence(
                        Actions.color(Color(0.9f, 0.9f, 0.9f, 1f), 0.5f),
                        Actions.color(Color.WHITE, 0.5f)
                    )
                ),
                Actions.run {
                    options.vsyncEnabled = true
                }
            )
        }
        VsyncEnableButton.inactiveAction = { Actions.hide() }
        VsyncDisableButton.inactiveAction = { Actions.hide() }
        Vsync.select(if (options.vsyncEnabled) 1 else 0, 0, true)
        Vsync.update()
        input.addProcessor(Vsync)
        grid.add(Vsync)

        grid.add(GridButton("Key Config", 32, 940f, 160f, 200f, 40f, 0, 2)).disable()
        grid.add(GridButton("Default", 32, 940f, 120f, 200f, 40f, 0, 3) {
            options = Options()
            BGM.setVolume(options.musicVolume)
            musicVolume.select(clamp((options.musicVolume * 20).roundToInt(), 0, 20), 0)
            SEVolume.select(clamp((options.SEVolume * 20).roundToInt(), 0, 20), 0)
        })
        grid.add(GridButton("Quit", 32, 940f, 80f, 200f, 40f, 0, 4, triggerSound = null) {
            onQuit()
        })
        grid.selectFirst()
        grid.activate()
        grid.updateComponent()
        grid.update()
        input.addProcessor(grid)
    }

    override fun fadeIn(oldScreen: KotoScreen?, duration: Float) {
        super.fadeIn(oldScreen, duration)
        oldOptions = options.copy()
        grid.clearActions()
        grid.setPosition(400f, 0f)
        grid.addAction(Actions.moveTo(0f, 0f, duration, Interpolation.pow5Out))
    }

    override fun fadeOut(newScreen: KotoScreen?, duration: Float) {
        super.fadeOut(newScreen, duration)
        grid.clearActions()
        grid.setPosition(0f, 0f)
        grid.addAction(Actions.moveTo(400f, 0f, duration, Interpolation.sineOut))
    }

    override fun onQuit() {
        if (grid.selectedY == 4) {
            super.onQuit()
            saveOptions()
            if (oldOptions!!.vsyncEnabled != options.vsyncEnabled
                || oldOptions!!.textureMagFilter != options.textureMagFilter
                || oldOptions!!.textureMinFilter != options.textureMinFilter
                || oldOptions!!.fpsMultiplier != options.fpsMultiplier
            ) {
                restartApp()
            } else {
                koto.setScreen("title", 0.5f)
            }
        } else {
            grid.select(0, 4)
        }
    }
}