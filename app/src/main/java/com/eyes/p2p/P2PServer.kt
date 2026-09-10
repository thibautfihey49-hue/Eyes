package com.eyes.p2p
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.hardware.Camera
import java.io.*
import java.net.*
import java.util.concurrent.atomic.AtomicBoolean
class P2PServer(private val context: Context, private val myId: String) {
    private var serverSocket: ServerSocket? = null
    private var camera: Camera? = null
    private val running = AtomicBoolean(false)
    private var onConnection: (() -> Unit)? = null
    fun start(callback: () -> Unit) {
        onConnection = callback
        running.set(true)
        Thread {
            try {
                serverSocket = ServerSocket(9999)
                while (running.get()) {
                    val client = serverSocket!!.accept()
                    onConnection?.invoke()
                    handleClient(client)
                }
            } catch (e: Exception) { if (!running.get()) return }
        }.start()
    }
    private fun handleClient(socket: Socket) {
        Thread {
            try {
                val output = socket.getOutputStream()
                camera = Camera.open(Camera.CAMERA_FACING_BACK)
                val params = camera!!.parameters
                params.setPreviewSize(640, 480)
                params.previewFormat = ImageFormat.NV21
                camera!!.parameters = params
                camera!!.setPreviewDisplay(null)
                camera!!.setPreviewCallback { data, _ ->
                    try {
                        val jpeg = YuvImage(data, ImageFormat.NV21, 640, 480, null)
                        val bos = ByteArrayOutputStream()
                        jpeg.compressToJpeg(android.graphics.Rect(0,0,640,480), 70, bos)
                        val jpegData = bos.toByteArray()
                        output.write(jpegData.size.toBigEndian())
                        output.write(jpegData)
                    } catch (e: Exception) {}
                }
                camera!!.startPreview()
                while (running.get() && !socket.isClosed) Thread.sleep(100)
            } catch (e: Exception) { e.printStackTrace() }
            finally { camera?.stopPreview(); camera?.release(); socket.close() }
        }.start()
    }
    fun stop() { running.set(false); try { serverSocket?.close() } catch (e: Exception) {} }
    private fun Int.toBigEndian(): ByteArray = byteArrayOf((this shr 24).toByte(), (this shr 16).toByte(), (this shr 8).toByte(), this.toByte())
}
