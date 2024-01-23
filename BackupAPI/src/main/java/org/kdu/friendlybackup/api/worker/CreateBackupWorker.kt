package org.kdu.friendlybackup.api.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.kdu.friendlybackup.api.common.CommonApiConstants
import org.kdu.friendlybackup.api.friendly.BackupDataStore
import org.kdu.friendlybackup.api.friendly.BackupManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CreateBackupWorker(val context : Context, params: WorkerParameters) : Worker(context, params) {

  override fun doWork(): Result {
    Log.d("FA BackupWorker", "doWork()")
    //if(BackupDataStore.isBackupDataSaved(context)) return Result.success()

    Log.d("FA BackupWorker", "creating backup...")
    val outStream = ByteArrayOutputStream()
    val success = BackupManager.backupCreator?.writeBackup(context, outStream) ?: return Result.success(Data.Builder().apply {
      putInt(CommonApiConstants.RESULT_CODE, CommonApiConstants.RESULT_CODE_ERROR)
    }.build())

    if(!success) {
      return Result.success(Data.Builder().apply {
        putInt(CommonApiConstants.RESULT_CODE, CommonApiConstants.RESULT_CODE_ERROR)
      }.build())
    }
    Log.d("FA BackupWorker", "backup created")
    outStream.close()

    BackupDataStore.saveBackupData(context, ByteArrayInputStream(outStream.toByteArray()))

    return Result.success(Data.EMPTY)
  }

}