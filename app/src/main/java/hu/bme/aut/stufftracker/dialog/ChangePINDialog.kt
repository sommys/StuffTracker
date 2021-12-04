package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.securepreferences.SecurePreferences
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.databinding.ChangepinDialogBinding

class ChangePINDialog : DialogFragment() {
    private lateinit var binding: ChangepinDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("PIN megváltoztatása")
        binding = ChangepinDialogBinding.inflate(layoutInflater)
        builder.setPositiveButton(R.string.save){_,_ ->
            var pref = SecurePreferences(activity, "", "PINPref")
            var pin = pref.getString("PIN", "N/A")
            if(pin!! == binding.oldPINEt.text.toString()){
                val prefEditor: SharedPreferences.Editor = pref.edit()
                prefEditor.putString("PIN", binding.newPINEt.text.toString())
                if(binding.reminderEt.text.isNotEmpty()) {
                    prefEditor.putString("REMINDER", binding.reminderEt.text.toString())
                }
                prefEditor.commit()
                dismiss()
            } else {
                Toast.makeText(activity, "Hibás Régi PIN!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.cancel){_,_ ->
            dismiss()
        }
        builder.setView(binding.root)
        return builder.create()
    }
}