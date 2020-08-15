package org.personal.videotogether.server

import android.util.Log
import java.io.*
import java.net.Socket

class TCPClient(private val serverName: String, private val serverPort: Int) {

    private val TAG by lazy { javaClass.name }

    private var socket: Socket? = null
    private var serverInPut: InputStream? = null
    private var serverOutPut: OutputStream? = null
    var bufferedReader: BufferedReader? = null

    // 서버와 연결을 확인하는 메소드
    fun connect(): Boolean {
        try {
            socket = Socket(serverName, serverPort)
            serverOutPut = socket!!.getOutputStream()
            serverInPut = socket!!.getInputStream()
            bufferedReader = BufferedReader(InputStreamReader(serverInPut!!))

        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "TCPClient connect : IOException 발생")
        }
        return true
    }

    fun writeMessage(message: String) {
        val writer = PrintWriter(serverOutPut!!)
        writer.println(message)
        writer.flush()
    }

    fun readMessage(): String? {
        if (!socket!!.isClosed) {
            return bufferedReader?.readLine()
        }
        return null
    }

    fun socketClose() {
        Log.i(TAG, socket?.isClosed.toString())
        serverInPut?.close()
        serverOutPut?.close()
        bufferedReader?.close()
        socket?.close()
        Log.i(TAG, socket?.isClosed.toString())
    }
}

