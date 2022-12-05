package cp.umons.testAndroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(){

    // The icon to access the menu from TODO
    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the drawerLayout and the Navigation View items
        val drawerLayout : DrawerLayout = findViewById(R.id.nav_menu)
        val navView : NavigationView = findViewById(R.id.navView)

        // Set the icon and menu state
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // The menu items listener
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.pompes_menu -> Toast.makeText(applicationContext, "Pompes clicked", Toast.LENGTH_SHORT).show()
                R.id.boissons_menu -> Toast.makeText(applicationContext, "Boissons clicked", Toast.LENGTH_SHORT).show()
                R.id.cocktail_menu -> Toast.makeText(applicationContext, "Cocktail clicked", Toast.LENGTH_SHORT).show()
                R.id.verser_menu -> Toast.makeText(applicationContext, "Verser clicked", Toast.LENGTH_SHORT).show()
                R.id.POWER -> this.finishAffinity()
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}