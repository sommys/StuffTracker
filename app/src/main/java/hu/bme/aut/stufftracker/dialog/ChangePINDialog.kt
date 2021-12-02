package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.securepreferences.SecurePreferences
import hu.bme.aut.stufftracker.databinding.ChangepinDialogBinding

class ChangePINDialog : DialogFragment() {
    private lateinit var binding: ChangepinDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("PIN megváltoztatása")
        binding = ChangepinDialogBinding.inflate(layoutInflater)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            var pref = SecurePreferences(activity, "", "PINPref")
            var pin = pref.getString("PIN", "N/A")
            if(pin!! == binding.oldPINEt.text.toString()){
                val prefEditor: SharedPreferences.Editor = pref.edit()
                prefEditor.putString("PIN", binding.newPINEt.text.toString())
                prefEditor.commit()
                dismiss()
            } else {
                Toast.makeText(activity, "Hibás Régi PIN!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setView(binding.root)
        return builder.create()
    }
}