package hu.bme.aut.stufftracker.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.securepreferences.SecurePreferences
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var pref: SecurePreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        pref = SecurePreferences(this, "", "PINPref")
        val pin = pref.getString("PIN", "N/A")
        if(pin == "N/A"){
            binding.btnLogin.setText(R.string.register)
        }
        initPinPAD()
        binding.fabReminder.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.reminderDialogTitle)
            val reminder = pref.getString("REMINDER", "Nincs megadva emlékeztető...")
            dialog.setMessage(reminder)
            dialog.create()
            dialog.show()
        }
        binding.btnLogin.setOnClickListener {
            if(pin == "N/A"){
                val prefEditor: SharedPreferences.Editor = pref.edit()
                prefEditor.putString("PIN", binding.pinEt.text.toString())
                prefEditor.commit()
                binding.btnLogin.setText(R.string.login)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else if(pin == binding.pinEt.text.toString()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Hibás PIN!", Toast.LENGTH_SHORT).show()
            }
            binding.pinEt.setText("")
        }

        setContentView(binding.root)
    }

    private fun initPinPAD() {
        binding.one.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('1')
            }
        }
        binding.two.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('2')
            }
        }
        binding.three.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('3')
            }
        }
        binding.four.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('4')
            }
        }
        binding.five.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('5')
            }
        }
        binding.six.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('6')
            }
        }
        binding.seven.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('7')
            }
        }
        binding.eight.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('8')
            }
        }
        binding.nine.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('9')
            }
        }
        binding.zero.setOnClickListener {
            if(binding.pinEt.text.length != 4) {
                binding.pinEt.text.append('0')
            }
        }
        binding.delete.setOnClickListener {
            binding.pinEt.setText("");
        }
        binding.clearPIN.setOnClickListener {
            if(binding.pinEt.text.length != 0) {
                binding.pinEt.setText(binding.pinEt.text.toString().substring(0,binding.pinEt.text.toString().length-1))
            }
        }
    }
}