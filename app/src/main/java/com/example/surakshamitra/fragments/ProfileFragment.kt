// ProfileFragment.kt

package com.example.surakshamitra.fragments

import AgencyInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.example.surakshamitra.R
import com.example.surakshamitra.data.UserRegistrationData

class ProfileFragment : Fragment(), AgencyInfo.DataCallback {

    private lateinit var agencyName: TextView
    private lateinit var userName: TextView
    private lateinit var agencyType: TextView
    private lateinit var agencyStrength: TextView
    private lateinit var agencyContactDetails: TextView
    private lateinit var agencyLocation: TextView
    private lateinit var agencyProfileImg: ImageView

    private val agencyTypeMap = mapOf(
        "National Disaster Response Force (NDRF)" to R.drawable.ndrf,
        "State Police Departments" to R.drawable.maharastrapol,
        "Fire Services" to R.drawable.firebrigade,
        "Emergency Medical Services (EMS)" to R.drawable.ems,
        "Indian Coast Guard" to R.drawable.indiancoastguard,
        "Indian Search and Rescue" to R.drawable.searchandrescue
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_agency_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialization of UI components
        userName = view.findViewById(R.id.agencyinfo_username)
        agencyName = view.findViewById(R.id.infoagencyName)
        agencyType = view.findViewById(R.id.infoagencyType)
        agencyStrength = view.findViewById(R.id.infoagencyTotalmembers)
        agencyContactDetails = view.findViewById(R.id.infoagencyContactNO)
        agencyLocation = view.findViewById(R.id.infoagencyAdd)
        agencyProfileImg = view.findViewById(R.id.agencyinfo_profilImg)

        // Check connectivity and fetch data accordingly
        fetchData()
    }

    private fun fetchData() {
        val agencyInfo = AgencyInfo(requireContext(),userName, agencyName, agencyType,
            agencyStrength, agencyContactDetails, agencyLocation, agencyProfileImg)

        if (agencyInfo.isInternetAvailable()) {
            agencyInfo.getAgencyInfo(object : AgencyInfo.DataCallback {
                override fun onDataLoaded(userData: UserRegistrationData) {
                    updateUI(userData)
                }
            })
        } else {
            agencyInfo.getLocalUserData(object : AgencyInfo.DataCallback {
                override fun onDataLoaded(userData: UserRegistrationData) {
                    updateUI(userData)
                }
            })
        }
    }

    private fun updateUI(userData: UserRegistrationData) {
        agencyTypeMap[userData.agencyType]?.let { agencyProfileImg.setImageResource(it) }
        userName.text = userData.emailAddress.substring(0, userData.emailAddress.indexOf('@'))
        agencyName.text = userData.agencyName
        agencyType.text = userData.agencyType
        agencyStrength.text = userData.totalMembers
        agencyContactDetails.text = userData.phoneNumber
        agencyLocation.text = userData.address
    }

    override fun onDataLoaded(userData: UserRegistrationData) {
        // Handle data loaded callback if needed
    }
}
