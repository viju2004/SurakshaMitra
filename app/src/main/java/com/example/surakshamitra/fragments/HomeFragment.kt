import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surakshamitra.LoginSignupScreen
import com.example.surakshamitra.MyAdapter
import com.example.surakshamitra.MyDataModel
import com.example.surakshamitra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment() {

    private val PREFS_NAME = "MyPrefsFile"
    private lateinit var adapter: MyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth

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
        val view = inflater.inflate(R.layout.dashboard_fragment, container, false)

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = MyAdapter(requireContext(), mutableListOf()) // Initialize the adapter with an empty list
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = adapter

        // Set the click listener for the button in the fragment
        view.findViewById<View>(R.id.buttonShowMenu).setOnClickListener {
            showPopupMenu(view)
        }

        // Fetch data from Firebase and update the RecyclerView
        getDataListFromFirebase()

        return view
    }

    // Function to display a toast message
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Function to be called when the button is clicked
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view.findViewById<View>(R.id.buttonShowMenu))
        popup.menuInflater.inflate(R.menu.menu_main, popup.menu)

        // Show the popup menu
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.action_logout -> {
                    showToast("Logout")

                    auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginSignupScreen::class.java)
                    startActivity(intent)

                    // Add your logout logic here
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // Save the state of the switch to SharedPreferences
    private fun saveSwitchState(switchKey: String, isChecked: Boolean) {
        val sharedPref =
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(switchKey, isChecked)
            apply()
        }
    }

    private fun getDataListFromFirebase() {
        // Assuming "Agencies" is your DatabaseReference pointing to the root of your Agencies in the Firebase Realtime Database
        val agenciesReference = FirebaseDatabase.getInstance().getReference("Agencies")

        agenciesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<MyDataModel>()

                for (agencySnapshot in dataSnapshot.children) {
                    val agencyName = agencySnapshot.child("agencyName").getValue(String::class.java)
                    val agencyType = agencySnapshot.child("agencyType").getValue(String::class.java)

                    // Check if both agencyName and agencyType are not null before proceeding
                    if (agencyName != null && agencyType != null) {
                        val imageResource = agencyTypeMap[agencyType] ?: R.drawable.age1
                        val myDataModel = MyDataModel(imageResource, agencyName)
                        dataList.add(myDataModel)
                    }
                }

                // Update the adapter with the new data
                adapter.setDataList(dataList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
                Log.e("getDataListFromFirebase", "Error: ${databaseError.message}")
            }
        })
    }
}

