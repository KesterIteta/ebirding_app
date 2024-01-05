package za.edu.varsitycollege.featherfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePassword : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<TextInputEditText>(R.id.email_EditText)
        val newPasswordEditText = findViewById<TextInputEditText>(R.id.new_password_ediText)
        val resetButton = findViewById<Button>(R.id.submit_button)

        // Reset password
        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                // Check if the user exists in Firestore
                firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // User found, initiate password reset
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful)
                                    {
                                        showErrorSnackBar(
                                            resources.getString(R.string.reset_password_message),
                                            false
                                        )
                                        // Launch Login Activity if password reset successfully
                                        val intent = Intent(
                                            this@ChangePassword,
                                            LoginActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                    }
                                    else
                                    {
                                        showErrorSnackBar(
                                            resources.getString(R.string.reset_err_message),
                                            true
                                        )
                                    }
                                }
                            }
                            else
                            {
                            // User not found in Firestore
                            showErrorSnackBar(
                                resources.getString(R.string.user_not_found),
                                true
                            )
                        }
                    }
                    .addOnFailureListener {
                        showErrorSnackBar(
                            resources.getString(R.string.reset_err_message),
                            true
                        )
                    }
                }
                else
                {
                    showErrorSnackBar(resources.getString(R.string.please_enter_email), true)
                }
            }
        }
    }
