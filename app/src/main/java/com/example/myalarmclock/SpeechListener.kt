package com.example.myalarmclock

import android.speech.tts.UtteranceProgressListener
import android.util.Log

class SpeechListener : UtteranceProgressListener(){
    var tag : String = "TTS"
    override fun onDone(utteranceId: String?) {
        Log.d(tag,"音声再生が完了しました。")
    }

    override fun onError(utteranceId: String?) {
        Log.d(tag,"音声再生中にエラーが発生しました。")
    }

    override fun onError(utteranceId: String?, errorCode: Int) {
        Log.d(tag,"音声再生中にエラーが発生しました。")
    }

    override fun onStart(utteranceId: String?) {
        Log.d(tag,"音声再生を開始します。")
    }

    override fun onStop(utteranceId: String?, interrupted: Boolean) {
        Log.d(tag,"音声再生を中止します。")
    }

    override fun onBeginSynthesis(utteranceId: String?, sampleRateInHz: Int, audioFormat: Int, channelCount: Int) {
        Log.d(tag,"音声の合成を開始します。")
    }

    override fun onAudioAvailable(utteranceId: String?, audio: ByteArray?) {
        Log.d(tag,"音声が利用可能になりました。")
    }
}