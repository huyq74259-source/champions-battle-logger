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
import com.championslogger.data.TurnRecord

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var expandedPanel: View? = null
    private var collapsedBubble: View? = null

    private var isExpanded = false
    private var currentTurn = 0

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "champions_overlay"

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

        if (overlayView == null) createOverlay()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Battle Logger Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Floating overlay for Pokémon Champions" }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
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
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_main, null)
        collapsedBubble = overlayView?.findViewById(R.id.collapsed_bubble)
        expandedPanel = overlayView?.findViewById(R.id.expanded_panel)

        setupCollapsedBubble()
        setupExpandedPanel()
        setupDrag(overlayView!!, layoutParams)
        showCollapsed()
        windowManager.addView(overlayView, layoutParams)
    }

    private fun setupCollapsedBubble() {
        collapsedBubble?.setOnClickListener { toggleExpand() }
        updateBubbleCounter()
    }

    private fun setupExpandedPanel() {
        expandedPanel?.findViewById<View>(R.id.btn_minimize)?.setOnClickListener { toggleExpand() }
        expandedPanel?.findViewById<View>(R.id.btn_close)?.setOnClickListener { stopSelf() }
        expandedPanel?.findViewById<View>(R.id.btn_add_turn)?.setOnClickListener { addTurn() }
        expandedPanel?.findViewById<View>(R.id.btn_events)?.setOnClickListener { showEventsDialog() }

        // My HP
        setupHpButton(R.id.hp_my_25, true, 1, -25)
        setupHpButton(R.id.hp_my_50, true, 1, -50)
        setupHpButton(R.id.hp_my_100, true, 1, -100)

        // Opp HP
        setupHpButton(R.id.hp_opp_25, false, 1, -25)
        setupHpButton(R.id.hp_opp_50, false, 1, -50)
        setupHpButton(R.id.hp_opp_100, false, 1, -100)
    }

    private fun setupHpButton(viewId: Int, isMySide: Boolean, slot: Int, change: Int) {
        expandedPanel?.findViewById<View>(viewId)?.setOnClickListener {
            if (isMySide) {
                val current = if (slot == 1) currentMyHp1.value else currentMyHp2.value
                val newVal = maxOf(0, current + change)
                if (slot == 1) currentMyHp1.value = newVal else currentMyHp2.value = newVal
            } else {
                val current = if (slot == 1) currentOpponentHp1.value else currentOpponentHp2.value
                val newVal = maxOf(0, current + change)
                if (slot == 1) currentOpponentHp1.value = newVal else currentOpponentHp2.value = newVal
            }
            updateHpLabels()
        }
    }

    private fun updateHpLabels() {
        expandedPanel?.findViewById<TextView>(R.id.my_hp_label)?.text =
            "HP: ${currentMyHp1.value}|${currentMyHp2.value}"
        expandedPanel?.findViewById<TextView>(R.id.opp_hp_label)?.text =
            "HP: ${currentOpponentHp1.value}|${currentOpponentHp2.value}"
    }

    private fun showEventsDialog() {
        // Cycle through common events and append to a temp field
        // For V1, just append a note marker
        Toast.makeText(this, "Event marked", Toast.LENGTH_SHORT).show()
    }

    private fun addTurn() {
        currentTurn++

        val myPokemon1 = expandedPanel?.findViewById<EditText>(R.id.et_my_pokemon1)?.text.toString()
        val myMove1 = expandedPanel?.findViewById<EditText>(R.id.et_my_move1)?.text.toString()
        val myPokemon2 = expandedPanel?.findViewById<EditText>(R.id.et_my_pokemon2)?.text.toString()
        val myMove2 = expandedPanel?.findViewById<EditText>(R.id.et_my_move2)?.text.toString()
        val oppPokemon1 = expandedPanel?.findViewById<EditText>(R.id.et_opp_pokemon1)?.text.toString()
        val oppMove1 = expandedPanel?.findViewById<EditText>(R.id.et_opp_move1)?.text.toString()
        val oppPokemon2 = expandedPanel?.findViewById<EditText>(R.id.et_opp_pokemon2)?.text.toString()
        val oppMove2 = expandedPanel?.findViewById<EditText>(R.id.et_opp_move2)?.text.toString()

        // Clear move fields for next turn
        expandedPanel?.findViewById<EditText>(R.id.et_my_move1)?.setText("")
        expandedPanel?.findViewById<EditText>(R.id.et_my_move2)?.setText("")
        expandedPanel?.findViewById<EditText>(R.id.et_opp_move1)?.setText("")
        expandedPanel?.findViewById<EditText>(R.id.et_opp_move2)?.setText("")

        val turn = TurnRecord(
            battleId = -1, // placeholder; real battle ID set when saved
            turnNumber = currentTurn,
            myPokemon1 = myPokemon1,
            myMove1 = myMove1,
            myHp1Before = currentMyHp1.value,
            myHp1After = currentMyHp1.value,
            myPokemon2 = myPokemon2,
            myMove2 = myMove2,
            myHp2Before = currentMyHp2.value,
            myHp2After = currentMyHp2.value,
            opponentPokemon1 = oppPokemon1,
            opponentMove1 = oppMove1,
            opponentHp1Before = currentOpponentHp1.value,
            opponentHp1After = currentOpponentHp1.value,
            opponentPokemon2 = oppPokemon2,
            opponentMove2 = oppMove2,
            opponentHp2Before = currentOpponentHp2.value,
            opponentHp2After = currentOpponentHp2.value
        )
        turns.add(turn)
        currentTurnNumber.value = currentTurn

        updateLogDisplay()
        updateBubbleCounter()
    }

    private fun updateLogDisplay() {
        val logContainer = expandedPanel?.findViewById<LinearLayout>(R.id.log_container)
        logContainer?.removeAllViews()

        val recentTurns = turns.takeLast(5).reversed()
        for (turn in recentTurns) {
            val summary = buildString {
                append("T${turn.turnNumber}: ")
                if (turn.myMove1.isNotBlank()) append("${turn.myMove1}")
                if (turn.myMove2.isNotBlank()) append("/${turn.myMove2}")
                append(" → ")
                if (turn.opponentMove1.isNotBlank()) append("${turn.opponentMove1}")
                if (turn.opponentMove2.isNotBlank()) append("/${turn.opponentMove2}")
            }
            val logEntry = TextView(this).apply {
                text = summary
                textSize = 11f
                setTextColor(android.graphics.Color.parseColor("#B0BEC5"))
                setPadding(0, 3, 0, 3)
                maxLines = 1
            }
            logContainer?.addView(logEntry)
        }
    }

    private fun updateBubbleCounter() {
        collapsedBubble?.findViewById<TextView>(R.id.turn_count)?.text = "$currentTurn"
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded
        if (isExpanded) showExpanded() else showCollapsed()
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
        overlayView = null; expandedPanel = null; collapsedBubble = null
    }
}
