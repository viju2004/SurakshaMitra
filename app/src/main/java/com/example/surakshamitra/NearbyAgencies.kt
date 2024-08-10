package com.example.surakshamitra

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surakshamitra.RegisterUsers.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NearbyAgencies : AppCompatActivity() {
    private lateinit var adapter: AgencylistAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var dataList: MutableList<AgencyListDataModel>


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val markersMap: MutableMap<String, Marker?> = mutableMapOf()

    private val agencyTypeMap = mapOf(
        "National Disaster Response Force (NDRF)" to R.drawable.ndrf,
        "State Police Departments" to R.drawable.maharastrapol,
        "Fire Services" to R.drawable.firebrigade,
        "Emergency Medical Services (EMS)" to R.drawable.ems,
        "Indian Coast Guard" to R.drawable.indiancoastguard,
        "Indian Search and Rescue" to R.drawable.searchandrescue
    )
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_agencies)







        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val alertButton: Button = findViewById(R.id.alertButton)

        alertButton.setOnClickListener {

            val noOfTeams = findViewById<EditText>(R.id.noofteams).text.toString()
            val priority = findViewById<EditText>(R.id.seviority).text.toString()
            Log.d("values",noOfTeams+"   "+priority)
//            if(Integer.parseInt(noOfTeams)>5 || Integer.parseInt(noOfTeams)<=0 ){
//                Toast.makeText(this,"No of Teams should be between 1 - 5", Toast.LENGTH_SHORT)
//            }
//            else if(Integer.parseInt(priority)>3 || Integer.parseInt(priority)<=0){
//                Toast.makeText(this,"Priority should be between 1 - 3", Toast.LENGTH_SHORT)
//            }
//            else {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Alert !")
                    .setMessage("Sure, you want to send alert! .")
                    .setPositiveButton("Yes") { dialog, _ ->
                        showSMSAlert()
                        getCurrentLocationAndSendMessage()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
                startScaleAnimation(alertButton)

//            }


        }

        dataList = mutableListOf()


        recyclerView = findViewById(R.id.nearbyagencyrecyclerView)
        adapter = AgencylistAdapter(mutableListOf())
        recyclerView.layoutManager = GridLayoutManager(this, 1)

        recyclerView.adapter = adapter


        getDataListFromFirebase()


    }



    private fun startScaleAnimation(view: View) {
        val scaleAnimation = ScaleAnimation(
            1f, 1.1f, // Start and end scale values
            1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f // Pivot point of Y scaling
        )
        scaleAnimation.duration = 300 // Animation duration in milliseconds
        scaleAnimation.repeatCount = 3 // Number of times to repeat the animation
//        scaleAnimation.repeatMode = Animation.REVERSE // Reverse the animation after it completes

        view.startAnimation(scaleAnimation)
    }

    private fun getDataListFromFirebase() {
        val agenciesReference = FirebaseDatabase.getInstance().getReference("Agencies")

        agenciesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear() // Clear existing data before adding new data

                for (agencySnapshot in dataSnapshot.children) {
                    val agencyName = agencySnapshot.child("agencyName").getValue(String::class.java)
                    val agencyType = agencySnapshot.child("agencyType").getValue(String::class.java)
                    val agencyStatus = agencySnapshot.child("activeStaus").getValue(Boolean::class.java)

//                    if (agencyStatus != null) {
//                        Log.d("HEL", agencyStatus.toString())
//                    }
                    var agestatus = agencyStatus.toString()
                    if (agencyName != null && agencyType != null && agestatus!=null) {
                        // Use some logic to determine the imageResource, statusIconResource, and alertIconResource
                        val imageResource = agencyTypeMap[agencyType]?: R.drawable.age1
                        var statusIconResource = R.drawable.statusoff

                        if(agestatus == "true"){
                            statusIconResource = R.drawable.statuson
                        }

                        val alertIconResource = R.drawable.alerticon

                        val agencyListDataModel = AgencyListDataModel(imageResource, agencyName, statusIconResource, alertIconResource)
                        dataList.add(agencyListDataModel)
                    }
                }

                // Update the adapter with the new data
                adapter.setDataList(dataList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("getDataListFromFirebase", "Error: ${databaseError.message}")
            }
        })
    }

    fun goBack(view: View) {
        this.finish()
    }


    private fun getCurrentLocationAndSendMessage() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation: Location? = locationResult.lastLocation
                lastLocation?.let {
                    // Message to be sent

                    val mapsLink = "https://www.google.com/maps?q=${it.latitude},${it.longitude}"

                    val noOfTeams = findViewById<EditText>(R.id.noofteams).text.toString()
                    val priority = findViewById<EditText>(R.id.seviority).text.toString()
                        val message = "Panic Alert!\nHelp needed at: $mapsLink\n" +
                                "Number of Teams: $noOfTeams\nPriority: $priority\n" +
                                "Please reach us ASAP to the given coordinates."
                        sendMessageToAgencies(message)




                    // Send the message to all agencies
                }
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showSMSAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert Sent")
        builder.setMessage("Emergency alert has been sent to nearby agencies.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    private fun sendMessageToAgencies(message: String) {
        // Get reference to the "Agencies" node in Firebase Realtime Database
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Agencies")

        // Attach a ValueEventListener to retrieve data from the "Agencies" node
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Loop through each child node under "Agencies"
                for (agencySnapshot in dataSnapshot.children) {
                    val phoneNumber = agencySnapshot.child("phoneNumber").value.toString()

                    // Send the SMS with the current user's location
                    sendSMS(phoneNumber, message)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
//                 Toast.makeText(
//                    this,
//                    "Failed to retrieve data from Firebase",
//                    Toast.LENGTH_SHORT
//                ).show()
                Log.d("test","Failed")
            }
        })
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val sentIntent = Intent("SMS_SENT")
        val deliveredIntent = Intent("SMS_DELIVERED")

        val sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,
            PendingIntent.FLAG_IMMUTABLE)
        val deliveredPI = PendingIntent.getBroadcast(this, 0, deliveredIntent,
            PendingIntent.FLAG_IMMUTABLE)

        // Check for message length and split it into parts if necessary
        val parts = smsManager.divideMessage(message)
        val messageCount = parts.size

        // Send each part of the message
        for (i in 0 until messageCount) {
            // Send the SMS using the default SMS manager
            smsManager.sendTextMessage(phoneNumber, null, parts[i], sentPI, deliveredPI)
        }
    }

}