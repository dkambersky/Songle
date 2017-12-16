package io.github.dkambersky.songle.data.definitions

/**
 * Holds information related to the game's state
 */
data class GameState(var maxPlacemarks: Int,
                     var currentLevel: Int,
                     var pickedUpPlacemarks: Int,
                     var currentThreshold: Int,
                     val step: Int,
                     var guessesLeft: Int,
                     var currentFloor: Int
)