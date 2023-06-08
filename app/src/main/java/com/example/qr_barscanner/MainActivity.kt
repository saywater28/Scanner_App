package com.example.qr_barscanner

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.qr_barscanner.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore

    val options=BarcodeScannerOptions.Builder(
    ).setBarcodeFormats(
        Barcode.FORMAT_QR_CODE,
        Barcode.FORMAT_AZTEC
    )
        .build()

    lateinit var binding: ActivityMainBinding
    private lateinit var database : DatabaseReference
    private lateinit var url : String

    private val REQUEST_IMAGE_CAPTURE = 1

    private var imageBitmap: Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {

            captureImage.setOnClickListener {

                takeImage()

                textView.text=""
            }

            detectScan.setOnClickListener {
                detectImage()
            }

        }


        val save : Button = findViewById(R.id.save)
        save.setOnClickListener{

            val url = detectImage()

            saveToFirestore(url.toString())
        }

    }

    private fun saveToFirestore(url: String): Any {
//        val url: MutableMap<String, Any> = HashMap()
//        url["value"] = url.toString()

//        val data: MutableMap<String, Any> = HashMap()
//        data["url"] = "your string value"

        val map =  mapOf("key" to url)



        db.collection("User").document(url.toString())
            .set(map)
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "QR code data saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity, "Failed to save QR code data", Toast.LENGTH_SHORT).show()
            }
        return url.toString()
    }

    private fun detectImage() {

        if (imageBitmap!=null){
            val image = InputImage.fromBitmap(imageBitmap!!, 0)

            val scanner=BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener {barcodes->

                    if (barcodes.toString()=="[]"){

                        Toast.makeText(this,"Nothing to scan", Toast.LENGTH_SHORT )


                    }

                    for(barcode in barcodes){

                        val valueType=barcode.valueType

                        when(valueType){

                            Barcode.TYPE_WIFI->{

                                val ssid=barcode.wifi!!.ssid

                                val password=barcode.wifi!!.password

                                val type=barcode.wifi!!.encryptionType

                                binding.textView.text=ssid+"\n"+password+"\n"+type
                            }

                            Barcode.TYPE_URL->{

                                val title=barcode.url!!.title

                                val url=barcode.url!!.url

                                binding.textView.text=title+"\n"+url
                            }
                        }
                    }

                }




        }

        else{

            Toast.makeText(this, "Please select photo", Toast.LENGTH_SHORT)

        }

    }

    private fun takeImage() {

        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            val url = URL(url.toString())

        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode== RESULT_OK){

            val extras:Bundle?=data?.extras

            imageBitmap=extras?.get("data") as Bitmap

            if(imageBitmap!=null){
                binding.imageView.setImageBitmap(imageBitmap)
            }
        }


    }




}