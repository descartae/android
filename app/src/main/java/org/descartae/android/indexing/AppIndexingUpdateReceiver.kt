package org.descartae.android.indexing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.appindexing.FirebaseAppIndex

class AppIndexingUpdateReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent != null && FirebaseAppIndex.ACTION_UPDATE_INDEX.equals(intent.action)) {
      context?.let { AppIndexingUpdateService.enqueueWork(it) }
    }
  }
}