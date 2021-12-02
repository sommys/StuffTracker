package hu.bme.aut.stufftracker.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securepreferences.SecurePreferences
import hu.bme.aut.stufftracker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var pref: SecurePreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        pref = SecurePreferences(this, "", "PINPref")
        binding.btnLogin.setOnClickListener {
            val pin = pref.getString("PIN", "N/A")
            if(pin == "N/A"){
                val prefEditor: SharedPreferences.Editor = pref.edit()
                prefEditor.putString("PIN", binding.pinEt.text.toString())
                prefEditor.commit()
                Toast.makeText(this, "Első bejelentkezés, PIN mentve!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else if(pin == binding.pinEt.text.toString()) {
                binding.pinEt.setText("")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Hibás PIN!", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(binding.root)
    }
}