package com.example.teamsphere.fcm


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.teamsphere.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) { // when we receive a message (data message here not the notification message)

        Log.d(TAG,"FROM: ${message.from}")
        message.data.isNotEmpty().let {
            Log.d(TAG,"Message data Payload: ${message.data}")
            val title=message.data[Constants.FCM_KEY_TITLE]!!
            val mess=message.data[Constants.FCM_KEY_MESSAGE]!!
            sendNotification(title,mess)
        }
        message.notification?.let {
            Log.d(TAG,"Message Notification Body: ${it.body}")
        }


        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG,"Refreshed Token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String){
        // Implement the functionality to send the registration to the server
    }

    private fun sendNotification(title:String,message:String){
        val intent=if(FirestoreClass().getCurrentUserID().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        } else {
            Intent(this,SignIn::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) // to make sure that a specific activity is set to a specific position inside of the stack
        val pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelID=this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundURI=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this,channelID).setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title).setContentText(message).setAutoCancel(true).
            setSound(defaultSoundURI).setContentIntent(pendingIntent)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel=NotificationChannel(channelID,"Channel TeamSphere title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}