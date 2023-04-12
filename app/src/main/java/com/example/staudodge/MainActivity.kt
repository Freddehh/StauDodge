package com.example.staudodge

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.staudodge.databinding.ActivityMainBinding
import com.example.staudodge.enums.CategoryEnum
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

/**
 * This is the main class where everything is shown. It handles the UI and the API requests.
 */

class MainActivity : AppCompatActivity() {
    val locationPermissionCode = 2

    private lateinit var recyclerView: RecyclerView
    private lateinit var incidentList: ArrayList<Incident>
    private var lastIncidentList: ArrayList<Incident> = ArrayList()
    private lateinit var incidentAdapter: IncidentAdapter

    private lateinit var permissions: Permissions

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationServiceClass: LocationServiceClass

    private var lat: Double = 0.0
    private var lon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeRecyclerView()

        locationServiceClass = LocationServiceClass()
        permissions = Permissions(this)
        permissions.fetchLocationPermission()

        if (permissions.checkPermission()) {
            locationServiceClass.initialize(this)
            locationServiceClass.updateLocation()
        }

        binding.btnUpdate.setOnClickListener {
            if (permissions.checkPermission()) {
                locationServiceClass.updateLocation()
            } else {
                permissions.fetchLocationPermission()
            }
        }

        binding.btnSort.setOnClickListener {
            sortByPriorityAscending()
        }
    }

    private fun initializeRecyclerView() {
        recyclerView = binding.incidentRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            this, RecyclerView.HORIZONTAL, false
        )
        incidentList = ArrayList()
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    //The result of the permission request. If permission is given it starts the process of getting incidents.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                locationServiceClass.initialize(this)
                locationServiceClass.updateLocation()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sortByPriorityAscending() {
        if (incidentList.isNotEmpty()) {
            val sortedByDescending = incidentList.sortedByDescending {
                it.priority
            }

            val sortedByAscending = sortedByDescending.reversed()

            incidentAdapter = IncidentAdapter(sortedByAscending)
            recyclerView.adapter = incidentAdapter

        } else {
            Toast.makeText(this, "Can't sort nothing?!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *  This function gets lat and lon from the last known location then uses that to get the
     *  location required from sr's api. Then it uses that location for the next api call to get all
     *  incidents in connected to that location. If there are more than 10 it makes a new api call
     *  to get all of the incidents available and saves it in incidentList. Then the list gets
     *  compared with lastIncidentList and if there not the same it displays the new one.
     */
    fun updateIncidents() {
        CoroutineScope(Dispatchers.Default).launch {

            if (locationServiceClass.lastLocation == null) {
                return@launch
            }
            lat = locationServiceClass.lastLocation?.latitude!!
            lon = locationServiceClass.lastLocation?.longitude!!

            try {
                //Fetching location with lat and lon
                val locationJSON = JSONObject(
                    URL(
                        "https://api.sr.se/api/v2/traffic/areas?latitude=$lat&longitude=$lon&format=json"
                    ).readText()
                )

                val locationFromJSON = locationJSON.getJSONObject(
                    "area"
                ).getString("name").toString()

                //Fetching incidents with location
                val incidentsJSON = JSONObject(
                    URL(
                        "https://api.sr.se/api/v2/traffic/messages?format=json&trafficareaname=$locationFromJSON"
                    ).readText()
                )

                var totalHits = incidentsJSON.getJSONObject("pagination").getInt("totalhits")
                val totalHitsToShow = totalHits

                var incidents = incidentsJSON.getJSONArray("messages")

                incidentList = ArrayList()

                for (i in 0 until incidents.length()) {
                    val incident = incidents.getJSONObject(i)
                    incidentList.add(
                        Incident(
                            incident.getInt("priority"),
                            incident.getString("title"),
                            incident.getString("description"),
                            CategoryEnum.values()[incident.getInt("category")].category,
                        )
                    )
                }

                //If there is more than one page
                while (totalHits > 10) {
                    val incidentsJSONNextPage = JSONObject(
                        URL(
                            incidentsJSON.getJSONObject(
                                "pagination"
                            ).getString("nextpage")
                        ).readText()
                    )

                    val incidentsNextPage = incidentsJSONNextPage.getJSONArray("messages")

                    for (i in 0 until incidentsNextPage.length()) {
                        val incident = incidentsNextPage.getJSONObject(i)
                        incidentList.add(
                            Incident(
                                incident.getInt("priority"), incident.getString("title"),
                                incident.getString("description"),
                                CategoryEnum.values()[incident.getInt("category")].category
                            )
                        )
                    }

                    totalHits -= 10
                }

                if (lastIncidentList != incidentList) {
                    runOnUiThread {
                        incidentAdapter = IncidentAdapter(incidentList)
                        recyclerView.adapter = incidentAdapter
                        binding.tvLocation.text = locationFromJSON
                        binding.tvNumberOfIncidents.text = (buildString {
                            append(getString(R.string.nr_of_incidents))
                            append(" ")
                            append(totalHitsToShow)
                        })
                    }

                    lastIncidentList = incidentList
                }

            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}