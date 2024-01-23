package org.kdu.friendlybackup.api.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.kdu.friendlybackup.api.friendly.BackupDataStore
import org.kdu.friendlybackup.api.friendly.BackupManager

class RestoreBackupWorker(val context : Context, params: WorkerParameters) : Worker(context, params) {

  override fun doWork(): Result {
    val restoreData = BackupDataStore.getRestoreData(context) ?: return Result.failure()
    val backupRestorer = BackupManager.backupRestorer ?: return Result.failure()

    val success = backupRestorer.restoreBackup(context, restoreData)

    if(!success) {
      return Result.failure()
    }

    // clean backup and restore data
    BackupDataStore.cleanRestoreData(context)
    BackupDataStore.cleanBackupDataIfNoRestoreData(context)

    return Result.success(Data.EMPTY)
  }

}