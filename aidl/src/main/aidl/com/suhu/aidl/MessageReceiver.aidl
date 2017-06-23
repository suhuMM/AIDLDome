// MessageReceiver.aidl
package com.suhu.aidl;
import com.suhu.aidl.data.MessageModel;

interface MessageReceiver {
    void onMessageReceived(in MessageModel receivedMessage);
}
