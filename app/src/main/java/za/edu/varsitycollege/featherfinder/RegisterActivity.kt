package za.edu.varsitycollege.featherfinder

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.edu.varsitycollege.featherfinder.model.User

 class RegisterActivity : BaseActivity()
 {
        private lateinit var fullNameTxt: TextInputEditText
        private lateinit var userNameTxt: TextInputEditText
        private lateinit var emailTxt: TextInputEditText
        private lateinit var contactTxt: TextInputEditText
        private lateinit var passwordTxt: TextInputEditText
        private lateinit var haveAccount: MaterialTextView
        private lateinit var regButton: MaterialButton
        private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\$@\$!%*#?&])[A-Za-z\\d\$@\$!%*#?&]{8,}\$")

        private lateinit var auth: FirebaseAuth
        private lateinit var firestore: FirebaseFirestore

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_register)

            // Initialize FirebaseAuth
            auth = FirebaseAuth.getInstance()

            // Initialize Firestore
            firestore = FirebaseFirestore.getInstance()

            fullNameTxt = findViewById(R.id.fullName_EditText)
            userNameTxt = findViewById(R.id.userNameEditText)
            emailTxt = findViewById(R.id.email_EditText)
            contactTxt = findViewById(R.id.contact_editText)
            passwordTxt = findViewById(R.id.password_ediText)
            haveAccount = findViewById(R.id.have_account_label)
            regButton = findViewById(R.id.register_button)

            haveAccount.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            regButton.setOnClickListener {
                if(validateRegistrationDetails()){
                    userRegistration()
                }

            }
        }

        //Validate all user entries
        private fun validateRegistrationDetails(): Boolean {
            return when {
                TextUtils.isEmpty(fullNameTxt.text.toString().trim { it <= ' ' }) ||
                        !fullNameTxt.text.toString().all { it.isLetterOrDigit() } -> {
                    showErrorSnackBar(resources.getString(R.string.full_name_error_message), true)
                    false
                }
                TextUtils.isEmpty(userNameTxt.text.toString().trim()) ||
                        !userNameTxt.text.toString().all { it.isLetterOrDigit() } -> {
                    showErrorSnackBar(resources.getString(R.string.username_error_message), true)
                    false
                }
                TextUtils.isEmpty(emailTxt.text.toString().trim { it <= ' ' }) ||
                        !emailTxt.text.toString().contains("@") -> {
                    showErrorSnackBar(resources.getString(R.string.email_error_message), true)
                    false
                }
                TextUtils.isEmpty(contactTxt.text.toString().trim { it <= ' ' }) ||
                        !TextUtils.isDigitsOnly(contactTxt.text.toString()) || contactTxt.length() != 9 -> {
                    showErrorSnackBar(resources.getString(R.string.contact_error_message), true)
                    false
                }
                TextUtils.isEmpty(passwordTxt.text.toString().trim { it <= ' ' }) ||
                        passwordTxt.length() < 8 || !passwordRegex.matches(passwordTxt.text.toString().trim()) -> {
                    showErrorSnackBar(resources.getString(R.string.password_error_message), true)
                    false
                }
                else -> true
            }
        }

        //Method to register a user with validation
        private fun userRegistration()
        {
              if (!::auth.isInitialized || !::firestore.isInitialized)
              {
                  showErrorSnackBar("Firebase authentication or Firestore not initialized", true)
                  return
              }
              showProgressBar(resources.getString(R.string.still_loading))
              if (validateRegistrationDetails())
              {
                  val userEmail = emailTxt.text.toString()
                  val userName = userNameTxt.text.toString()

                  // Check if the email already exists
                  firestore.collection("users")
                      .whereEqualTo("email", userEmail)
                      .get()
                      .addOnSuccessListener { result ->
                          if (!result.isEmpty)
                          {
                              hideProgressBar()
                              // Email already exists
                              showErrorSnackBar("User with this email already exists", true)
                          }
                          else
                          {
                              // Check if the username already exists
                              firestore.collection("users")
                                  .whereEqualTo("userName", userName)
                                  .get()
                                  .addOnSuccessListener { result ->
                                      if (!result.isEmpty)
                                      {
                                          hideProgressBar()
                                          // Username already exists
                                          showErrorSnackBar("User with this username already exists", true)
                                      }
                                      else
                                      {
                                          // Create a new user
                                          auth.createUserWithEmailAndPassword(userEmail, passwordTxt.text.toString())
                                              .addOnCompleteListener(this) { task ->
                                                  if (task.isSuccessful)
                                                  {
                                                      val user = auth.currentUser
                                                      if (user != null)
                                                      {
                                                          val userDetails = User(
                                                              fullName = fullNameTxt.text.toString(),
                                                              userName = userName,
                                                              email = userEmail,
                                                              contact = contactTxt.text.toString(),
                                                              password = passwordTxt.text.toString()
                                                          )

                                                          firestore.collection("users")
                                                              .document(user.uid)
                                                              .set(userDetails)
                                                              .addOnSuccessListener {
                                                                  showErrorSnackBar(resources.getString(R.string.account_created), false)
                                                                  hideProgressBar()
                                                                  // Move to the login screen when the user is registered
                                                                  val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                                                  startActivity(intent)
                                                                  finish()
                                                              }
                                                              .addOnFailureListener { e ->
                                                                  hideProgressBar()
                                                                  showErrorSnackBar("Error saving user details: ${e.message}", true)
                                                              }
                                                      }
                                                      else
                                                      {
                                                          showErrorSnackBar("User is null", true)
                                                      }
                                                  }
                                                  else
                                                  {
                                                      hideProgressBar()
                                                      showErrorSnackBar("Registration failed: ${task.exception?.message}", true)
                                                  }
                                              }
                                      }
                                  }.addOnFailureListener { e ->
                                      hideProgressBar()
                                      showErrorSnackBar("Error checking email: ${e.message}", true)
                                  }
                          }
                      }.addOnFailureListener { e ->
                          hideProgressBar()
                          showErrorSnackBar("Error checking email: ${e.message}", true)
                      }

          }

               }

        //Method to clear text boxes when the user is created and save in firebase
        private fun clearEditTextFields() {
            fullNameTxt.text?.clear()
            userNameTxt.text?.clear()
            emailTxt.text?.clear()
            contactTxt.text?.clear()
            passwordTxt.text?.clear()
        }
 }
/////////////////////////////////////////////////////////// END OF CLASS ///////////////////////////////////////////////////////////
