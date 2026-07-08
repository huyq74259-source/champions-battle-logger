package com.championslogger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battles")
data class BattleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val opponentName: String = "",
    val result: String = "", // "Win", "Loss", "Draw", "Ongoing"
    val myTeam: String = "", // comma-separated Pokémon names
    val opponentTeam: String = "",
    val totalTurns: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "turns")
data class TurnRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val battleId: Long,
    val turnNumber: Int,
    val myPokemon: String = "",
    val myMove: String = "",
    val myHpBefore: Int = -1,
    val myHpAfter: Int = -1,
    val opponentPokemon: String = "",
    val opponentMove: String = "",
    val opponentHpBefore: Int = -1,
    val opponentHpAfter: Int = -1,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
