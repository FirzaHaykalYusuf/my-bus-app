package com.rexensoft.mybus.ui.activity.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.rexensoft.mybus.R
import com.rexensoft.mybus.ui.activity.auth.LoginActivity
import com.rexensoft.mybus.ui.activity.menu.BusActivity
import com.rexensoft.mybus.ui.activity.menu.HelpActivity
import com.rexensoft.mybus.ui.activity.menu.SettingsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogout = findViewById<ImageView>(R.id.btn_logout)

        val cardAktifitas = (findViewById<LinearLayout>(R.id.menu_container)).getChildAt(0) as androidx.cardview.widget.CardView
        val cardSetting = (findViewById<LinearLayout>(R.id.menu_container)).getChildAt(1) as androidx.cardview.widget.CardView
        val cardHelp = (findViewById<LinearLayout>(R.id.menu_container)).getChildAt(2) as androidx.cardview.widget.CardView


        val layoutAktifitas = cardAktifitas.getChildAt(0) as LinearLayout
        val layoutSetting = cardSetting.getChildAt(0) as LinearLayout
        val layoutHelp = cardHelp.getChildAt(0) as LinearLayout

        layoutAktifitas.setOnClickListener {
            startActivity(Intent(this, BusActivity::class.java))
        }

        layoutSetting.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        layoutHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
