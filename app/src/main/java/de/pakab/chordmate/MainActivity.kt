package de.pakab.chordmate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import de.pakab.chordmate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SpotifyWebControl.start(this)
        SpotifyRemoteControl.start(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navController = supportFragmentManager.findFragmentById(R.id.fragment)!!.findNavController()
        setupActionBarWithNavController(navController)
        toolbar.setNavigationOnClickListener { navController.popBackStack() }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SpotifyRemoteControl.requestCode) {
            SpotifyRemoteControl.onRequestReturns(this, resultCode, data)
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyRemoteControl.stop()
    }
}
