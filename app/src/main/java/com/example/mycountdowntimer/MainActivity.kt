package com.example.mycountdowntimer

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Debug
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool  // SoundPoolクラスのインスタンスを宣言(後で初期化)
    private var soundResID = 0  // サウンドファイルのリソースIDを保持するプロパティを宣言
    private var count = 180
    private var beforeCount = 0

    // CountDownTimerを継承したクラス
    // millisInFuture : タイマーの残り時間をミリ秒で指定
    // countDownInterval : onTickメソッドを実行する間隔をミリ秒で指定
    inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
       //var isRunning = false  // 現在カウントダウン中か停止中かを表すフラグ
        var isPausing = false
       var isRunning = false  // 現在カウントダウン中か停止中かを表すフラグ

        // コンストラクタで指定した間隔で呼び出される
        override fun onTick(millisUntilFinished: Long) {
            val minute = millisUntilFinished / 1000L / 60L  // ミリ秒単位のタイマーから分を取り出す
            val second = millisUntilFinished / 1000L % 60L  // ミリ秒単位のタイマーから秒を取り出す

            count = (minute * 60 + second).toInt()
            //Log.d("MainActivity", "count" + count.toString())
            timerText.text = "%1d:%2$02d".format(minute, second)  // 分と秒をテキストビューに表示
            // format : 値をフォーマットされた文字列に変換することができる(ここでは「分:秒」)
            // フォーマット文字列.format(値, 値, ・・・)
            // %1 : 引数リストの1番目(minute)
            // d : 整数で表示
            // %2 : 引数リストの2番目(second)
            // 02d : 2桁の整数で表示
        }

        // タイマー終了時に呼ばれる
        override  fun onFinish() {
            timerText.text = "0:00"  // 画面に「0:00」を表示
            // サウンドIDをplayメソッドに指定してサウンドを再生
            soundPool.play(soundResID, 1.0f, 100f, 0 ,0 ,1.0f)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //timerText.text = "3:00"  // TextViewに「3:00」と表示
        timerText.text = "3:00"

        // CountDownTimerを継承したクラスのインスタンスを作成
        // タイマーの継続時間として3分、onTickメソッドが呼ばれる間隔として0.1秒を設定
        val timer = MyCountDownTimer( count.toLong() * 1000, 100)
        val timer = MyCountDownTimer( 3 * 60 * 1000, 100)

        // フローティングアクションボタンがタップされたときのリスナーを設定
        play.setOnClickListener {
            timer.apply {
                //isRunning = true // カウントダウンを開始するフラグを設定
                when (isPausing) {
                    true -> {
                        //this.isPausing = false
                        start()  // startメソッドでカウントダウンを開始
                        MyCountDownTimer( beforeCount.toLong() * 1000, 100)
                    }
                    false -> start()  // startメソッドでカウントダウンを開始
        playStop.setOnClickListener {
            when (timer.isRunning) {
                true ->  timer.apply {
                    isRunning = false  // 停止するフラグを設定
                    cancel()  // CountDownTimerクラスのcancelメソッドでカウントダウンを停止
                    playStop.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                }
                false -> timer.apply {
                    isRunning = true  // カウントダウンを開始するフラグを設定
                    start()  // startメソッドでカウントダウンを開始
                    playStop.setImageResource(R.drawable.ic_stop_black_24dp)
                }
            }
        }

        pause.setOnClickListener {
            timer.apply {
                this.isPausing = true
                cancel()
                beforeCount = count
            }
        }

        stop.setOnClickListener {
            timer.apply {
                //isRunning = false // 停止するフラグを設定
                cancel()  // CountDownTimerクラスのcancelメソッドでカウントダウンを停止
                timerText.text = "3:00"
                count = 180
            }
        }
    }

    // アクティビティが画面に表示されたときに実行
    override fun onResume() {
        super.onResume()
        soundPool =
                // SoundPoolのコンストラクタはAPI21(Lollipop)以降では非推奨となっている
                // これはAPI21以降はSoundPool.Builderクラスの仕様が推奨されているためである
                // よって、両方のAPIに対応したコードを書いておくと後々困らずに済む

                // 実行中のOSがAPI21以降ならfalse、そうでなければtrue
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    @Suppress("DEPRECATION")  // 非推奨のメソッドを使っているが、対応済みなので検査不要
                    SoundPool(2, AudioManager.STREAM_ALARM, 0)  // SoundPoolのインスタンスを作成
                } else {
                    val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                    SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build()
                }
        soundResID = soundPool.load(this, R.raw.bellsound, 1)  // 登録されたサウンドリソースを読み込む
    }

    // アクティビティが非表示になったときに実行
    override fun onPause() {
        super.onPause()
        soundPool.release()  // SoundPoolが使用しているメモリをreleaseメソッドを使って解放
    }
}
