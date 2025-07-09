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
import com.example.minisiasat.utils.DatabaseNodes
import com.example.minisiasat.utils.Lecturer
import com.example.minisiasat.utils.Students
// --- AKHIR PERBAIKAN ---
import com.example.minisiasat.utils.Users
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
        setupBackButtonListener()

        navView.setNavigationItemSelectedListener(this)
        setupDrawerMenu()

        // Set user info in header
        val header = navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.nav_header_name).text = users.name
        header.findViewById<TextView>(R.id.nav_header_email).text = users.email

        // Show Home by default
        if (savedInstanceState == null) {
            showHome()
        }
    }
    private fun setupBackButtonListener() {
        // Logika ini kita nonaktifkan untuk sementara agar hamburger menu selalu ada
        /*
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                // Tampilkan panah kembali
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                toggle.isDrawerIndicatorEnabled = false // Sembunyikan ikon hamburger
                // Handle klik pada panah kembali
                toggle.setToolbarNavigationClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            } else {
                // Tampilkan ikon hamburger lagi
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                toggle.isDrawerIndicatorEnabled = true
                toggle.setToolbarNavigationClickListener(null) // Hapus listener
            }
        }
        */
    }

    private fun setupDrawerMenu() {
        val menu: Menu = navView.menu
        // Hide all except Home
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
            R.id.nav_input_jadwal -> showInputJadwalFragment()
            R.id.nav_jadwal_mengajar -> showJadwalMengajarFragment()
            R.id.nav_input_nilai -> showInputNilaiFragment()
            R.id.nav_registrasi -> showRegistrasiFragment()
            R.id.nav_kartu_studi -> showKartuStudiFragment()
            R.id.nav_jadwal_kuliah -> showJadwalKuliahFragment()
            R.id.nav_hasil_studi -> showHasilStudiFragment()
            R.id.nav_transkrip -> showTranskripFragment()
            R.id.nav_manage_period -> showManagePeriodFragment()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    // ... di dalam MainActivity

    private fun showManagePeriodFragment() {
        // Arahkan ke list, bukan ke detail langsung
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PeriodsListFragment())
            .addToBackStack(null)
            .commit()
    }
    private fun showTranskripFragment() {
        val fragment = TranskripFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showHasilStudiFragment() {
        val fragment = HasilStudiFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showJadwalKuliahFragment() {
        val fragment = JadwalKuliahFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showKartuStudiFragment() {
        val fragment = KartuStudiFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showRegistrasiFragment() {
        val fragment = RegistrasiFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun logoutUser() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showInputJadwalFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InputJadwalFragment())
            .addToBackStack(null)
            .commit()
    }
    fun navigateToMenuItem(itemId: Int) {
        // Cari item menu berdasarkan ID yang dikirim
        val item = navView.menu.findItem(itemId)
        if (item != null) {
            // Panggil fungsi navigasi yang sudah ada
            onNavigationItemSelected(item)
        }
    }

    private fun showHome() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        // --- INI PERUBAHANNYA ---
        val fragment = when (users.role) {
            "dosen" -> HomeDosenFragment.newInstance(users)
            "mahasiswa" -> HomeMahasiswaFragment.newInstance(users) // Tampilkan home mahasiswa
            else -> HomeFragment.newInstance(users)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    private fun showJadwalMengajarFragment() {
        // Kirim data user ke fragment agar fragment tahu kode dosen yang login
        val fragment = JadwalMengajarFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showInputNilaiFragment() {
        val fragment = InputNilaiFragment.newInstance(users)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadStudentData(kode: String?, textView: TextView) {
        if (kode == null) {
            textView.text = "Kode mahasiswa tidak tersedia"
            return
        }
        DatabaseNodes.studentsRef.child(kode)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val student = snapshot.getValue(Students::class.java)
                    student?.let {
                        val info = """
                            Halo, ${it.name}
                            Email: ${users.email}
                            Prodi: ${it.major}
                            Tahun Masuk: ${it.entryYear}
                        """.trimIndent()
                        textView.text = info
                    } ?: run {
                        textView.text = "Data mahasiswa tidak ditemukan."
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    textView.text = "Gagal ambil data mahasiswa: ${error.message}"
                }
            })
    }

    private fun loadLecturerData(kode: String?, textView: TextView) {
        if (kode == null) {
            textView.text = "Kode dosen tidak tersedia"
            return
        }
        DatabaseNodes.lecturersRef.child(kode)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val lecturer = snapshot.getValue(Lecturer::class.java)
                    lecturer?.let {
                        val info = """
                            Halo, ${users.name} (${users.role})
                            Email: ${users.email}
                            -------------------------
                            Nama Dosen: ${it.name}
                            Departemen: ${it.department}
                            Jabatan: ${users.position ?: "DOSEN"}
                        """.trimIndent()
                        textView.text = info
                    } ?: run {
                        textView.text = "Data dosen tidak ditemukan."
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    textView.text = "Gagal ambil data dosen: ${error.message}"
                }
            })
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Perbaikan kecil: Gunakan onBackPressedDispatcher modern
            super.onBackPressed()
        }
    }
}