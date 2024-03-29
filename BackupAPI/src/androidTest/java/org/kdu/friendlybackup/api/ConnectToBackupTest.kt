package org.kdu.friendlybackup.api

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.kdu.friendlybackup.api.friendly.BackupManager
import org.kdu.friendlybackup.api.friendly.IBackupCreator
import org.kdu.friendlybackup.api.friendly.IBackupRestorer
import org.kdu.friendlybackup.api.worker.ConnectBackupWorker
import org.kdu.friendlybackup.api.worker.CreateBackupWorker
import java.io.InputStream
import java.io.OutputStream

@RunWith(AndroidJUnit4::class)
class ConnectToBackupTest {
  val packageName = "org.kdu.friendlybackup.api"
  val appContext = InstrumentationRegistry.getInstrumentation().targetContext
  lateinit var workManager: WorkManager

  @Before
  fun setUp() {
    val config = Configuration.Builder().setExecutor(SynchronousExecutor())
      .setMinimumLoggingLevel(Log.DEBUG).build()
    WorkManagerTestInitHelper.initializeTestWorkManager(appContext, config)
    workManager = WorkManager.getInstance(appContext)

    BackupManager.backupRestorer = object : IBackupRestorer {
      override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        Thread.sleep(1000)
        Log.d("BACKUP CREATOR", "createBackup called.")
        return true
      }
    }

    BackupManager.backupCreator = object : IBackupCreator {
      override fun writeBackup(context: Context, outputStream: OutputStream): Boolean {
        Thread.sleep(1000)
        Log.d("BACKUP RESTORER", "restoreBackup called.")
        outputStream.write("{ 'test': [] }".toByteArray())
        return true
      }
    }
  }

  @Ignore
  @Test
  fun connect() {
    val backupWork = OneTimeWorkRequest.Builder(CreateBackupWorker::class.java)
      .addTag("org.kdu.friendlybackup.api.CreateBackupWork")
      //.setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
      .build()

    val connectWork = OneTimeWorkRequest.Builder(ConnectBackupWorker::class.java)
      .addTag("org.kdu.friendlybackup.api.ConnectBackupWork")
      .build()


    runBlocking(Dispatchers.Default) {
      workManager
        .beginUniqueWork("org.kdu.friendlybackup.api.ConnectBackupWork", ExistingWorkPolicy.KEEP, backupWork)
        .then(connectWork).enqueue()

      assertEquals(
        WorkInfo.State.SUCCEEDED,
        workManager.getWorkInfoById(backupWork.id).get().state
      )

      do {
        delay(1000)
      } while (workManager.getWorkInfoById(connectWork.id).get().state == WorkInfo.State.RUNNING)

      assertEquals(
        WorkInfo.State.SUCCEEDED,
        workManager.getWorkInfoById(connectWork.id).get().state
      )
    }
  }
}