package com.example.notsensorlogger2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RelativeHumiditySensorHandler extends AbstractSensorHandler {
    public RelativeHumiditySensorHandler(SensorManager sensorManager) {
        super(sensorManager, Sensor.TYPE_RELATIVE_HUMIDITY);
        this.sensorName = "humidity";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        JSONObject dataPoint = new JSONObject();
        try {
            JSONObject values = new JSONObject();
            values.put("humidity", event.values[0]);

            dataPoint.put("name", this.sensorName);
            dataPoint.put("time", System.currentTimeMillis() * 1000000);
            dataPoint.put("values", values);

            synchronized (this) {
                sensorData.put(dataPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        JSONObject dataPoint = new JSONObject();
//        try {
//            dataPoint.put("sensor", sensorName);
//            dataPoint.put("time", System.nanoTime());
//          //  dataPoint.put("seconds_elapsed", (System.currentTimeMillis() - startTime) / 1000.0);
//            dataPoint.put("humidity", event.values[0]);  // "lux" is the unit of illuminance, used for light sensors
//            sensorData.put(dataPoint);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public JSONArray getSensorData() {
        return sensorData;
    }
}




















//package com.example.notsensorlogger2;
//
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorManager;
//import android.util.Log;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class RelativeHumiditySensorHandler extends AbstractSensorHandler {
//    private float lastValue;
//    public RelativeHumiditySensorHandler(SensorManager sensorManager) {
//        super(sensorManager, Sensor.TYPE_RELATIVE_HUMIDITY);
//        this.sensorName = "Relative Humidity";
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        // Save the latest sensor value
//        lastValue = event.values[0];
//    }
//
//    @Override
//    public JSONObject getSensorData() {
//        // Create a new JSON object to store sensor data
//        JSONObject sensorData = new JSONObject();
//        try {
//            // Put the sensor name, which should match the expected key from SensorLoggerMobileAppAgent
//            sensorData.put("name", "humidity"); // Name used by SensorLoggerMobileAppAgent
//
//            // Create the values object
//            JSONObject values = new JSONObject();
//            values.put("humidity", lastValue);
//
//            // Put the values object into sensorData
//            sensorData.put("values", values);
//
//            // Put the timestamp in nanoseconds since epoch (adjust as necessary)
//            sensorData.put("time", System.currentTimeMillis() * 1000000);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return sensorData;
//    }
//
//    // Method to retrieve the latest sensor value
//    public float getLastSensorValue() {
//        return lastValue;
//    }
//}
