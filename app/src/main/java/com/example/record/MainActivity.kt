package com.example.record

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaDrmResetException
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }

    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            resetButton.isEnabled = value == State.AFTER_RECORDING || value == State.ON_PALYING
            recordButton.updateIconWithState(value)
        }

    // 정적변수로 만들어주기 위해 companion 객체 사용
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        requestAudioPermission()
        bindViews()
        initVariables()
    }

    private fun initViews() {
        recordButton.updateIconWithState(state)
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
    }

    private fun bindViews() {
        resetButton.setOnClickListener {
            stopPlaying()
            state = State.BEFORE_RECORDING
        }
        recordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> startRecording()
                State.ON_RECORDING -> stopRecording()
                State.AFTER_RECORDING -> startPlaying()
                State.ON_PALYING -> stopPlaying()
            }
        }
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted) {
            finish()
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC) // 마이크에 접근
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // 포멧 지정
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // 인코더 방식 지정
            setOutputFile(recordingFilePath) // 지정해준 경로에 저장
            prepare()
        }
        recorder?.start()
        state = State.ON_RECORDING
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            release() // 메모리 해제
        }
        recorder = null
        state = State.AFTER_RECORDING
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.start()
        state = State.ON_PALYING
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        state = State.AFTER_RECORDING
    }
}