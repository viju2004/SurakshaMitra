
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.surakshamitra.R
import com.example.surakshamitra.data.UserRegistrationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class AgencyInfo {

    interface DataCallback {
        fun onDataLoaded(userData: UserRegistrationData)
    }

    private lateinit var email: String
    private var userName: TextView
    private var context: Context
    private var agencyName: TextView
    private var agencyType: TextView
    private var agencyStrength: TextView
    private var agencyContactDetails: TextView
    private var agencyLocation: TextView
    private var agencyProfileImg: ImageView

    private val agencyTypeMap = mapOf(
        "National Disaster Response Force (NDRF)" to R.drawable.ndrf,
        "State Police Departments" to R.drawable.maharastrapol,
        "Fire Services" to R.drawable.firebrigade,
        "Emergency Medical Services (EMS)" to R.drawable.ems,
        "Indian Coast Guard" to R.drawable.indiancoastguard,
        "Indian Search and Rescue" to R.drawable.searchandrescue
    )

    constructor(context: Context, userName: TextView, agencyName: TextView, agencyType: TextView,
                agencyStrength: TextView, agencyContactDetails: TextView,
                agencyLocation: TextView, agencyProfileImg: ImageView) {
        this.context = context
        this.userName = userName
        this.agencyName = agencyName
        this.agencyType = agencyType
        this.agencyStrength = agencyStrength
        this.agencyContactDetails = agencyContactDetails
        this.agencyLocation = agencyLocation
        this.agencyProfileImg = agencyProfileImg
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    fun getLocalUserData(callback: DataCallback) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userDataJson = sharedPreferences.getString("userData", null)

        if (userDataJson != null) {
            val userData = Gson().fromJson(userDataJson, UserRegistrationData::class.java)
            callback.onDataLoaded(userData)
        }
    }

    fun updateUI(userData: UserRegistrationData) {
        agencyTypeMap[userData.agencyType]?.let { agencyProfileImg.setImageResource(it) }

        userName.text = email.substring(0, email.indexOf('@'))
        agencyName.text = userData.agencyName
        agencyType.text = userData.agencyType
        agencyStrength.text = userData.totalMembers
        agencyContactDetails.text = userData.phoneNumber
        agencyLocation.text = userData.address
    }

    fun getAgencyInfo(callback: DataCallback) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            email = currentUser.email.toString()
            Log.d("error", "Logged in user's email: $email")
        } else {
            Log.d("error", "No user is currently logged in")
        }

        val index = email.indexOf('@')
        val usernameInp = email.substring(0, index)
        val database = FirebaseDatabase.getInstance().reference
        val userReference = database.child("Agencies").child(usernameInp)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserRegistrationData::class.java)

                if (userData != null) {
                    callback.onDataLoaded(userData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Database error: ${error.message}")
            }
        })
    }
}
