package com.example.surakshamitra

import android.Manifest
import android.content.pm.PackageManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.surakshamitra.data.UserRegistrationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class RegisterUsers : AppCompatActivity() {

    private lateinit var usernameInp: String
    private lateinit var agencyNameInp: EditText
    private lateinit var agencyTypeInp: Spinner
    private lateinit var agencyAddressInp: EditText
    private lateinit var agencyPhoneNoInp: EditText
    private lateinit var agencyTotalMembersInp: EditText
    private lateinit var agencypassword: EditText
    private lateinit var agencycnfpassword: EditText
    private lateinit var agencyRegisterBtn: Button
    private lateinit var registrationfromExitBtn: Button
    private lateinit var selectedAgencyType: String
    private lateinit var agencyEmail: EditText
    private lateinit var userRegistrationData: UserRegistrationData
    private lateinit var mAuth: FirebaseAuth
    private var userLatitude: String = "0.00"
    private var userLongitude: String = "0.00"
    private var verificationDocUrl: String = ""
    private var userStatus: Boolean = false
    private lateinit var uploadButton: Button
    private lateinit var storageRef: StorageReference
    private val PICK_IMAGE_REQUEST = 1

    // For location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_users)

        // For location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storageRef = FirebaseStorage.getInstance().reference

        initializeViews()
        setupAgencyTypeSpinner()

        registrationfromExitBtn.setOnClickListener {
            finish()
        }

        agencyRegisterBtn.setOnClickListener {
            createUserAndSignUp()
        }
        uploadButton.setOnClickListener { chooseImage() }
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val filePath = data.data!!
            uploadImage(filePath)
        }
    }
    private fun uploadImage(imageUri: Uri?) {
        imageUri ?: return // Check if imageUri is null

        val fileName = "${UUID.randomUUID()}.jpg"
        val storageReference = FirebaseStorage.getInstance().reference.child("Agency Verifications Docs/$fileName")

        // Create and show a progress dialog
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.putFile(imageUri)
            .addOnProgressListener { taskSnapshot ->
                // Update progress
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressDialog.progress = progress
            }
            .addOnSuccessListener { taskSnapshot ->
                // Image upload successful
                progressDialog.dismiss() // Dismiss the progress dialog
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                     verificationDocUrl = uri.toString()

                    // Save the URL in Firestore or any other persistent storage
                    saveImageUrlToFirestore(verificationDocUrl)



                    Toast.makeText(applicationContext, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    Toast.makeText(applicationContext, verificationDocUrl, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                // Handle upload failures
                progressDialog.dismiss() // Dismiss the progress dialog
                // ...
            }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        // Assuming you have initialized Firestore elsewhere in your code
        val firestore = FirebaseFirestore.getInstance()

        // Add code to save the URL to a Firestore collection/document
        // For example:
        val data = hashMapOf("imageUrl" to imageUrl)
        firestore.collection("verificationDocs").add(data)
            .addOnSuccessListener { documentReference ->
                // Document added successfully
                // You can add further handling if needed
            }
            .addOnFailureListener { e ->
                // Handle failures
                // ...
            }
    }




    private fun saveUserDataLocally() {
        val userDataJson = userRegistrationData.toJson()
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userData", userDataJson)
        editor.apply()
    }

    private fun initializeViews() {
        agencyNameInp = findViewById(R.id.agency_name_edit_text)
        agencyTypeInp = findViewById(R.id.agency_type)
        agencyAddressInp = findViewById(R.id.address_edit_text)
        agencyPhoneNoInp = findViewById(R.id.phone_number_edit_text)
        agencyTotalMembersInp = findViewById(R.id.total_members_edit_text)
        agencypassword = findViewById(R.id.password_edit_text)
        agencycnfpassword = findViewById(R.id.confirm_password_edit_text)
        agencyRegisterBtn = findViewById(R.id.register_button)
        registrationfromExitBtn = findViewById(R.id.exitbutton)
        agencyEmail = findViewById(R.id.email_id)
        uploadButton = findViewById(R.id.uploadImgBtn)
    }

    private fun setupAgencyTypeSpinner() {
        val agencyTypes = arrayOf(
            "National Disaster Response Force (NDRF)",
            "State Police Departments",
            "Fire Services",
            "Indian Search and Rescue",
            "Emergency Medical Services (EMS)",
            "Indian Coast Guard"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, agencyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        agencyTypeInp.adapter = adapter

        agencyTypeInp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAgencyType = agencyTypes[position]
                val agencyTypeSpinner: Spinner = findViewById(R.id.agency_type)
                agencyTypeSpinner.prompt = "Selected Agency Type: $selectedAgencyType"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case when nothing is selected (optional)
            }
        }
    }

    private fun createUserAndSignUp() {
        // To get the user's location
        if (checkLocationPermission()) {
            // Permissions are already granted, get the current location
            getCurrentLocation(object : LocationCallback {
                override fun onLocationReceived(latitude: Double, longitude: Double) {
                    // Location received, proceed with user creation and sign-up
                    userLatitude = latitude.toString()
                    userLongitude = longitude.toString()
                    performUserSignUp()
                }

                override fun onLocationError(error: String) {
                    // Handle location error if needed
                    showAlertDialog("Location Error", "Failed to get location: $error")
                }
            })
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun checkLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getCurrentLocation(callback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            callback.onLocationError("Location permissions not granted")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some situations, this can be null.
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    callback.onLocationReceived(latitude, longitude)
                } else {
                    callback.onLocationError("Last known location is null")
                }
            }
    }

    private fun performUserSignUp() {
        val email = agencyEmail.text.toString()
        val password = agencypassword.text.toString()
        val confirmPassword = agencycnfpassword.text.toString()
        val phoneNumber = agencyPhoneNoInp.text.toString()

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
            phoneNumber.isBlank() || !isValidPhoneNumber(phoneNumber) || !isValidEmail(email)
        ) {
            showAlertDialog(
                "Validation Error",
                getValidationErrorMessage(email, password, confirmPassword, phoneNumber)
            )
            return
        }

        if (password != confirmPassword) {
            showAlertDialog("Validation Error", "Passwords do not match")
            return
        }

        if (password.contains(" ")) {
            showAlertDialog("Validation Error", "Password should not contain spaces")
            return
        }

        val index = email.indexOf('@')
        usernameInp = email.substring(0, index)
        userRegistrationData = UserRegistrationData(
            username = usernameInp,
            authDocument = verificationDocUrl,
            agencyName = agencyNameInp.text.toString(),
            agencyType = selectedAgencyType,
            address = agencyAddressInp.text.toString(),
            phoneNumber = phoneNumber,
            emailAddress = email,
            totalMembers = agencyTotalMembersInp.text.toString(),
            password = password,
            activeStatus = userStatus,
            latitude = userLatitude,
            longitude = userLongitude
        )

        signUpUser(email, password)
    }

    private fun getValidationErrorMessage(
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String
    ): String {
        val errorMessage = StringBuilder("Please fill in the following fields with valid information:\n")

        if (email.isBlank() || !isValidEmail(email)) {
            errorMessage.append("- Email\n")
        }
        if (password.isBlank()) {
            errorMessage.append("- Password\n")
        }
        if (confirmPassword.isBlank()) {
            errorMessage.append("- Confirm Password\n")
        }
        if (password != confirmPassword) {
            errorMessage.append("- Passwords do not match\n")
        }
        if (phoneNumber.isBlank() || !isValidPhoneNumber(phoneNumber)) {
            errorMessage.append("- Phone Number\n")
        }

        return errorMessage.toString()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Simple validation for demonstration purposes
        return phoneNumber.matches(Regex("\\d{10}"))
    }

    private fun isValidEmail(email: String): Boolean {
        // Simple validation for demonstration purposes
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showAlertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun signUpUser(email: String, password: String) {
        mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User Created", Toast.LENGTH_SHORT).show()
                    saveUserDataToDatabase()
                    saveUserDataLocally()  // Save data locally
                    navigateToLoginScreen()
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun saveUserDataToDatabase() {
        if (agencypassword.text.toString() == agencycnfpassword.text.toString()) {
            val database = FirebaseDatabase.getInstance().reference
            val usersReference = database.child("Agencies")

            usersReference.child(userRegistrationData.username).setValue(userRegistrationData.toMap())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Registration Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            clearPasswordFields()
            Toast.makeText(applicationContext, "Check the password", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToLoginScreen() {
        intent = Intent(applicationContext, LoginScreen::class.java)
        startActivity(intent)
    }

    private fun handleRegistrationError(exception: Exception?) {
        when {
            exception is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "User Creation failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearPasswordFields() {
        agencypassword.text.clear()
        agencycnfpassword.text.clear()
    }

    private interface LocationCallback {
        fun onLocationReceived(latitude: Double, longitude: Double)
        fun onLocationError(error: String)
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
