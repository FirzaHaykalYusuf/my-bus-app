package com.rexensoft.mybus.network

import android.util.Log
import com.mapbox.geojson.Point
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object MapboxDirectionsFetcher {

    private const val BASE_URL = "https://api.mapbox.com/directions/v5/mapbox/driving"
    private const val ACCESS_TOKEN = "pk.eyJ1IjoiZmlyemFoeSIsImEiOiJjbWJuYzNpdzAxcGNnMnNvOXZ5Z244YzBmIn0.WuijfaVRoIuhtnODJH9ApA"

    fun getRouteCoordinates(origin: Point, destination: Point): List<Point> {
        val client = OkHttpClient()

        val url = "$BASE_URL/${origin.longitude()},${origin.latitude()};${destination.longitude()},${destination.latitude()}" +
                "?geometries=geojson&overview=full&access_token=$ACCESS_TOKEN"

        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("MapboxDirections", "Failed: ${response.code}")
                return emptyList()
            }

            val responseBody = response.body?.string() ?: return emptyList()
            val json = JSONObject(responseBody)
            val coordinates = json.getJSONArray("routes")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<Point>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lng = coord.getDouble(0)
                val lat = coord.getDouble(1)
                points.add(Point.fromLngLat(lng, lat))
            }

            points
        } catch (e: Exception) {
            Log.e("MapboxDirections", "Exception: ${e.message}")
            emptyList()
        }
    }
}
