
# 키워드

-   Request Runtime Permissions
-   CustomView
-   MediaRecorder

# 구현 목록

-   마이크를 통한 음성 녹음
-   녹음한 음성 재생
-   녹음 중인 음성 시각화

#  개발 과정 [(노션에서 확인)](https://www.notion.so/codekodo/Android-Record-App-7675dc1855164141bf60157dac851993)

## 1. 기본 UI 설정하기

### State Class

```kotlin
package com.example.record

enum class State {
    BEFORE_RECORDING,
    ON_RECORDING,
    AFTER_RECORDING,
    ON_PALYING
}

```

상태에 따라 다른 UI를 보여줘야하는데 이를 `Enum` 으로 미리 정의해줬다.

### RecordButton Class

버튼의 상태관리를 보다 편하게 하기 위해서 `ImageButton` 을 상속하는 `RecordButton` 클래스를 정의해줬다.

```kotlin
package com.example.record

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

class RecordButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs) {

    fun updateIconWithState(state: State) {
        when (state) {
            State.BEFORE_RECORDING ->
                setImageResource(R.drawable.ic_record)
            State.ON_RECORDING ->
                setImageResource(R.drawable.ic_stop)
            State.AFTER_RECORDING ->
                setImageResource(R.drawable.ic_play)
            State.ON_PALYING ->
                setImageResource(R.drawable.ic_stop)
        }
    }
}

```

직접 만들어준 UI이므로 하위 버젼의 안드로이드 API에서는 적용이 안될 수 있다. 이를 위해 AppCompat 키워드를 사용해야한다고 알려준다. 또한 `Record 클래스` 내부에 상태에 따라 버튼의 모양을 변경해주는 `updateIconWithState` 메소드를 정의해줬다.

![image](https://user-images.githubusercontent.com/33795856/133890479-554194b1-0ed3-4f45-83ff-8e47384b9b17.png)

따로 정의해준 `RecordButton 클래스` 를 통해 버튼을 구현했다. 초기 상태를 지정해주지 않았기에 첫 번째 사진을 보면 빈칸으로 뜨는 것을 볼 수 있다. 초기 상태를 지정해주기 위해 `MainActivity` 에서 `RecordButton` 과 연결된 변수를 선언하고 해당 클래스에서 정의해준 `updateIconWIthState` 메소드를 사용해서 현재 상태를 업데이트 해줬다. 두 번째 사진에서는 `ic_record` 의 Vector Asset으로 되어 있는 것을 볼 수 있다.

```kotlin
package com.example.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private val recordButton : RecordButton by lazy {
        findViewById(R.id.recordButton)
    }

    private var state = State.BEFORE_RECORDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    fun initViews() {
        recordButton.updateIconWithState(state)
    }
}

```

## 2. 권한 요청하기

지난번에 갤러리 앱을 제작할 때처럼 이번에도 사용자의 마이크에 접근해야 하므로 `권한` 을 요청해야한다. `AndroidManifest.xml` 에서 우선 권한을 요청하는 코드를 작성한다.

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="<http://schemas.android.com/apk/res/android>"
    package="com.example.record">
    
    <!--권한 요청-->
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Record">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>

```

```kotlin
	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        requestAudioPermission()
    }

```

이번 녹음기 앱은 앱을 실행하면 동시에 권한을 요청하도록 했다. 따라서 `onCreate` 메소드에 권한을 요청하는 메소드를 구현하려고 한다.

```kotlin
	private val requiredPermissions = arrayOf(Manifest.permission.RECORD_AUDIO) (첫 번째 인자)

	private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
	}

// ... 중략
// 정적변수로 만들어주기 위해 companion 객체 사용 (두번째 인자)
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
   }


```

`requestPermissons` 메소드는 첫 번째 인자로 권한을 부여할 목록(배열)을 두 번째 인자로 응답코드를 받는다. 목록에 있는 권한들을 사용자에게 요청한다. 권한이 수락되면 두 번째 인자로 넣어준 값을 응답코드로 가진다.

```kotlin
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

```

또한 사용자가 권한을 거부한 경우 앱을 종료시키기 위해 `onRequestPermissionsResult` 를 오버라이딩 해줬다. 권한을 부여 받은 경우 응답 코드에는 `정적 변수` 로 선언해준 `REQUEST_RECORD_AUDIO_PERMISSION` 가 저장되어 있다. 또한 실행 결과가 `grantResult` 에 배열로 저장되어 있는데 현재 `오디오에 대한 권한` 만 요청했으므로 1개만 돌려받는다. 따라서 `firstOfNull` 메소드를 통해 실행 결과를 비교했다.

## 3. 녹음 기능 구현하기

### startRecording

녹음 기능을 구현하기 위해서는 `MediaRecorder` 를 사용한다. 하지만 바로 사용할 수는 없고 아래 그림과 같은 `상태도` 를 가진다.

![](https://developer.android.com/images/mediarecorder_state_diagram.gif)

순서를 나열해보자.

1.  `setAudioSource` 로 마이크에 접근한다.
    
2.  `setOutputFormat` 으로 포맷 방식을 지정한다.
    
3.  `setAudioEncorder` 를 통해 인코더 방식을 지정한다.
    
    인코더 방식을 지정하는 이유는 녹음 파일의 크기를 줄이기 위함이다.
    
4.  `setOutputFile` 을 통해 파일이 저장될 경로를 지정한다.
    
5.  `prepare` 을 통해 모든 준비를 완료한다.
    

이 순서에 따라 코드를 작성하면 되는데 주의할 점이 있다. 바로 녹음 파일의 크기이다. 녹음 파일의 경우 짧게는 몇 초, 길게는 몇 시간이 될 수도 있다. 이런 상황에 만약 앱의 로컬 스토리지에 저장하게 된다면 앱 용량이 상당히 커질 수 있다. 이를 방지하기 위해 휴대폰의 캐시 스토리지에 저장하고 삭제하는 방식으로 앱을 제작하려고 한다.



실제로 공식 문서를 보면 앱의 로컬 스토리지가 충분한 공간을 제공하지 않는 다면 외부 스토리지를 사용하라고 나와있다.

```kotlin
	private var recorder: MediaRecorder? = null
	// ... 중략
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

```

우선 `MediaRecorder` 객체를 `Nullable` 로 만들어주고 `startRecording` 메소드를 만들어줬다. 위에서 나열한 순서에 따라 Prepare 상태까지 진행하고 녹음을 시작한다. 이후 상태를 `ON_RECORDING` 으로 바꿔줬다.

```kotlin
	private var state = State.BEFORE_RECORDING
        set(value) {
            field = value
            recordButton.updateIconWithState(value)
        }

```

상태가 변경되는 경우 실제 화면에서의 아이콘도 변경되야하므로 `setter` 를 통해 값을 변경시켜줬다.

### stopRecording

녹음을 중지하는 메소드이다. 간단하게 구현했다.

```kotlin
	private fun stopRecording() {
        recorder?.run {
            stop()
            release() // 메모리 해제
        }
        recorder = null
        state = State.AFTER_RECORDING
    }

```

녹음이 진행되면 recorder 객체에는 null이 아니게 된다. 따라서 run을 실행하게 되고 이때 `stop` 을 실행하고 `release` 를 통해 메모리를 해제한다. 이후 recorder 객체를 null로 만들어주고 상태를 `AFTER_RECORDING` 으로 바꿔줬다.

### startPlaying

녹음된 파일을 듣기 위해서는 `MediaPlayer` 를 사용한다. 역시 `MediaRecorder` 처럼 상태 값을 갖는다. 공식 문서에 나와있는 상태도를 살펴보자.

![MediaPlayer State diagram](https://developer.android.com/images/mediaplayer_state_diagram.gif)

MediaRecorder보다 Prepare까지의 과정이 좀 더 간결하다.

1.  `setDataSource` 로 파일을 불러온다.
2.  `prepare` 을 통해 모든 준비를 완료한다.

두 과정이 끝이다. 실제 코드를 살펴보자.

```kotlin
	private var player: MediaPlayer? = null
	// ... 중략
	private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.start()
        state = State.ON_PALYING
    }

```

`player` 객체가 null이 아닌 경우 `start` 를 실행한다. 이후 `ON_PLAYING` 상태로 바꿔준다.

### stopPlaying

`stopRecording` 과 유사하다.

```kotlin
	private fun stopPlaying() {
        player?.release()
        player = null
        state = State.AFTER_RECORDING
    }

```

이렇게 녹음 시작, 녹음 중지, 녹음 파일 재생, 녹음 파일 재생 중지 총 4가지에 상태에 따른 메소드를 모두 만들었고 녹음 버튼과 연결해주면 된다.

### bindViews

`setOnClickListener` 를 통해 버튼 클릭 이벤트가 발생한 경우 현재 버튼의 상태에 따라 특정 메소드를 실행하도록 구현했다.

```kotlin
	private fun bindViews() {
        recordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> startRecording()
                State.ON_RECORDING -> stopRecording()
                State.AFTER_RECORDING -> startPlaying()
                State.ON_PALYING -> stopPlaying()
            }
        }
    }

```
