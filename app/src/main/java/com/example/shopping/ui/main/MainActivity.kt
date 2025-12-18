package com.example.shopping.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.shopping.R
import com.example.shopping.databinding.ActivityMainBinding
import com.example.shopping.ui.login.LoginActivity
import com.example.shopping.utils.UserSession

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private var showFavoriteMenu = true

    lateinit var productActionBar: LinearLayout
    lateinit var btnAddToCart: TextView
    lateinit var btnBuyNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)

        productActionBar = binding.productActionBar
        btnAddToCart = binding.btnAddToCart
        btnBuyNow = binding.btnBuyNow

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                    as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_cart,
                R.id.navigation_notifications,
                R.id.navigation_user
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)


        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_user -> {
                    if (UserSession.isLogin) {
                        navController.navigate(R.id.navigation_user)
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
        val goSetPassword = intent.getBooleanExtra("goSetPassword", false)
        if (goSetPassword) {
            navController.navigate(R.id.setPasswordFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.favorite,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_favorite)?.isVisible = showFavoriteMenu
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                if (UserSession.isLogin){
                    navController.navigate(R.id.favoriteFragment)
                }else{
                    Toast.makeText(this,"請先登入!!",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun navigateToHome() {
        binding.navView.selectedItemId = R.id.navigation_home
    }

    fun setFavoriteMenuVisible(visible: Boolean) {
        showFavoriteMenu = visible
        invalidateOptionsMenu()
    }
    fun showHomeToolbar() {
        binding.searchBar.searchBarRoot.visibility = View.VISIBLE
        binding.toolbar.title = "GGSHOP"
        binding.toolbar.navigationIcon = null
    }

    fun getSearchInput(): EditText {
        return binding.searchBar.searchInput
    }
    fun clearSearch(){
        binding.searchBar.searchInput.setText("")
    }
    fun showSimpleTitle(title: String) {
        binding.searchBar.searchBarRoot.visibility = View.GONE
        binding.toolbar.title = title
        binding.toolbar.navigationIcon = null
    }

    fun showProductToolbar(title: String) {
        binding.searchBar.searchBarRoot.visibility = View.GONE
        binding.toolbar.title = title
        binding.toolbar.setNavigationIcon(R.drawable.back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun showProductActionBar() {
        productActionBar.visibility = View.VISIBLE
    }
    fun hideProductActionBar() {
        productActionBar.visibility = View.GONE
    }
    fun showAuthToolbar(title: String) {
        binding.searchBar.searchBarRoot.visibility = View.GONE

        binding.toolbar.title = title

        binding.toolbar.navigationIcon = null

        productActionBar.visibility = View.GONE

        binding.navView.visibility = View.GONE
    }

}
