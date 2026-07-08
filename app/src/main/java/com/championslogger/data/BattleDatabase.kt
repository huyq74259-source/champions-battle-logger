package com.championslogger.data

import androidx.room.*

@Dao
interface BattleDao {
    // Battle records
    @Insert
    suspend fun insertBattle(battle: BattleRecord): Long

    @Update
    suspend fun updateBattle(battle: BattleRecord)

    @Delete
    suspend fun deleteBattle(battle: BattleRecord)

    @Query("SELECT * FROM battles ORDER BY timestamp DESC")
    suspend fun getAllBattles(): List<BattleRecord>

    @Query("SELECT * FROM battles WHERE id = :id")
    suspend fun getBattleById(id: Long): BattleRecord?

    // Turn records
    @Insert
    suspend fun insertTurn(turn: TurnRecord): Long

    @Update
    suspend fun updateTurn(turn: TurnRecord)

    @Query("SELECT * FROM turns WHERE battleId = :battleId ORDER BY turnNumber ASC")
    suspend fun getTurnsForBattle(battleId: Long): List<TurnRecord>

    @Query("DELETE FROM turns WHERE battleId = :battleId")
    suspend fun deleteTurnsForBattle(battleId: Long)

    @Query("SELECT * FROM turns WHERE battleId = :battleId ORDER BY turnNumber DESC LIMIT 1")
    suspend fun getLatestTurn(battleId: Long): TurnRecord?
}

@Database(
    entities = [BattleRecord::class, TurnRecord::class],
    version = 1,
    exportSchema = false
)
abstract class BattleDatabase : RoomDatabase() {
    abstract fun battleDao(): BattleDao
}
