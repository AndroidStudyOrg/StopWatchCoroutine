package org.shop.stopwatchcor

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.shop.stopwatchcor.databinding.ActivityMainBinding
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // 타이머 객체 선언
    private var timer: Timer? = null

    // 현재 시간을 나타내는 변수
    private var currentTime = 0

    // 현재 구간 시간을 나타내는 변수
    private var currentIntervalTime = 0

    // 구간 기록 Flag
    private var lapFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        /**
         * 초기 버튼들 상태
         */
        updateView(
            startVisibility = true,
            intervalRecVisibility = true,
            stopVisibility = false,
            resetVisibility = false,
            continueVisibility = false,
            groupVisibility = false
        )

        with(binding) {
            /**
             * 초기 텍스트뷰 상태 (00:00.00)
             */
            tvCurrentTime.text = resources.getString(R.string.textTime)
            tvIntervalTime.text = resources.getString(R.string.textTime)

            btnStart.setOnClickListener {
                showToastMessage("시작되었습니다.")
                startClicked()
            }
            btnIntervalRec.setOnClickListener {
                if (!lapFlag) {
                    showToastMessage("스톱워치를 실행해주세요.")
                } else {
                    intervalRecClicked()
                }
            }

            btnStop.setOnClickListener {
                showToastMessage("중지되었습니다.")
                stopClicked()
            }

            btnReset.setOnClickListener {
                showStopDialog()
            }

            btnContinue.setOnClickListener {
                showToastMessage("계속 진행합니다.")
                continueClicked()
            }
        }
    }

    private fun startClicked() {
        /*
        버튼 상태 업데이트 및 Flag 상태 변경
         */
        lapFlag = true
        updateView(
            startVisibility = false,
            intervalRecVisibility = true,
            stopVisibility = true,
            resetVisibility = false,
            continueVisibility = false,
            groupVisibility = true
        )
        /*
        타이머 재시작
         */
        startTimer()
    }

    /**
     * 레이아웃 컨테이너를 생성한 후 gravity와 size, padding 등을 설정하고 컨테이너에 addView
     */
    private fun intervalRecClicked() {
        val container = binding.layoutLapTimeContainer
        TextView(this).apply {
            val milliSec = currentTime % 100
            val second = currentTime.div(100) % 60
            val minute = currentTime.div(100) / 60

            val intervalTime = currentTime - currentIntervalTime
            val intervalMilliSec = intervalTime % 100
            val intervalSecond = intervalTime.div(100) % 60
            val intervalMinute = intervalTime.div(100) / 60
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            textSize = 20f
            text = String.format(
                "%02d \t\t\t\t\t %02d:%02d.%02d \t\t\t\t\t %02d:%02d.%02d",
                container.childCount.inc(),
                intervalMinute,
                intervalSecond,
                intervalMilliSec,
                minute,
                second,
                milliSec
            )
            setPadding(0, 10, 0, 10)
        }.let { labTime ->
            container.addView(labTime, 0)
        }
        currentIntervalTime = currentTime
    }

    private fun stopClicked() {
        /*
        버튼 상태 업데이트 및 Flag 상태 변경
         */
        lapFlag = false
        updateView(
            startVisibility = false,
            intervalRecVisibility = false,
            stopVisibility = false,
            resetVisibility = true,
            continueVisibility = true,
            groupVisibility = true
        )
        timer?.cancel()
        timer = null
    }

    private fun resetClicked() {
        /*
        버튼 상태 업데이트 및 Flag 상태 변경
         */
        lapFlag = false
        updateView(
            startVisibility = true,
            intervalRecVisibility = true,
            stopVisibility = false,
            resetVisibility = false,
            continueVisibility = false,
            groupVisibility = false
        )
        /*
        버튼 및 시간들을 초기 설정으로 초기화
         */
        with(binding) {
            currentTime = 0
            currentIntervalTime = 0
            tvCurrentTime.text = resources.getString(R.string.textTime)
            tvIntervalTime.text = resources.getString(R.string.textTime)
            layoutLapTimeContainer.removeAllViews()
        }

    }

    private fun continueClicked() {
        /*
        버튼 상태 업데이트 및 Flag 상태 변경
         */
        lapFlag = true
        updateView(
            startVisibility = false,
            intervalRecVisibility = true,
            stopVisibility = true,
            resetVisibility = false,
            continueVisibility = false,
            groupVisibility = true
        )
        /*
        타이머 재시작
         */
        startTimer()
    }

    private fun startTimer() {
        timer = timer(initialDelay = 0, period = 10) {
            currentTime += 1
            val milliSec = currentTime % 100
            val second = currentTime.div(100) % 60
            val minute = currentTime.div(100) / 60

            val intervalTime = currentTime - currentIntervalTime
            val intervalMilliSec = intervalTime % 100
            val intervalSecond = intervalTime.div(100) % 60
            val intervalMinute = intervalTime.div(100) / 60
//            Log.d("time", String.format("%02d:%02d.%02d", minute, second, milliSec))

            // 앞서 계산한 시간을 UI 업데이트
            CoroutineScope(Dispatchers.Main).launch {
                binding.tvCurrentTime.text =
                    String.format("%02d:%02d.%02d", minute, second, milliSec)
                binding.tvIntervalTime.text = String.format(
                    "%02d:%02d.%02d",
                    intervalMinute,
                    intervalSecond,
                    intervalMilliSec
                )
            }
        }
    }

    // 스톱워치 중지 시 확인 Dialog 함수
    private fun showStopDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("초기화 하시겠습니까?")
            setPositiveButton("네") { _, _ ->
                resetClicked()
            }
            setNegativeButton("아니요", null)
        }.show()
    }

    // 이벤트 발생 시 Toast 메세지 보여주는 함수
    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    // 버튼 상태 변경 함수
    private fun updateView(
        startVisibility: Boolean,
        intervalRecVisibility: Boolean,
        stopVisibility: Boolean,
        resetVisibility: Boolean,
        continueVisibility: Boolean,
        groupVisibility: Boolean
    ) {
        with(binding) {
            btnStart.isVisible = startVisibility
            btnIntervalRec.isVisible = intervalRecVisibility
            btnStop.isVisible = stopVisibility
            btnReset.isVisible = resetVisibility
            btnContinue.isVisible = continueVisibility
            group.isVisible = groupVisibility
        }
    }
}