package io.github.dkambersky.songle.data.definitions

/**
 * Holds information about difficulty levels
 */
enum class Difficulty(val pickupRange: Int, val startMapMode: Int, val bonusItemFactor: Float, val guessAttempts: Int = 0) {
    EASY(30, 3, 0.2f),
    MEDIUM(25, 2, 0.15f),
    HARD(20, 1, 0.15f, 3)

}