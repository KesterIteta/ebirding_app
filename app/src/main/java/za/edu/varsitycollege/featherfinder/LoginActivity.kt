package za.edu.varsitycollege.featherfinder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import android.text.TextUtils
import android.widget.Toast

class LoginActivity : BaseActivity() {

    private lateinit var emailText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var forgotPassword: MaterialTextView
    private lateinit var registerBtn: MaterialTextView

    // Initialize FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val PERMISSION_REQUEST_CODE = 1
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Giving permission for access
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }

        emailText = findViewById(R.id.et_email)
        passwordText = findViewById(R.id.edt_password)
        forgotPassword = findViewById(R.id.forgot_password)
        loginButton = findViewById(R.id.btn_login)
        registerBtn = findViewById(R.id.text_register)

        registerBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ChangePassword::class.java)
            //Display progress bar
            showProgressBar(resources.getString(R.string.still_loading))
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {

            loginUser()
        }
    }

    private fun loginUser() {
        val email = emailText.text.toString().trim()
        val password = passwordText.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showErrorSnackBar(resources.getString(R.string.enter_email_and_password), true)
            return
        }
        //Display progress bar
        showProgressBar(resources.getString(R.string.still_loading))
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Login Success message
                    showToast(resources.getString(R.string.logged_in_success) + "ID: ${user?.uid}")
                    // Move to the maps screen
                    val intent = Intent(this@LoginActivity, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    hideProgressBar()
                    // If sign-in fails, display a message to the user.
                    showErrorSnackBar(resources.getString(R.string.login_err_message), true)
                }
            }
        }

    // Using toast to display message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}