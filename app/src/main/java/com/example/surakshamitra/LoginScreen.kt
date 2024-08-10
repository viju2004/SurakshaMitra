package com.example.surakshamitra
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.surakshamitra.data.UserRegistrationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginScreen : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var donthaveac: TextView
    private lateinit var email: String
    private lateinit var userRegistrationData: UserRegistrationData

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        mAuth = FirebaseAuth.getInstance()

        // Check if the user is already logged in
        if (mAuth.currentUser != null) {
            // User is already logged in, redirect to the dashboard
            startDashboardActivity()
            return
        }

        // Initialization
        username = findViewById(R.id.loginUserName)
        password = findViewById(R.id.loginPassword)
        loginBtn = findViewById(R.id.loginButton2)
        donthaveac = findViewById(R.id.donthaveac)

        donthaveac.setOnClickListener {
            intent = Intent(applicationContext, RegisterUsers::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val userEmail = username.text.toString()
            val userPassword = password.text.toString()

            if (validateInputs(userEmail, userPassword)) {
                authenticateUser(userEmail, userPassword)
            }
        }
    }

    private fun startDashboardActivity() {
        val intent = Intent(applicationContext, DashBoard::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent the user from coming back to the login screen
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showAlert("Invalid Email")
            return false
        }

        if (password.isEmpty()) {
            showAlert("Password cannot be empty")
            return false
        }

        // You can add more specific validation checks for the password if needed

        return true
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Validation Error")
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun authenticateUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    getAgencyInfo()
                    startDashboardActivity()
                } else {
                    // If the task is not successful, handle different scenarios
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        // The user account does not exist
                        showAlert("User account not found. Please register.")
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid password
                        showAlert("Wrong password. Please try again.")
                    } else {
                        // General authentication failure
                        showAlert("Authentication failed. Please try again.")
                    }
                }
            }
    }

    private fun getAgencyInfo() {
        //photo mapping

        //get user
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            email = currentUser.email.toString()
            Log.d("error", "Logged in user's email: $email")
            // Use the email for any actions or display
        } else {
            Log.d("error", "No user is currently logged in")
        }
        val index = email.indexOf('@')
        val usernameInp = email.substring(0, index)
        val database = FirebaseDatabase.getInstance().reference
        val userReference = database.child("Agencies").child(usernameInp)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Data retrieved successfully
                val userData = snapshot.getValue(UserRegistrationData::class.java)

                if (userData != null) {
                    userRegistrationData = UserRegistrationData(
                        username = usernameInp,
                        agencyName = userData.agencyName,
                        agencyType = userData.agencyType,
                        address = userData.address,
                        phoneNumber = userData.phoneNumber,
                        emailAddress = userData.emailAddress,
                        totalMembers = userData.totalMembers,
                        password = userData.password
                    )
                    saveUserDataLocally()
                    Toast.makeText(applicationContext,"Data Saved Locally", Toast.LENGTH_LONG).show()


                }

                // Use the userData object to populate your UI elements
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("error", "Database error: ${error.message}")
            }
        })
    }
    private fun saveUserDataLocally() {
        val userDataJson = userRegistrationData.toJson()
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userData", userDataJson)
        editor.apply()
    }

}
