package com.example.pizzacompas

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorListener(mainActivity: MainActivity,sensorManager: SensorManager):SensorEventListener {

    var mGravity: FloatArray? = null
    var mGeomagnetic: FloatArray? = null
    var dn:Float = 0f //degrees to north
    var magneticField: Sensor
    var accellerometer: Sensor

    init {
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accellerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accellerometer,2)
        sensorManager.registerListener(this, magneticField,2)
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) mGravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values
        if (mGravity != null && mGeomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                dn = (-orientation[0])*360/(2*3.14159f)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}