package com.championslogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battles")
data class BattleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val opponentName: String = "",
    val format: String = "Doubles VGC", // Singles, Doubles VGC, Multi
    val result: String = "", // "Win", "Loss", "Draw", "Ongoing"
    val myTeam: String = "", // comma-separated Pokémon names
    val opponentTeam: String = "",
    val totalTurns: Int = 0,
    val notes: String = "",
    // Win/loss details
    val rating: Int = 0,
    val opponentRating: Int = 0
)

@Entity(tableName = "turns")
data class TurnRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val battleId: Long,
    val turnNumber: Int,
    // My side (2 slots for Doubles)
    val myPokemon1: String = "",
    val myMove1: String = "",
    val myHp1Before: Int = -1,
    val myHp1After: Int = -1,
    val myPokemon2: String = "",
    val myMove2: String = "",
    val myHp2Before: Int = -1,
    val myHp2After: Int = -1,
    // Opponent side (2 slots for Doubles)
    val opponentPokemon1: String = "",
    val opponentMove1: String = "",
    val opponentHp1Before: Int = -1,
    val opponentHp1After: Int = -1,
    val opponentPokemon2: String = "",
    val opponentMove2: String = "",
    val opponentHp2Before: Int = -1,
    val opponentHp2After: Int = -1,
    // Events this turn (comma-separated: "Mega Y, Drought, Crit, Focus Sash")
    val events: String = "",
    // Weather/terrain at start of turn
    val weather: String = "", // "Sun", "Rain", "Sand", "Snow", "Harsh Sun", "Heavy Rain", "Fog"
    val terrain: String = "", // "Electric", "Grassy", "Misty", "Psychic"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
