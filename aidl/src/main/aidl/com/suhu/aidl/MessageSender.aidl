// MessageSender.aidl
package com.suhu.aidl;
import com.suhu.aidl.data.MessageModel;
import com.suhu.aidl.MessageReceiver;

interface MessageSender {
    void sendMessage(in MessageModel messageModel);

    void registerReceiveListener(MessageReceiver messageReceiver);

    void unregisterReceiveListener(MessageReceiver messageReceiver);
}
