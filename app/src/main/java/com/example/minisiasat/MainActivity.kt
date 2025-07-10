package com.example.minisiasat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
// --- PERBAIKAN DI SINI: Tambahkan import yang hilang ---
// --- AKHIR PERBAIKAN ---
import com.example.minisiasat.domain.model.Users
import com.example.minisiasat.ui.auth.LoginActivity
import com.example.minisiasat.ui.coursecard.CourseCardFragment
import com.example.minisiasat.ui.gradereport.GradeReportFragment
import com.example.minisiasat.ui.home.HomeDosenFragment
import com.example.minisiasat.ui.home.HomeMahasiswaFragment
import com.example.minisiasat.ui.period.PeriodsListFragment
import com.example.minisiasat.ui.registration.EnrollmentFragment
import com.example.minisiasat.ui.schedule.AddScheduleFragment
import com.example.minisiasat.ui.schedule.CourseGradingFragment
import com.example.minisiasat.ui.schedule.ScheduleFragment
import com.example.minisiasat.ui.schedule.LecturerScheduleFragment
import com.example.minisiasat.ui.transcript.TranscriptFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var users: Users
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        users = intent.getSerializableExtra("user") as? Users ?: run {
            finish()
            return
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val logoutButton = navView.findViewById<MaterialButton>(R.id.logout_button)
        logoutButton.setOnClickListener {
            logoutUser()
        }
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        setupDrawerMenu()

        // Set user info in header
        val header = navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.nav_header_name).text = users.name
        header.findViewById<TextView>(R.id.nav_header_email).text = users.email


        if (savedInstanceState == null) {
            showHome()
        }
    }

    private fun setupDrawerMenu() {
        val menu: Menu = navView.menu
        menu.findItem(R.id.nav_registrasi).isVisible = false
        menu.findItem(R.id.nav_kartu_studi).isVisible = false
        menu.findItem(R.id.nav_jadwal_kuliah).isVisible = false
        menu.findItem(R.id.nav_hasil_studi).isVisible = false
        menu.findItem(R.id.nav_transkrip).isVisible = false
        menu.findItem(R.id.nav_jadwal_mengajar).isVisible = false
        menu.findItem(R.id.nav_input_nilai).isVisible = false
        menu.findItem(R.id.nav_input_jadwal).isVisible = false

        when (users.role) {
            "mahasiswa" -> {
                menu.findItem(R.id.nav_registrasi).isVisible = true
                menu.findItem(R.id.nav_kartu_studi).isVisible = true
                menu.findItem(R.id.nav_jadwal_kuliah).isVisible = true
                menu.findItem(R.id.nav_hasil_studi).isVisible = true
                menu.findItem(R.id.nav_transkrip).isVisible = true
            }
            "dosen" -> {
                menu.findItem(R.id.nav_jadwal_mengajar).isVisible = true
                menu.findItem(R.id.nav_input_nilai).isVisible = true
                if (users.position?.uppercase() == "KAPRODI") {
                    menu.findItem(R.id.nav_input_jadwal).isVisible = true
                    navView.menu.findItem(R.id.nav_manage_period).isVisible = true
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_home -> showHome()
            R.id.nav_input_jadwal -> showAddScheduleFragment()
            R.id.nav_jadwal_mengajar -> showLecturerSchedule()
            R.id.nav_input_nilai -> showGradeReportingFragment()
            R.id.nav_registrasi -> showEnrollmentFragment()
            R.id.nav_kartu_studi -> showCourseCardFragment()
            R.id.nav_jadwal_kuliah -> showScheduleFragment()
            R.id.nav_hasil_studi -> showGradeReportFragment()
            R.id.nav_transkrip -> showTranscriptFragment()
            R.id.nav_manage_period -> showManagePeriodFragment()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showManagePeriodFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PeriodsListFragment())
            .addToBackStack(null)
            .commit()
    }
    private fun showTranscriptFragment() {
        val fragment = TranscriptFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showGradeReportFragment() {
        val fragment = GradeReportFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showScheduleFragment() {
        val fragment = ScheduleFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showCourseCardFragment() {
        val fragment = CourseCardFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showEnrollmentFragment() {
        val fragment = EnrollmentFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showAddScheduleFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddScheduleFragment())
            .addToBackStack(null)
            .commit()
    }
    private fun logoutUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    fun navigateToMenuItem(itemId: Int) {
        val item = navView.menu.findItem(itemId)
        if (item != null) {
            onNavigationItemSelected(item)
        }
    }
    private fun showHome() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        val fragment = if (users.role =="dosen") {
            HomeDosenFragment.newInstance(users)}
        else   {
           HomeMahasiswaFragment.newInstance(users)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    private fun showLecturerSchedule() {
        val fragment = LecturerScheduleFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showGradeReportingFragment() {
        val fragment = CourseGradingFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}