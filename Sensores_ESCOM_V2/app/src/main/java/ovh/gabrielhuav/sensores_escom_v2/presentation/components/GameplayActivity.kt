/*private fun checkPositionForMapChange(position: Pair<Int, Int>) {
    // ...existing code...
    when {
        // ...existing code...
        position.first == 7 && position.second == 20 -> {
            canChangeMap = true
            targetDestination = "auditorio"
            runOnUiThread {
                Toast.makeText(this, "Presiona A para entrar al auditorio", Toast.LENGTH_SHORT).show()
            }
        }
        else -> {
            canChangeMap = false
            targetDestination = null
        }
    }
}

private fun setupButtonListeners() {
    uiManager.apply {
        // ...existing code...
        
        buttonA.setOnClickListener {
            if (canChangeMap) {
                when (targetDestination) {
                    // ...existing code...
                    "auditorio" -> startAuditorioActivity()
                    else -> showToast("No hay interacción disponible en esta posición")
                }
            } else {
                showToast("No hay interacción disponible en esta posición")
            }
        }
    }
}

private fun startAuditorioActivity() {
    val intent = Intent(this, Auditorio::class.java).apply {
        putExtra("PLAYER_NAME", playerName)
        putExtra("IS_SERVER", gameState.isServer)
        putExtra("INITIAL_POSITION", Pair(1, 1))
        putExtra("PREVIOUS_POSITION", gameState.playerPosition)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    startActivity(intent)
    finish()
}
*/