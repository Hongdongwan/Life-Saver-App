package com.example.pj4test

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.SEND_SMS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*
import android.telephony.SmsManager
import android.view.View
import com.example.pj4test.fragment.AudioFragment
import com.example.pj4test.fragment.CameraFragment


class MainActivity : AppCompatActivity(), OnDataPassListener, OnFaceListener, ActivityToCamera {
    private val TAG = "MainActivity"
    var mediaPlayer: MediaPlayer? = null
    var isHelpNeed = false;

    // permissions
    private val permissions = arrayOf(RECORD_AUDIO, CAMERA, SEND_SMS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    private val PERMISSIONS_REQUEST = 0x0000001;

    private val delayMillis: Long = 1000 // 주기적으로 실행할 간격 (1초)

    //위도 경도
    var lat: Double = 0.0;
    var long: Double = 0.0;

    // audio
    val handler = Handler(Looper.getMainLooper())

    // Runnable 객체 생성
    val handleRunnable = Runnable {
        // 예약된 작업 코드

        Log.d("MainFaceTag222", isHelpNeed.toString())
        if (isHelpNeed) {
            endCam()
            val phoneNumber = "01073794936" // 112로 해야한다.
            val message = "응급 상황입니다.\n위도 : " + lat.toString() + "\n경도 : " + long.toString()
            sendSMS(phoneNumber, message)

            isHelpNeed = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer.create(this, R.raw.min1)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions() // check permissions
        mediaPlayer = MediaPlayer.create(this, R.raw.min1)

        val fragment_audio = AudioFragment()
        fragment_audio.setOnDataPassListener(this)
        val fragment_face = CameraFragment()
        fragment_face.setOnDataPassListener(this)


        //val tt = Intent(Intent.ACTION_CALL, Uri.parse("tel:01073794936"))
        //startActivity(tt)

//        val phoneNumber = "01073794936"
//        val message = "보낼 메시지 내용"
//        sendSMS(phoneNumber, message)
    }

    private fun checkPermissions() {
        if (permissions.all{ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED}){
            Log.d(TAG, "All Permission Granted")
        }
        else{
            requestPermissions(permissions, PERMISSIONS_REQUEST)
        }
    }

    fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    override fun onDataPass(data: String) {
        Log.d("MainTag", data)

        /*// beep sound
        val toneType = ToneGenerator.TONE_PROP_BEEP
        val durationMillis = 1000
        val volume = 100

        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, volume)
        toneGenerator.startTone(toneType, durationMillis)



         */
        // GPS
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastKnownLocation != null) {
                val latitude = lastKnownLocation.latitude
                val longitude = lastKnownLocation.longitude
                lat = latitude
                long = longitude

                // Use the last known GPS location
                Log.d("Last Location", "Latitude: $latitude, Longitude: $longitude")
            } else {
                // Last known location is not available
                Log.e("Last Location", "Last known location is not available")
            }
        } else {
            // Location permission is not granted
            Log.e("Last Location", "Location permission not granted")
        }

        if (!(mediaPlayer?.isPlaying)!!){
            isHelpNeed = true
            mediaPlayer?.start()
            startSetUpCam()
            // 1분(60,000 밀리초) 후에 실행되는 코드
            handler.postDelayed(handleRunnable, 60000) // 1분을 밀리초로 표현한 값
        }
    }

    override fun onFaceDataPass(data: Boolean) {
        Log.d("MainFaceTag", data.toString())
        if (data){
            endCam()

            // 예약된 작업 취소
            handler.removeCallbacks(handleRunnable)
            isHelpNeed = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            mediaPlayer = MediaPlayer.create(this, R.raw.min1)
        }
    }

    override fun onCameraStart(data: Boolean) {
        // Handle the received data in the Fragment

    }
    override fun onCameraEnd(data: Boolean) {
        // Handle the received data in the Fragment
    }

    private fun startSetUpCam() {
        val fragment = supportFragmentManager.findFragmentById(R.id.cameraFragmentContainerView)
        if (fragment is CameraFragment) {
            fragment.startSetUpCamera()
            val cameraFragmentContainerView = findViewById<View>(R.id.cameraFragmentContainerView)
            cameraFragmentContainerView.visibility = View.VISIBLE
        }
    }
    private fun endCam() {
        val fragment = supportFragmentManager.findFragmentById(R.id.cameraFragmentContainerView)
        if (fragment is CameraFragment) {
            fragment.endCamera()
            val cameraFragmentContainerView = findViewById<View>(R.id.cameraFragmentContainerView)
            cameraFragmentContainerView.visibility = View.INVISIBLE
        }
    }
}