package org.kdu.friendlybackup.api

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.kdu.friendlybackup.api.friendly.BackupDataStore
import org.kdu.friendlybackup.api.friendly.BackupManager
import org.kdu.friendlybackup.api.friendly.IBackupCreator
import org.kdu.friendlybackup.api.friendly.IBackupRestorer
import org.kdu.friendlybackup.api.worker.CreateBackupWorker
import org.kdu.friendlybackup.api.worker.RestoreBackupWorker
import java.io.InputStream
import java.io.OutputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FaWorkerTest {
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

    @Test
    fun testCreateBackupWorker() {
        //val constraints: Constraints =
        //val worker = OneTimeWorkRequestBuilder<CreateBackupWorker>().build()
        //workManager.enqueue(worker)

        // driver = WorkManagerTestInitHelper.getTestDriver(context)
        // driver.setAllConstraintsMet(worker.id)
        //val workInfo = workManager.getWorkInfoById(worker.id).get()
        //assertEquals(WorkInfo.State.SUCCEEDED, workInfo.state)

//        val worker = OneTimeWorkRequestBuilder<CreateBackupWorker>().build()
//        workManager.enqueue(worker)
//        val info = workManager.getWorkInfoById(worker.id).get()
//        assertEquals(WorkInfo.State.SUCCEEDED, info.state)
        assertEquals(WorkInfo.State.SUCCEEDED, runWorker<CreateBackupWorker>().state)
    }

    @Test
    fun testRestoreBackupWorker() {
        BackupDataStore.saveRestoreData(appContext, "{ 'test': [] }".byteInputStream())
        assertEquals(WorkInfo.State.SUCCEEDED, runWorker<RestoreBackupWorker>().state)
    }

    @Test
    fun testRestoreBackupWorkerWithoutRestoreData() {
        assertEquals(WorkInfo.State.FAILED, runWorker<RestoreBackupWorker>().state)
    }

    inline fun <reified T : Worker> runWorker(): WorkInfo {
        val worker = OneTimeWorkRequestBuilder<T>().build()
        workManager.enqueue(worker)
        return workManager.getWorkInfoById(worker.id).get()
    }
}