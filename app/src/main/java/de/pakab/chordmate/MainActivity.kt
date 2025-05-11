package de.pakab.chordmate

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.pakab.chordmate.SongsAdapter.OnClickListener
import de.pakab.chordmate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val songList: MutableList<Song> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val rvSongs = findViewById<RecyclerView>(R.id.rv_songs)
        binding.fab.setOnClickListener { view ->
            songList.add(Song())
            rvSongs.adapter?.notifyDataSetChanged()
        }

        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter =
            SongsAdapter(
                songList,
                object : OnClickListener() {
                    override fun onClick(song: Song) {
                        startActivity(Intent(this@MainActivity, SongActivity::class.java))
                    }
                },
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean = false
}
