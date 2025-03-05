package ovh.gabrielhuav.sensores_escom_v2.presentation.components

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
import ovh.gabrielhuav.sensores_escom_v2.data.map.BluetoothWebSocketBridge
import ovh.gabrielhuav.sensores_escom_v2.data.map.Bluetooth.BluetoothGameManager
import ovh.gabrielhuav.sensores_escom_v2.data.map.OnlineServer.OnlineServerManager
import ovh.gabrielhuav.sensores_escom_v2.presentation.components.mapview.*

class Biblioteca : AppCompatActivity(),
    BluetoothManager.BluetoothManagerCallback,
    BluetoothGameManager.ConnectionListener,
    OnlineServerManager.WebSocketListener,
    MapView.MapTransitionListener {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var movementManager: MovementManager
    private lateinit var serverConnectionManager: ServerConnectionManager
    private lateinit var mapView: MapView

    // UI Components
    private lateinit var btnNorth: Button
    private lateinit var btnSouth: Button
    private lateinit var btnEast: Button
    private lateinit var btnWest: Button
    private lateinit var btnBackToHome: Button
    private lateinit var tvBluetoothStatus: TextView

    private lateinit var playerName: String
    private lateinit var bluetoothBridge: BluetoothWebSocketBridge

    // Reuse BuildingNumber2.GameState structure
    private var gameState = BuildingNumber2.GameState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca)

        try {
            // Initialize MapView
            mapView = MapView(
                context = this,
                mapResourceId = R.drawable.escom_biblioteca
            )
            findViewById<FrameLayout>(R.id.map_container).addView(mapView)

            initializeComponents(savedInstanceState)

            mapView.post {
                mapView.setCurrentMap(MapMatrixProvider.MAP_BIBLIOTECA, R.drawable.escom_biblioteca)
                
                mapView.playerManager.apply {
                    setCurrentMap(MapMatrixProvider.MAP_BIBLIOTECA)
                    localPlayerId = playerName
                    updateLocalPlayerPosition(gameState.playerPosition)
                }

                if (gameState.isConnected) {
                    serverConnectionManager.sendUpdateMessage(playerName, gameState.playerPosition, MapMatrixProvider.MAP_BIBLIOTECA)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing activity.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // ... Rest of the implementation follows the same pattern as Cafeteria.kt ...
    // Include all necessary component initialization, movement handling, and network communication methods

    private fun returnToMainMap() {
        val previousPosition = intent.getSerializableExtra("PREVIOUS_POSITION") as? Pair<Int, Int>
            ?: MapMatrixProvider.BIBLIOTECA_TO_MAIN_POSITION

        val intent = Intent(this, GameplayActivity::class.java).apply {
            putExtra("PLAYER_NAME", playerName)
            putExtra("IS_SERVER", gameState.isServer)
            putExtra("IS_CONNECTED", gameState.isConnected)
            putExtra("INITIAL_POSITION", previousPosition)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        mapView.playerManager.cleanup()
        startActivity(intent)
        finish()
    }

    override fun onMapTransitionRequested(targetMap: String, initialPosition: Pair<Int, Int>) {
        when (targetMap) {
            MapMatrixProvider.MAP_MAIN -> {
                returnToMainMap()
            }
            else -> {
                Log.d(TAG, "Unrecognized target map: $targetMap")
            }
        }
    }
override fun onBluetoothDeviceConnected(device: BluetoothDevice) {
    // Implement the logic for when a Bluetooth device is connected
    Log.d(TAG, "Bluetooth device connected: ${device.name}")
}
    companion object {
        private const val TAG = "BibliotecaESCOM"
    }
}
