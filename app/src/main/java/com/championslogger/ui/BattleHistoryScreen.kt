package com.championslogger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.championslogger.data.BattleDatabase
import com.championslogger.data.BattleRecord
import com.championslogger.data.TurnRecord
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleHistoryScreen(database: BattleDatabase) {
    val scope = rememberCoroutineScope()
    var battles by remember { mutableStateOf<List<BattleRecord>>(emptyList()) }
    var selectedBattle by remember { mutableStateOf<BattleRecord?>(null) }
    var turns by remember { mutableStateOf<List<TurnRecord>>(emptyList()) }

    LaunchedEffect(Unit) {
        battles = database.battleDao().getAllBattles()
    }

    if (selectedBattle != null) {
        // Show turns for selected battle
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Back button
            TextButton(
                onClick = {
                    selectedBattle = null
                    turns = emptyList()
                }
            ) {
                Text("← Back to Battles")
            }

            // Battle header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Battle - ${selectedBattle!!.result}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Turns: ${selectedBattle!!.totalTurns}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Opponent: ${selectedBattle!!.opponentName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Turns list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(turns) { turn ->
                    TurnCard(turn)
                }
            }
        }
    } else {
        // Battle list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (battles.isEmpty()) {
                item {
                    Text(
                        "No battles recorded yet.\nStart the overlay and log your next battle!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            items(battles) { battle ->
                Card(
                    onClick = {
                        selectedBattle = battle
                        scope.launch {
                            turns = database.battleDao().getTurnsForBattle(battle.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = battle.result.ifEmpty { "Ongoing" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when (battle.result) {
                                    "Win" -> MaterialTheme.colorScheme.primary
                                    "Loss" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "${battle.totalTurns} turns",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (battle.opponentName.isNotBlank()) {
                            Text(
                                text = "vs ${battle.opponentName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatTimestamp(battle.timestamp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TurnCard(turn: TurnRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "Turn ${turn.turnNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "YOU: ${turn.myPokemon}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "→ ${turn.myMove}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (turn.myHpBefore >= 0) {
                        Text(
                            "HP: ${turn.myHpBefore}% → ${turn.myHpAfter}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "OPP: ${turn.opponentPokemon}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "→ ${turn.opponentMove}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (turn.opponentHpBefore >= 0) {
                        Text(
                            "HP: ${turn.opponentHpBefore}% → ${turn.opponentHpAfter}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (turn.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "📝 ${turn.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
