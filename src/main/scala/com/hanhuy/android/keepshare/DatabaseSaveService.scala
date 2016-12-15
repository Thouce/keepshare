package com.hanhuy.android.keepshare

import android.widget.Toast

import android.app.{Notification, Service}
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.hanhuy.android.common.Futures
import com.hanhuy.android.common.Logcat
import com.hanhuy.keepassj.PwDatabase

import scala.concurrent.Future

import Futures._

object DatabaseSaveService {
  private val _saving: Var[Boolean] = Var(false)
  def saving: Obs[Boolean] = _saving

  def save(): Unit = {
    Application.instance.startService(
      new Intent(Application.instance, classOf[DatabaseSaveService]))
  }
}

class DatabaseSaveService extends Service {
  import DatabaseSaveService._
  val log = Logcat("DatabaseSaveService")

  def onBind(p1: Intent) = null

  private[this] var currentSave: Option[Future[PwDatabase]] = None
  private[this] var saveQueued = false

  def notification = {
    new NotificationCompat.Builder(this)
      .setPriority(Notification.PRIORITY_MIN)
      .setContentTitle("KeepShare: Saving database")
      .setSmallIcon(R.drawable.ic_save_white_24dp)
    .build
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int) = {
    _saving() = true
    startForeground(Notifications.NOTIF_DATABASE_SAVING, notification)
    if (currentSave.isEmpty) {
      val f = Database.save()
      f onComplete onSaved
      currentSave = Some(f)
      saveQueued = false
    } else {
      saveQueued = true
    }
    Service.START_NOT_STICKY
  }

  val onSaved: PartialFunction[util.Try[PwDatabase],Any] = {
    case util.Success(_) =>
      if (saveQueued) {
        val f = Database.save()
        f onComplete onSaved
        currentSave = Some(f)
      } else {
        currentSave = None
        _saving() = false
        stopForeground(true)
        stopSelf()
      }
      saveQueued = false
    case util.Failure(ex) =>
      stopForeground(true)
      stopSelf()
      log.e("Failed to save database: " + ex.getMessage, ex)
      Future.main {
        Toast.makeText(this, "Failed to save database: " + ex.getMessage, Toast.LENGTH_LONG).show()
      }
  }
}
