package com.championslogger.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.championslogger.MainActivity
import com.championslogger.R
import com.championslogger.data.TurnRecord

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var expandedPanel: View? = null
    private var collapsedBubble: View? = null

    private var isExpanded = false
    private var currentTurn = 0
    private var currentBattleId: Long? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "champions_overlay"

        // Current state — Doubles (2 mons per side)
        val currentMyPokemon1 = mutableStateOf("")
        val currentMyMove1 = mutableStateOf("")
        val currentMyPokemon2 = mutableStateOf("")
        val currentMyMove2 = mutableStateOf("")
        val currentOpponentPokemon1 = mutableStateOf("")
        val currentOpponentMove1 = mutableStateOf("")
        val currentOpponentPokemon2 = mutableStateOf("")
        val currentOpponentMove2 = mutableStateOf("")
        val currentMyHp1 = mutableStateOf(100)
        val currentMyHp2 = mutableStateOf(100)
        val currentOpponentHp1 = mutableStateOf(100)
        val currentOpponentHp2 = mutableStateOf(100)
        val lastEvent = mutableStateOf("") // e.g. "Mega! | Drought | Crit | Focus Sash"

        val turns = mutableListOf<TurnRecord>()
        var currentTurnNumber = mutableStateOf(0)
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (overlayView == null) {
            createOverlay()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battle Logger Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating overlay for Pokémon Champions"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battle Logger Active")
            .setContentText("Tap to open controls")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createOverlay() {
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(this).inflate(
            R.layout.overlay_main, null
        )

        collapsedBubble = overlayView?.findViewById(R.id.collapsed_bubble)
        expandedPanel = overlayView?.findViewById(R.id.expanded_panel)

        setupCollapsedBubble()
        setupExpandedPanel()
        setupDrag(overlayView!!, layoutParams)

        showCollapsed()

        windowManager.addView(overlayView, layoutParams)
    }

    private fun setupCollapsedBubble() {
        collapsedBubble?.setOnClickListener {
            toggleExpand()
        }

        // Update turn count on bubble
        val turnText = collapsedBubble?.findViewById<TextView>(R.id.turn_count)
        turnText?.text = "0"
    }

    private fun setupExpandedPanel() {
        // Minimize button
        expandedPanel?.findViewById<View>(R.id.btn_minimize)?.setOnClickListener {
            toggleExpand()
        }

        // Close button (stops service)
        expandedPanel?.findViewById<View>(R.id.btn_close)?.setOnClickListener {
            stopSelf()
        }

        // Add turn button
        expandedPanel?.findViewById<View>(R.id.btn_add_turn)?.setOnClickListener {
            addTurn()
        }

        // HP quick buttons - My HP
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_my_25), true, -25)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_my_50), true, -50)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_my_75), true, -75)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_my_100), true, -100)

        // HP quick buttons - Opponent HP
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_opp_25), false, -25)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_opp_50), false, -50)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_opp_75), false, -75)
        setupHpButtons(expandedPanel?.findViewById(R.id.hp_opp_100), false, -100)
    }

    private fun setupHpButtons(button: View?, isMyHp: Boolean, change: Int) {
        button?.setOnClickListener {
            if (isMyHp) {
                val current = currentMyHp.value
                currentMyHp.value = maxOf(0, current + change)
            } else {
                val current = currentOpponentHp.value
                currentOpponentHp.value = maxOf(0, current + change)
            }
            updateHpLabels()
        }
    }

    private fun updateHpLabels() {
        expandedPanel?.findViewById<TextView>(R.id.my_hp_label)?.text =
            "My HP: ${currentMyHp.value}%"
        expandedPanel?.findViewById<TextView>(R.id.opp_hp_label)?.text =
            "Opp HP: ${currentOpponentHp.value}%"
    }

    private fun addTurn() {
        currentTurn++

        val myPokemon = expandedPanel?.findViewById<EditText>(R.id.et_my_pokemon)?.text.toString()
        val myMove = expandedPanel?.findViewById<EditText>(R.id.et_my_move)?.text.toString()
        val oppPokemon = expandedPanel?.findViewById<EditText>(R.id.et_opp_pokemon)?.text.toString()
        val oppMove = expandedPanel?.findViewById<EditText>(R.id.et_opp_move)?.text.toString()

        // Reset fields for next turn
        expandedPanel?.findViewById<EditText>(R.id.et_my_move)?.setText("")
        expandedPanel?.findViewById<EditText>(R.id.et_opp_move)?.setText("")

        val turn = TurnRecord(
            battleId = currentBattleId ?: -1,
            turnNumber = currentTurn,
            myPokemon = myPokemon,
            myMove = myMove,
            opponentPokemon = oppPokemon,
            opponentMove = oppMove,
            myHpBefore = if (turns.isEmpty()) 100 else currentMyHp.value,
            myHpAfter = currentMyHp.value,
            opponentHpBefore = if (turns.isEmpty()) 100 else currentOpponentHp.value,
            opponentHpAfter = currentOpponentHp.value
        )
        turns.add(turn)
        currentTurnNumber.value = currentTurn

        // Update the log
        updateLogDisplay()
        updateBubbleCounter()
    }

    private fun updateLogDisplay() {
        val logContainer = expandedPanel?.findViewById<LinearLayout>(R.id.log_container)
        logContainer?.removeAllViews()

        // Show last 5 turns
        val recentTurns = turns.takeLast(5).reversed()
        for (turn in recentTurns) {
            val logEntry = TextView(this).apply {
                text = "T${turn.turnNumber}: ${turn.myMove} → ${turn.opponentMove}"
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
                setPadding(0, 2, 0, 2)
            }
            logContainer?.addView(logEntry)
        }
    }

    private fun updateBubbleCounter() {
        collapsedBubble?.findViewById<TextView>(R.id.turn_count)?.text = "$currentTurn"
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded
        if (isExpanded) {
            showExpanded()
        } else {
            showCollapsed()
        }
    }

    private fun showCollapsed() {
        collapsedBubble?.visibility = View.VISIBLE
        expandedPanel?.visibility = View.GONE
    }

    private fun showExpanded() {
        collapsedBubble?.visibility = View.GONE
        expandedPanel?.visibility = View.VISIBLE
        updateHpLabels()
        updateLogDisplay()
    }

    private fun setupDrag(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        expandedPanel = null
        collapsedBubble = null
    }
}
