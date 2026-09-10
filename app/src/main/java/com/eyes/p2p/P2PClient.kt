package com.eyes.p2p
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
class P2PClient(private val context: Context) {
    private var socket: Socket? = null
    private val running = AtomicBoolean(false)
    fun connect(targetIp: String, onFrame: (Bitmap) -> Unit) {
        running.set(true)
        Thread {
            try {
                socket = Socket(targetIp, 9999)
                val input = socket!!.getInputStream()
                val sizeBuffer = ByteArray(4)
                while (running.get() && socket?.isClosed == false) {
                    if (input.read(sizeBuffer) != 4) break
                    val frameSize = sizeBuffer.toIntBigEndian()
                    val frameData = ByteArray(frameSize)
                    var read = 0
                    while (read < frameSize) {
                        val r = input.read(frameData, read, frameSize - read)
                        if (r == -1) break
                        read += r
                    }
                    BitmapFactory.decodeByteArray(frameData, 0, frameSize)?.let { onFrame(it) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }
    fun disconnect() { running.set(false); try { socket?.close() } catch (e: Exception) {} }
    private fun ByteArray.toIntBigEndian(): Int =
        ((this[0].toInt() and 0xFF) shl 24) or
        ((this[1].toInt() and 0xFF) shl 16) or
        ((this[2].toInt() and 0xFF) shl 8) or
        (this[3].toInt() and 0xFF)
}
