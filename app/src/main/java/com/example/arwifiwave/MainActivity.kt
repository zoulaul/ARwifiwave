package com.example.arwifiwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.arwifiwave.ui.theme.ARwifiwaveTheme
import android.content.Context
import android.net.wifi.WifiManager
// for using live  broadcast reciever
import android.content.BroadcastReceiver
import android.content.Intent
import  android.content.IntentFilter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import android.Manifest
import  androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import  androidx.compose.ui.Modifier
import  androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
// imports for UI
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateOf



class MainActivity : ComponentActivity() {

    // letting the permission warning or notice pops out

  private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
  { isGranted: Boolean ->
  if (isGranted){
      // The user said YES!
      println("permission Granted")
  } else {
      // The user said NO!
      println("permission Denied")
  }
  }
    //   --- BRACKET LEVEL 1: CLASS VARIABLES ---

// 1. The "memory" (State )
    private var wifiSignal by mutableIntStateOf(0)
    private var linkSpeed by mutableIntStateOf(0)
    private var frequency by mutableIntStateOf(0)
    private var isSimulatorMode by mutableStateOf (true)



//    2. The "tool"  (Manager)
private lateinit var wifiManager: WifiManager

// Math : normalize the signal
    // IT TURNS -90 ( Weak ) to - 30 (Strong)  into a percentage(0.0 to 1.0)
    val normalizedSignal: Float
        get() = ((wifiSignal.toFloat()+ 90f)/60f).coerceIn(0f,1f)
    //the artist : decides the color
    // It looks at the percentage and picks a color object
    val signalColor: Color
        get() = when {
            normalizedSignal > 0.7f -> Color.Green // Strong (70 %)
            normalizedSignal > 0.4f -> Color.Yellow // okay (40%to70%)
            else -> Color.Red

        }
            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this code related to the permission so it makes the code actually pops on screen

        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        // 3. Initialize the WifiManager here
        //  calling the wright department for wifi services

         wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                setContent {
            ARwifiwaveTheme {
                // A surface is the background layer
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                // 4. Use the "State" variable inside your Text() her
          Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
          ) {
              Text(
                  text = "THE INVISIBLE OCEAN",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color= Color.Gray,
                  letterSpacing = 2.sp
              )
              Spacer(modifier = Modifier.height(16.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("SIMULATOR MODE", fontSize = 12.sp, color = Color.Gray)
                  Spacer(modifier = Modifier.width(8.dp))
                  Switch(
                      checked = isSimulatorMode,
                      onCheckedChange = { newValue ->
                          isSimulatorMode = newValue

                          // If we just turned the simulator of , grab real data now
                          if (!newValue){
                              val info = wifiManager.connectionInfo
                              wifiSignal = info.rssi
                              linkSpeed = info.linkSpeed
                              frequency = info.frequency
                          }
                      }
                  )
              }
                  Spacer(modifier = Modifier.height(24.dp))

                  //The main data card
                  Card(
                      modifier = Modifier.fillMaxWidth(),
                      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                  ) {
                      Column(modifier = Modifier.padding(20.dp))
                      {
                          Text(text = "CURRENT SIGNAL", fontSize = 14.sp, color = Color.Gray)

                          // Big signal Number
                          Text(
                              text = "$wifiSignal dBm",
                              fontSize = 48.sp,
                              fontWeight = FontWeight.ExtraBold,
                              color = signalColor
                          )
                          // The power Bar
                          LinearProgressIndicator(
                              progress = normalizedSignal,
                              modifier = Modifier.fillMaxWidth().height(8.dp),
                              color = signalColor,
                              trackColor = Color.LightGray
                          )
                          Text(
                              text = "${(normalizedSignal * 100).toInt()}%Strength",
                              modifier = Modifier.align(Alignment.End),
                              fontSize = 12.sp
                          )
                      }
                  }
                  Spacer(modifier = Modifier.weight(1f))
                  if (isSimulatorMode) {
                      // simulator of the debugger tool
                      Text(text = "EMULATOR SIMULATOR ", fontSize = 12.sp, color = Color.Gray)
                      Slider(
                          value = wifiSignal.toFloat(),
                          onValueChange = { wifiSignal = it.toInt() },
                          valueRange = -90f..-30f
                      )
                  }
               }
              }
             }
            }
        }
    // --- BRACKET LEVEL 2: LIFECYCLE METHODS ---
    override fun onResume() {
        super.onResume()
        //5.start listening(registerRecevier)here.
        val filter = IntentFilter(WifiManager.RSSI_CHANGED_ACTION)
        registerReceiver(rssiReceiver, filter)
        val currentInfo = wifiManager.connectionInfo
        wifiSignal= currentInfo.rssi
        linkSpeed = currentInfo.linkSpeed
        frequency = currentInfo.frequency
    }

override fun onPause() {
    super.onPause()
        //6. stop listening to (unrigisteRreciever ) here .
    unregisterReceiver(rssiReceiver)
}
// --- BRACKET LEVEL 3: the Ear ---

    val rssiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val newRssi = intent?.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0)

            //7. extract the number and update our memory
            if (!isSimulatorMode && newRssi != null) {
                wifiSignal = newRssi
                val info = wifiManager.connectionInfo
                linkSpeed = info.linkSpeed
                frequency = info.frequency
            }
            println("Live Signal Strength: $newRssi dBm")
        }
    }


}
