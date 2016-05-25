package com.example.administrator.test1;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ServerService extends IntentService {
    private boolean serviceEnabled;

    private ResultReceiver serverResult;
    private int port;

    DataOutputStream dos;
    Socket socket = null;

    public ServerService() {
        super("ServerService");
        Log.i("TAG", "서버 서비스 생성");
        serviceEnabled = true;
    }

    public class ServerServiceBinder extends Binder{
        ServerService getService(){
            return ServerService.this;
        }
    }

    private final IBinder sBinder = new ServerServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return sBinder;
    }

    public void onDestroy() {

        serviceEnabled = false;

        stopSelf();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");


        ServerSocket welcomeSocket = null;

        try {
            Log.i("TAG", "서버소켓생성");
            welcomeSocket = new ServerSocket(port);
            OutputStream os = null;

            //InputStream is = null;
            Log.i("TAG", "연결 대기");

            while (socket == null) {
                socket = welcomeSocket.accept();
            }

            Log.i("TAG", "연결 됨!");
            //is = socket.getInputStream();

            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            while (serviceEnabled) {


            }

            os.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            Log.i("TAG", e.getMessage());
        }
        Log.i("TAG", "서버 소켓 종료");

    }


    public synchronized void sendMusic(File file) {
        try {

            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(Constants.SEND_MUSIC);

            byte[] buffer = new byte[4096 * 64];

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            Log.i("TAG", "전송시작");
            while (true) {
                int bytesRead = bis.read(buffer, 0, buffer.length);

                if (bytesRead == -1) {
                    break;
                }

                dos.write(buffer, 0, bytesRead);
                dos.flush();
            }

            Log.i("TAG", "다보냄");

            dos.close();
            bis.close();
            fis.close();
        } catch (IOException e) {

        }
    }


    public synchronized void sendPosition(int position) {

        try {

            dos.writeUTF(Constants.SEND_POSITION);

            dos.writeInt(position);

        } catch (IOException e) {

        }
    }

}
