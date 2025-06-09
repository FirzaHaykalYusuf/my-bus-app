package com.rexensoft.mybus.ui.activity.menu

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.rexensoft.mybus.R
import com.rexensoft.mybus.data.model.BusModel
import com.rexensoft.mybus.network.MapboxDirectionsFetcher
import com.rexensoft.mybus.adapter.BusAdapter
import kotlin.concurrent.thread

class BusActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var rvBusList: RecyclerView
    private lateinit var annotationManager: PointAnnotationManager
    private val handler = Handler(Looper.getMainLooper())

    private var movingMarkerA: PointAnnotation? = null
    private var movingMarkerB: PointAnnotation? = null
    private var movingRunnableA: Runnable? = null
    private var movingRunnableB: Runnable? = null
    private var focusedBusName: String? = null

    private val busA = BusModel("Bus A", -6.1754, 106.8272)
    private val busB = BusModel("Bus B", -6.226944, 106.802528)
    private val busList = listOf(busA, busB)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnRefresh = findViewById<ImageView>(R.id.btn_refresh)

        mapView = findViewById(R.id.mapView)
        rvBusList = findViewById(R.id.rv_bus_list)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRefresh.setOnClickListener {
            reloadMap()
        }

        setupBusList()

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            val busBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_bus)
            style.addImage("bus_icon", busBitmap)

            annotationManager = mapView.annotations.createPointAnnotationManager()

            moveToUserLocation()

            thread {
                val routeA = MapboxDirectionsFetcher.getRouteCoordinates(
                    Point.fromLngLat(106.8272, -6.1754),
                    Point.fromLngLat(106.8265, -6.1747)
                )

                val routeB = MapboxDirectionsFetcher.getRouteCoordinates(
                    Point.fromLngLat(106.802528, -6.226944),
                    Point.fromLngLat(106.816139, -6.193778)
                )

                runOnUiThread {
                    if (routeA.isNotEmpty()) {
                        drawRoute(routeA, "route-a", "#1E90FF")
                        startMovingBus(routeA, isBusA = true)
                    }

                    if (routeB.isNotEmpty()) {
                        drawRoute(routeB, "route-b", "#FF8C00")
                        startMovingBus(routeB, isBusA = false)
                    }
                }
            }
        }
    }

    private fun reloadMap() {
        movingRunnableA?.let { handler.removeCallbacks(it) }
        movingRunnableB?.let { handler.removeCallbacks(it) }
        movingMarkerA?.let { annotationManager.delete(it) }
        movingMarkerB?.let { annotationManager.delete(it) }
        movingMarkerA = null
        movingMarkerB = null

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            val busBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_bus)
            style.addImage("bus_icon", busBitmap)

            annotationManager = mapView.annotations.createPointAnnotationManager()
            moveToUserLocation()

            thread {
                val routeA = MapboxDirectionsFetcher.getRouteCoordinates(
                    Point.fromLngLat(106.8272, -6.1754),
                    Point.fromLngLat(106.8265, -6.1747)
                )

                val routeB = MapboxDirectionsFetcher.getRouteCoordinates(
                    Point.fromLngLat(106.802528, -6.226944),
                    Point.fromLngLat(106.816139, -6.193778)
                )

                runOnUiThread {
                    if (routeA.isNotEmpty()) {
                        drawRoute(routeA, "route-a", "#1E90FF")
                        startMovingBus(routeA, isBusA = true)
                    }

                    if (routeB.isNotEmpty()) {
                        drawRoute(routeB, "route-b", "#FF8C00")
                        startMovingBus(routeB, isBusA = false)
                    }
                }
            }
        }
    }


    private fun setupBusList() {
        rvBusList.layoutManager = LinearLayoutManager(this)
        rvBusList.adapter = BusAdapter(busList) { bus ->
            focusBusOnMap(bus)
        }
    }

    private fun moveToUserLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return

        client.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val point = Point.fromLngLat(it.longitude, it.latitude)
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder().center(point).zoom(13.0).build()
                )
            }
        }
    }

    private fun focusBusOnMap(bus: BusModel) {
        focusedBusName = bus.id
        val point = Point.fromLngLat(bus.longitude, bus.latitude)
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(point).zoom(15.0).build()
        )
    }

    private fun drawRoute(points: List<Point>, sourceId: String, color: String) {
        val lineString = LineString.fromLngLats(points)
        val source = geoJsonSource(sourceId) { geometry(lineString) }

        mapView.getMapboxMap().getStyle()?.apply {
            addSource(source)
            addLayer(
                LineLayer("${sourceId}-layer", sourceId).apply {
                    lineColor(color)
                    lineWidth(4.0)
                    lineJoin(LineJoin.ROUND)
                    lineCap(LineCap.ROUND)
                }
            )
        }
    }

    private fun startMovingBus(route: List<Point>, isBusA: Boolean) {
        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                if (index >= route.size) index = 0
                val point = route[index++]

                val marker = if (isBusA) movingMarkerA else movingMarkerB

                val updatedMarker = if (marker == null) {
                    annotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("bus_icon")
                    )
                } else {
                    annotationManager.delete(marker)
                    annotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("bus_icon")
                    )
                }

                if (isBusA) {
                    movingMarkerA = updatedMarker
                } else {
                    movingMarkerB = updatedMarker
                }

                if ((isBusA && focusedBusName == "Bus A") || (!isBusA && focusedBusName == "Bus B")) {
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder().center(point).zoom(14.0).build()
                    )
                }

                handler.postDelayed(this, 3000)
            }
        }

        if (isBusA) {
            movingRunnableA = runnable
            handler.post(runnable)
        } else {
            movingRunnableB = runnable
            handler.post(runnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        movingRunnableA?.let { handler.removeCallbacks(it) }
        movingRunnableB?.let { handler.removeCallbacks(it) }
    }
}
