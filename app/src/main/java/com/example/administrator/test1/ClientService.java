package com.example.administrator.test1;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ClientService extends IntentService {


    private int port;

    private Boolean serviceEnabled;



    private ResultReceiver clientResult;

    public ClientService() {
        super("ClientService");
        Log.i("TAG", "클라이언트 서비스 시작");
        serviceEnabled = true;
    }

    public class ClientServiceBinder extends Binder{
        ClientService getService(){
            return ClientService.this;
        }
    }

    private final IBinder cBinder = new ClientServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return cBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceEnabled = false;
        stopSelf();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean isConnected = false;
        port = (Integer) intent.getExtras().get("port");
        clientResult = (ResultReceiver) intent.getExtras().get("clientResult");

        try {
            InetAddress targetIP = InetAddress.getByName("192.168.49.1");

            Socket clientSocket = null;
            //OutputStream os = null;

            while (!isConnected) {
                Thread.sleep(1000);
                clientSocket = new Socket(targetIP, port);
                isConnected = clientSocket.isConnected();
            }

            Log.i("TAG", "클라이언트 소켓 연결됨");

            InputStream is = clientSocket.getInputStream();
            //dis = new DataInputStream(is);

            //OutputStream os = clientSocket.getOutputStream();

            File file = new File("/storage/emulated/0/Download/a.mp3");

            byte[] buffer = new byte[4096 * 64];
            int bytesRead;
            String state;

            while (serviceEnabled) {
                DataInputStream dis = new DataInputStream(is);
                try {
                    state = dis.readUTF();

                    if (state.equals(Constants.SEND_MUSIC)) {

                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        while (true) {
                            bytesRead = is.read(buffer, 0, buffer.length);
                            Log.i("TAG", String.valueOf(bytesRead) + " 바이트를 읽음");
                            if ( bytesRead == -1 ) {
                                break;
                            }
                            bos.write(buffer, 0, bytesRead);
                            bos.flush();

                        }

                        Log.i("Tag", "다받았다");
                        bos.close();
                        fos.close();

                        dis.close();
                    } else if (state.equals(Constants.SEND_POSITION) ) {
                        Log.i("TAG", dis.readInt() + "");
                    }
                } catch (Exception e) {

                }

            }

            is.close();
            clientSocket.close();

        } catch (Exception e) {
        }

        Log.i("TAG", "클라이언트 소켓연결 종료");


    }


}
