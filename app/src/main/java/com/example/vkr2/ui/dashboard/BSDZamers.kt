package com.example.vkr2.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.BottomsheetForBodyBinding
import com.example.vkr2.repository.BodyMeasurementsRepository
import com.example.vkr2.repository.BodyMeasurementsRepositoryImpl
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class BSDZamers: BottomSheetDialogFragment() {

    private var _binding: BottomsheetForBodyBinding? = null
    private val binding get() = _binding!!


    private lateinit var bodyPart:String
    private var isLeft:Boolean = true
    private lateinit var zamersAdapter: ZamersItemAdapter
    private lateinit var repository: BodyMeasurementsRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bodyPart = requireArguments().getString("bodyPart")?:"?"
        isLeft = requireArguments().getBoolean("isLeft")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener{
            val bootomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bootomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetForBodyBinding.inflate(inflater, container, false)
        repository = BodyMeasurementsRepositoryImpl(requireContext(),lifecycleScope)
        binding.nameZamer.text = bodyPart
        zamersAdapter = ZamersItemAdapter(bodyPart, isLeft)
        binding.zamerConteiner.layoutManager = LinearLayoutManager(requireContext())
        binding.zamerConteiner.adapter = zamersAdapter
        binding.closeZamer.setOnClickListener(){dismiss()}
        binding.bntAddZamer.setOnClickListener(){shodAddZamerDialog()}
        loadZamer()
        return binding.root
    }
    private fun loadZamer(){
        lifecycleScope.launch {
            val all = repository.getAll().first()
            val filtered = all.filter { getValueForBodyPart(it) != null }

            if (filtered.isEmpty()){
                binding.ifNotExist.visibility = View.VISIBLE
                binding.zamerConteiner.visibility = View.GONE
            }
            else{
                binding.ifNotExist.visibility = View.GONE
                binding.zamerConteiner.visibility = View.VISIBLE
                zamersAdapter.submitList(filtered)
            }
        }
    }
    private fun shodAddZamerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_zamer, null)
        val editText = dialogView.findViewById<EditText>(R.id.zamerValue)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавить замер")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val value = editText.text.toString().toIntOrNull()
                if (value != null) {
                    lifecycleScope.launch {
                        val all = repository.getAll().first()
                        val latest = all.maxByOrNull { it.date }

                        val now = LocalDate.now()

                        val updated = if (latest != null) {
                            latest.copy(
                                date = now,
                                neck = if (bodyPart == "Шея") value else latest.neck,
                                shoulders = if (bodyPart == "Плечи") value else latest.shoulders,
                                chest = if (bodyPart == "Грудь") value else latest.chest,
                                waist = if (bodyPart == "Талия") value else latest.waist,
                                pelvis = if (bodyPart == "Таз") value else latest.pelvis,
                                forearmsLeft = if (bodyPart == "Предплечья" && isLeft) value else latest.forearmsLeft,
                                forearmsRight = if (bodyPart == "Предплечья" && !isLeft) value else latest.forearmsRight,
                                bicepsLeft = if (bodyPart == "Бицепсы" && isLeft) value else latest.bicepsLeft,
                                bicepsRight = if (bodyPart == "Бицепсы" && !isLeft) value else latest.bicepsRight,
                                tricepsLeft = if (bodyPart == "Трицепсы" && isLeft) value else latest.tricepsLeft,
                                tricepsRight = if (bodyPart == "Трицепсы" && !isLeft) value else latest.tricepsRight,
                                bedroLeft = if (bodyPart == "Бедра" && isLeft) value else latest.bedroLeft,
                                begroRight = if (bodyPart == "Бедра" && isLeft) value else latest.begroRight,
                                ikriLeft = if (bodyPart == "Икры" && isLeft) value else latest.ikriLeft,
                                ikriRight = if (bodyPart == "Икры" && !isLeft) value else latest.ikriRight
                            )
                        } else {
                            BodyMeasurementsEntity(
                                date = now,
                                neck = if (bodyPart == "Шея") value else null,
                                shoulders = if (bodyPart == "Плечи") value else null,
                                chest = if (bodyPart == "Грудь") value else null,
                                waist = if (bodyPart == "Талия") value else null,
                                pelvis = if (bodyPart == "Таз") value else null,
                                forearmsLeft = if (bodyPart == "Предплечья" && isLeft) value else null,
                                forearmsRight = if (bodyPart == "Предплечья" && !isLeft) value else null,
                                bicepsLeft = if (bodyPart == "Бицепсы" && isLeft) value else null,
                                bicepsRight = if (bodyPart == "Бицепсы" && !isLeft) value else null,
                                tricepsLeft = if (bodyPart == "Трицепсы" && isLeft) value else null,
                                tricepsRight = if (bodyPart == "Трицепсы" && !isLeft) value else null,
                                bedroLeft = if (bodyPart == "Бедра" && isLeft) value else null,
                                ikriLeft = if (bodyPart == "Икры" && isLeft) value else null,
                                ikriRight = if (bodyPart == "Икры" && !isLeft) value else null
                            )
                        }

                        repository.insertOrUpdate(updated)
                        loadZamer()
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Введите число", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun getValueForBodyPart(measurement: BodyMeasurementsEntity): Int?{
        return when(bodyPart){
            "Шея" -> measurement.neck
            "Плечи" -> measurement.shoulders
            "Грудь" -> measurement.chest
            "Талия" -> measurement.waist
            "Таз" -> measurement.pelvis
            "Предплечья" -> if (isLeft) measurement.forearmsLeft else measurement.forearmsRight
            "Бицепсы" -> if (isLeft) measurement.bicepsLeft else measurement.bicepsRight
            "Трицепсы" -> if (isLeft) measurement.tricepsLeft else measurement.tricepsRight
            "Бедра" -> if (isLeft) measurement.bedroLeft else measurement.bicepsRight
            "Икры" -> if (isLeft) measurement.ikriLeft else measurement.ikriRight
            else -> null
        }
    }

    companion object {
        fun newInstance(bodyPart: String, isLeft: Boolean): BSDZamers {
            val fragment = BSDZamers()
            val args = Bundle().apply {
                putString("bodyPart", bodyPart)
                putBoolean("isLeft", isLeft)
            }
            fragment.arguments = args
            return fragment
        }
    }

}