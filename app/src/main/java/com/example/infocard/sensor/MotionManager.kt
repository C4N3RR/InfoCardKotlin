package com.example.infocard.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MotionManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _roll = MutableStateFlow(0.0)
    val roll: StateFlow<Double> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0.0)
    val pitch: StateFlow<Double> = _pitch.asStateFlow()

    private var referenceRoll: Double? = null
    private var referencePitch: Double? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationValues = FloatArray(3)

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        referenceRoll = null
        referencePitch = null
        _roll.value = 0.0
        _pitch.value = 0.0
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationValues)

        // yaw = orientationValues[0]
        // pitch = orientationValues[1] (around X axis)
        // roll = orientationValues[2] (around Y axis)
        val currentPitch = orientationValues[1].toDouble()
        val currentRoll = orientationValues[2].toDouble()

        if (referenceRoll == null) {
            referenceRoll = currentRoll
            referencePitch = currentPitch
        }

        val refRoll = referenceRoll!!
        val refPitch = referencePitch!!

        val relativeRoll = currentRoll - refRoll
        val relativePitch = currentPitch - refPitch

        // Auto-centering: slowly leak the reference to the current position (decay)
        referenceRoll = refRoll * 0.98 + currentRoll * 0.02
        referencePitch = refPitch * 0.98 + currentPitch * 0.02

        // Low-pass filter to smooth out fast jitter
        _roll.value = _roll.value * 0.90 + relativeRoll * 0.10
        _pitch.value = _pitch.value * 0.90 + relativePitch * 0.10
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
