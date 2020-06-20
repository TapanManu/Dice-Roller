package com.example.diceroller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(),AccelerometerListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stopButton : Button = findViewById(R.id.stopButton)
        stopButton.setOnClickListener{
            onDestroy()
        }
        val startButton : Button = findViewById(R.id.startButton)
        startButton.setOnClickListener{
            onResume()
        }
    }
    override fun onAccelerationChanged(x: Float, y: Float, z: Float) {
        // TODO Auto-generated method stub
    }

    override fun onShake(force: Float) {

        rollDice()

    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(
            baseContext, "onResume Accelerometer Started",
            Toast.LENGTH_SHORT
        ).show()

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isSupported(this)) {

            //Start Accelerometer Listening
            AccelerometerManager.startListening(this)
        }
    }

    override fun onStop() {
        super.onStop()

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening()
            Toast.makeText(
                baseContext, "onStop Accelerometer Stoped",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Sensor", "Service  distroy")

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening()
            Toast.makeText(
                baseContext, "onDestroy Accelerometer Stoped",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun rollDice() {
        val dice = Dice(6)
        val diceroll = dice.roll()
        val diceImage : ImageView = findViewById(R.id.imageView)
        val drawing = when(diceroll){
            1->R.drawable.dice_1
            2->R.drawable.dice_2
            3->R.drawable.dice_3
            4->R.drawable.dice_4
            5->R.drawable.dice_5
            else ->R.drawable.dice_6
        }
        diceImage.setImageResource(drawing)
        diceImage.contentDescription = diceroll.toString()

    }

}
class Dice(val numSides:Int) {
    fun roll(): Int {
        return (1..numSides).random()
    }
}
interface AccelerometerListener {
    fun onAccelerationChanged(x: Float, y: Float, z: Float)
    fun onShake(force: Float)
}
object AccelerometerManager {
    private var aContext: Context? = null

    /** Accuracy configuration  */
    private var threshold = 15.0f
    private var interval = 200
    private var sensor: Sensor? = null
    private var sensorManager: SensorManager? = null

    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
    private var listener: AccelerometerListener? = null

    /** indicates whether or not Accelerometer Sensor is supported  */
    private var supported: Boolean? = null

    /**
     * Returns true if the manager is listening to orientation changes
     */
    /** indicates whether or not Accelerometer Sensor is running  */
    var isListening = false
        private set

    /**
     * Unregisters listeners
     */
    fun stopListening() {
        isListening = false
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager!!.unregisterListener(sensorEventListener)
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    fun isSupported(context: Context?): Boolean {
        aContext = context
        if (supported == null) {
            if (aContext != null) {
                sensorManager =
                    aContext!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

                // Get all sensors in device
                val sensors: List<Sensor> =
                    sensorManager!!.getSensorList(
                        Sensor.TYPE_ACCELEROMETER
                    )
                supported = sensors.isNotEmpty()
            } else {
                supported = java.lang.Boolean.FALSE
            }
        }
        return supported!!
    }

    /**
     * Configure the listener for shaking
     * @param threshold
     * minimum acceleration variation for considering shaking
     * @param interval
     * minimum interval between to shake events
     */
    fun configure(threshold: Int, interval: Int) {
        AccelerometerManager.threshold = threshold.toFloat()
        AccelerometerManager.interval = interval
    }

    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     * callback for accelerometer events
     */
    fun startListening(accelerometerListener: AccelerometerListener?) {
        sensorManager =
            aContext!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

        // Take all sensors in device
        val sensors: List<Sensor> =
            sensorManager!!.getSensorList(
                Sensor.TYPE_ACCELEROMETER
            )
        if (sensors.size > 0) {
            sensor = sensors[0]

            // Register Accelerometer Listener
            isListening = sensorManager!!.registerListener(
                sensorEventListener, sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            listener = accelerometerListener
        }
    }

    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener
     * callback for accelerometer events
     * @param threshold
     * minimum acceleration variation for considering shaking
     * @param interval
     * minimum interval between to shake events
     */
    fun startListening(
        accelerometerListener: AccelerometerListener?,
        threshold: Int, interval: Int
    ) {
        configure(threshold, interval)
        startListening(accelerometerListener)
    }

    /**
     * The listener that listen to events from the accelerometer listener
     */
    private val sensorEventListener: SensorEventListener? = object : SensorEventListener {
        private var now: Long = 0
        private var timeDiff: Long = 0
        private var lastUpdate: Long = 0
        private var lastShake: Long = 0
        private var x = 0f
        private var y = 0f
        private var z = 0f
        private var lastX = 0f
        private var lastY = 0f
        private var lastZ = 0f
        private var force = 0f
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            // use the event timestamp as reference
            // so the manager precision won't depends
            // on the AccelerometerListener implementation
            // processing time
            now = event.timestamp
            x = event.values[0]
            y = event.values[1]
            z = event.values[2]

            // if not interesting in shake events
            // just remove the whole if then else block
            if (lastUpdate == 0L) {
                lastUpdate = now
                lastShake = now
                lastX = x
                lastY = y
                lastZ = z
                Toast.makeText(
                    aContext, "No Motion detected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                timeDiff = now - lastUpdate
                if (timeDiff > 0) {

                    /*force = Math.abs(x + y + z - lastX - lastY - lastZ)
                                / timeDiff;*/
                    force = Math.abs(x + y + z - lastX - lastY - lastZ)
                    if (java.lang.Float.compare(force, threshold) > 0) {
                        //Toast.makeText(Accelerometer.getContext(),
                        //(now-lastShake)+"  >= "+interval, 1000).show();
                        if (now - lastShake >= interval) {

                            // trigger shake event
                            listener!!.onShake(force)
                        } else {
                            Toast.makeText(
                                aContext, "No Motion detected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        lastShake = now
                    }
                    lastX = x
                    lastY = y
                    lastZ = z
                    lastUpdate = now
                } else {
                    Toast.makeText(
                        aContext,
                        "No Motion detected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // trigger change event
            listener!!.onAccelerationChanged(x, y, z)
        }
    }
}
