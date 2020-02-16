package com.saltserv.tiltspot

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var sensorAccelerometer: Sensor? = null
    private var sensorMagnetometer: Sensor? = null

    private var accelerometerData = FloatArray(3)
    private var magnetometerData = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sensorManager = getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
        sensorAccelerometer = sensorManager!!.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )
        sensorMagnetometer = sensorManager!!.getDefaultSensor(
            Sensor.TYPE_MAGNETIC_FIELD
        )
    }

    override fun onStart() {
        super.onStart()
        if (sensorAccelerometer != null) {
            sensorManager!!.registerListener(
                this, sensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        if (sensorMagnetometer != null) {
            sensorManager!!.registerListener(
                this, sensorMagnetometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {

        when (sensorEvent.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerData =
                sensorEvent.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerData =
                sensorEvent.values.clone()
            else -> return
        }

        val rotationMatrix = FloatArray(9)

        val rotationOK = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerData,
            magnetometerData
        )

        val orientationValues = FloatArray(3)

        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues)
        }

        val azimuth = orientationValues[0]
        var pitch = orientationValues[1]
        var roll = orientationValues[2]

        if (abs(pitch) < VALUE_DRIFT) {
            pitch = 0F
        }
        if (abs(roll) < VALUE_DRIFT) {
            roll = 0F
        }

        azimuthValueTV.text = resources.getString(R.string.value_format, azimuth)
        pitchValueTV.text = resources.getString(R.string.value_format, pitch)
        rollValueTV.text = resources.getString(R.string.value_format, roll)

        spot_bottom.alpha = 0F
        spot_top.alpha = 0F
        spot_right.alpha = 0F
        spot_left.alpha = 0F

        if (pitch > 0) {
            spot_bottom.alpha = pitch
        } else {
            spot_top.alpha = (abs(pitch))
        }

        if (roll > 0) {
            spot_left.alpha = (roll)
        } else {
            spot_right.alpha = abs(roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {}

    companion object {
        private const val VALUE_DRIFT = 0.05f
    }
}