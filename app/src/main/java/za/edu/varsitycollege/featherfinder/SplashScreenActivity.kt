package za.edu.varsitycollege.featherfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button

class SplashScreenActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val launchAppBtn = findViewById<Button>(R.id.launch_app_btn)

        launchAppBtn.setOnClickListener {
            // Move to the login screen
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            //Display progress bar
            showProgressBar(resources.getString(R.string.still_loading))
            startActivity(intent)
            finish()
        }
    }
}
//////////////////////////////////////////////////// END OF CLASS ////////////////////////////////////////////////////