/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioapp.commons.service.CardiovascularDataMarshaller;
import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;

public class CardiovascularDataTCPReceiverService extends
        CardiovascularDataService {

    private static class Receiver implements Runnable {

        private final Handler toastHandler;
        private final GraphViewObserver observer;
        private Context context;
        private CardiovascularDataTCPReceiverService serv;
        private int port;
        private String serverAddress;

        public Receiver(Handler toastHandler, CardiovascularDataTCPReceiverService serv,
                        String serverAddress, int port, GraphViewObserver observer) {
            this.context = context;
            this.serv = serv;
            this.serverAddress = serverAddress;
            this.port = port;
            this.observer = observer;
            this.toastHandler = toastHandler;
        }

        @Override
        public void run() {
            String line = "";
            Socket socket = null;
            try {
                socket = new Socket(serverAddress, port);
                BufferedReader inboundStream = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                while ((line = inboundStream.readLine()) != null) {
                    observer.update(CardiovascularDataMarshaller.unmarshal(line));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // TODO Can't create handler inside thread that has not called Looper.prepare
                Bundle bundle = new Bundle();
                bundle.putString("message", "IOException: " + e.getMessage() + ". Closing.");
                Message message = toastHandler.obtainMessage();
                message.setData(bundle);
                message.sendToTarget();
            } finally {
                if (socket != null)
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Bundle bundle = new Bundle();
                        bundle.putString("message", "Closing socket failed.");
                        Message message = toastHandler.obtainMessage();
                        message.setData(bundle);
                        message.sendToTarget();
                    }
            }
        }

    }

    private final Handler toastHandler;
    private Thread thread;
    private String serverAddress;
    private int port;
    private GraphViewObserver observer;
    private boolean isRunning;

    public CardiovascularDataTCPReceiverService(final Context context, String serverAddress, int port, GraphViewObserver observer)
            throws ServiceException {
        this.toastHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(context, message.getData().getString("message"), Toast.LENGTH_LONG).show();
            }
        };
        this.serverAddress = serverAddress;
        this.port = port;
        this.observer = observer;
        this.isRunning = false;
    }

    @Override
    public synchronized void start() {
        thread = new Thread(new Receiver(toastHandler, this, serverAddress, port, observer));
        thread.start();
        this.isRunning = true;
    }

    @Override
    public synchronized void stop() {
        thread.interrupt();
        this.isRunning = false;
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

}
