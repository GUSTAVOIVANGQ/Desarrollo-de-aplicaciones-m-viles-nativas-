package ovh.gabrielhuav.sensores_escom_v2.presentation.components.ipn.zacatenco.escom.house

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import ovh.gabrielhuav.sensores_escom_v2.R
import ovh.gabrielhuav.sensores_escom_v2.presentation.components.mapview.*

class InsideHouse : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var movementManager: MovementManager
    private lateinit var playerName: String
    private var gameState = House.GameState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inside_house)

        try {
            mapView = MapView(
                context = this,
                mapResourceId = R.drawable.inside_house_map
            )
            findViewById<FrameLayout>(R.id.map_container).addView(mapView)
            
            initializeComponents()
            setupButtonListeners()
        } catch (e: Exception) {
            Log.e("InsideHouse", "Error en onCreate: ${e.message}")
            finish()
        }
    }

    private fun initializeComponents() {
        playerName = intent.getStringExtra("PLAYER_NAME") ?: run {
            finish()
            return
        }

        movementManager = MovementManager(mapView) { position -> 
            updatePlayerPosition(position)
        }

        // Configurar botones de movimiento
        findViewById<Button>(R.id.button_north).setOnTouchListener { _, event -> 
            handleMovement(event, 0, -1)
            true 
        }
        findViewById<Button>(R.id.button_south).setOnTouchListener { _, event -> 
            handleMovement(event, 0, 1)
            true 
        }
        findViewById<Button>(R.id.button_east).setOnTouchListener { _, event -> 
            handleMovement(event, 1, 0)
            true 
        }
        findViewById<Button>(R.id.button_west).setOnTouchListener { _, event -> 
            handleMovement(event, -1, 0)
            true 
        }

        // Configurar posición inicial
        gameState.playerPosition = Pair(20, 20)
        mapView.updateLocalPlayerPosition(gameState.playerPosition)
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.button_back_to_main).setOnClickListener {
            returnToHouse()
        }
    }

    private fun handleMovement(event: MotionEvent, deltaX: Int, deltaY: Int) {
        movementManager.handleMovement(event, deltaX, deltaY)
    }

    private fun updatePlayerPosition(position: Pair<Int, Int>) {
        gameState.playerPosition = position
        mapView.updateLocalPlayerPosition(position)
    }

    private fun returnToHouse() {
        val intent = Intent(this, House::class.java).apply {
            putExtra("PLAYER_NAME", playerName)
            putExtra("IS_SERVER", gameState.isServer)
            putExtra("INITIAL_POSITION", Pair(2, 2))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        returnToHouse()
    }
}
