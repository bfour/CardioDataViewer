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

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.service.CardiovascularDataMarshaller;
import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;

public class CardiovascularDataTCPReceiverService extends
        CardiovascularDataService {

    private static class Receiver implements Runnable {

        private final Handler notifHandler;
        private Context context;
        private CardiovascularDataTCPReceiverService serv;
        private int port;
        private String serverAddress;

        public Receiver(final Context context, CardiovascularDataTCPReceiverService serv,
                        String serverAddress, int port) {
            this.context = context;
            this.serv = serv;
            this.serverAddress = serverAddress;
            this.port = port;
            notifHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    Toast.makeText(context, message.getData().getString("message"), Toast.LENGTH_LONG).show();
                }
            };
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
                    serv.receive(CardiovascularDataMarshaller.unmarshal(line));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // TODO Can't create handler inside thread that has not called Looper.prepare
                Bundle bundle = new Bundle();
                bundle.putString("message", "IOException: " + e.getMessage() + ". Closing.");
                Message message = new Message();
                message.setData(bundle);
                notifHandler.handleMessage(message);
            } finally {
                if (socket != null)
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Bundle bundle = new Bundle();
                        bundle.putString("message", "Closing socket failed.");
                        Message message = new Message();
                        message.setData(bundle);
                        notifHandler.handleMessage(message);
                    }
            }
        }

    }

    private Thread thread;
    private Context context;
    private String serverAddress;
    private int port;
    private boolean isRunning;

    public CardiovascularDataTCPReceiverService(Context context, String serverAddress, int port)
            throws ServiceException {
        this.context = context;
        this.serverAddress = serverAddress;
        this.port = port;
        this.isRunning = false;
    }

    @Override
    public synchronized void start() {
        thread = new Thread(new Receiver(context, this, serverAddress, port));
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

    private synchronized void receive(CardiovascularData data) {
        setChanged();
        notifyObservers(data);
    }

}
