package com.example.safeproassignment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.safeproassignment.databinding.ActivityDetailsBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.*
import java.util.*

class DetailsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailsBinding.inflate(layoutInflater) }
    private val PICK_PDF_REQUEST_CODE = 101
    private val SELECT_PICTURE = 2
    private var imageUri: Uri? = null
    var file: File? = null
    private var imageStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        datepicker()

        val apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        val placesClient = Places.createClient(this)
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment1) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(
            Arrays.asList(
                Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG
            )
        )

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {

                Log.i(ContentValues.TAG, "An error occurred: $p0")
            }

            @SuppressLint("SetTextI18n")
            override fun onPlaceSelected(p0: Place) {
                binding.address.setText(p0.address?.toString())
            }
        })

        binding.pdfBtn.setOnClickListener {
            pickFileFromGallery()
        }

        binding.nextBtn.setOnClickListener {
            val name: String = binding.name.text.toString()
            val dob: String = binding.date.text.toString()
            val number: String = binding.number.text.toString()
            val mail: String = binding.mailid.text.toString()
            val address: String = binding.address.text.toString()
            val pdf: String = binding.pdfName.text.toString()

            val data: String =
                "Name: $name\nDate of Birth: $dob\nContact Number: $number\nMail Id :$mail\n Address : $address\nAttachment : $pdf"
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("hemasg1234@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Details")
            intent.putExtra(Intent.EXTRA_TEXT, data)
            startActivity(Intent.createChooser(intent, "Send mail"))
        }

        binding.photoIv.setOnClickListener {
//            isStoragePermissionGranted
            chooseImage()
        }
    }

    private fun datepicker() {
        val editText = findViewById<TextView>(R.id.date)
        val cal = Calendar.getInstance()
        val myYear = cal.get(Calendar.YEAR)
        val myMonth = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        editText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    editText.text = "" + year + "-" + (month + 1) + "-" + dayOfMonth
                }, myYear, myMonth, day
            )
            datePickerDialog.show()
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SELECT_PICTURE) {
//            if (resultCode == Activity.RESULT_OK) {
//                onPictureSelect(data)
//            }
//        }
//    }


    private fun onPictureSelect(data: Intent?) {
        imageUri = data!!.data
        try {
            imageStream = contentResolver.openInputStream(imageUri!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (imageStream != null) {
            binding.photoIv.setImageBitmap(BitmapFactory.decodeStream(imageStream))
        }
    }


    private fun pickFileFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, PICK_PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                onPictureSelect(data)
            }
        }
        if (requestCode == PICK_PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedFileUri: Uri? = data.data
            selectedFileUri?.let { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                inputStream?.let {
                    val pdfFile: File? = createPDFFile(inputStream)
                    if (pdfFile != null) {

                        Log.d("PDF Path", pdfFile.absolutePath)
                        binding.pdfName.text = pdfFile.absolutePath
                        // Use the pdfFile as needed (e.g., upload, read content)
                    } else {
                        Log.e("PDF Error", "Failed to create PDF file.")
                    }
                }
            }
        }
    }

    private fun createPDFFile(inputStream: InputStream): File? {
        val outputDir: File? = cacheDir // Use appropriate directory for your needs
        val outputFile: File? = try {
            File.createTempFile("temp", ".pdf", outputDir)
        } catch (e: IOException) {
            null
        }
        if (outputFile != null) {
            try {
                val outputStream: OutputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(4 * 1024) // Adjust buffer size as needed
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                return outputFile
            } catch (e: IOException) {
                return null
            }
        }
        return null
    }


}