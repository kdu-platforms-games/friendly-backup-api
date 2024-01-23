package org.kdu.friendlybackup.api.friendly

import android.content.Intent
import android.os.Binder
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.kdu.friendlybackup.api.IFAService
import org.kdu.friendlybackup.api.common.AbstractAuthService
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_CODE
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_CODE_ERROR
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_CODE_SUCCESS
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_ERROR
import org.kdu.friendlybackup.api.common.FriendlyApi.ACTION_CONNECT
import org.kdu.friendlybackup.api.common.FriendlyApi.EXTRA_CONNECT_IMMEDIATE
import org.kdu.friendlybackup.api.common.FriendlyApi.EXTRA_CONNECT_PACKAGE_NAME
import org.kdu.friendlybackup.api.common.FriendlyError
import org.kdu.friendlybackup.api.util.ApiFormatter
import org.kdu.friendlybackup.api.worker.ConnectBackupWorker
import org.kdu.friendlybackup.api.worker.CreateBackupWorker
import java.util.concurrent.Executors

/**
 * This class is meant to be extended by the FA. Also it should then be included in the FA's
 * AndroidManifest.xml file.
 *
 * <pre>
 * {@code
 *      <service
 *         android:name=".FAAuthService"
 *         android:enabled="true"
 *         android:exported="true"
 *         android:process=":backup"
 *         tools:ignore="ExportedService">
 *         <intent-filter>
 *           <action android:name="org.kdu.friendlybackup.api.friendly.FAAuthService" />
 *         </intent-filter>
 *      </service>
 *      }
 * </pre>
 */
abstract class FAAuthService : AbstractAuthService() {

  val TAG = "FA AuthService"

  val executor = Executors.newSingleThreadExecutor()

  override val SUPPORTED_API_VERSIONS = listOf(1)

  override val mBinder : IFAService.Stub = object : IFAService.Stub()  {

    override fun send(data: Intent?): Intent {
      Log.d(this.javaClass.simpleName, "Intent received: ${ApiFormatter.formatIntent(data)}")
      val result = canAccess(data, Binder.getCallingUid())
      if(result != null) {
        return result
      }
      // data can not be null here else canAccess(Intent) would have returned an error
      val resultIntent = handle(data!!)
      Log.d(this.javaClass.simpleName, "Sent Reply: ${ApiFormatter.formatIntent(resultIntent)}")
      return resultIntent
    }

    private fun handle(data: Intent): Intent {
      return when(data.action) {
        ACTION_CONNECT -> handleConnect(data)
        else -> Intent().apply {
          putExtra(RESULT_CODE, RESULT_CODE_ERROR)
          putExtra(RESULT_ERROR,
            FriendlyError(
              FriendlyError.FriendlyErrorCode.ACTION_ERROR,
              "Action ${data.action} is unsupported."
            )
          )
        }
      }
    }

    private fun handleConnect(data: Intent): Intent {
      val backupPackageName = data.getStringExtra(EXTRA_CONNECT_PACKAGE_NAME)
      val connectImmediately = data.getBooleanExtra(EXTRA_CONNECT_IMMEDIATE, false)

      return if(startBackupProcess())
        Intent().apply {
          putExtra(RESULT_CODE, RESULT_CODE_SUCCESS)
        }
      else
        Intent().apply {
          putExtra(RESULT_CODE, RESULT_CODE_ERROR)
        }
    }
  }

  private fun startBackupProcess() : Boolean {
    val backupWork = OneTimeWorkRequest.Builder(CreateBackupWorker::class.java)
      //.addTag("org.kdu.friendlybackup.api.CreateBackupWork")
      //.setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
      .build()

    val connectWork = OneTimeWorkRequest.Builder(ConnectBackupWorker::class.java)
      .addTag("org.kdu.friendlybackup.api.ConnectBackupWork")
      .build()

    // if connection is already running - don't cancel it
    val connectInfo = WorkManager.getInstance(this@FAAuthService).getWorkInfosByTag("org.kdu.friendlybackup.api.ConnectBackupWork").get()
    if(connectInfo != null && connectInfo.isNotEmpty() && connectInfo[0].state == WorkInfo.State.RUNNING) {
      return true
    }

    WorkManager.getInstance(this@FAAuthService)
      .beginUniqueWork("org.kdu.friendlybackup.api.ConnectBackupWork", ExistingWorkPolicy.REPLACE, backupWork).then(connectWork)
      .enqueue()

    val workInfo = WorkManager.getInstance(this@FAAuthService).getWorkInfoById(backupWork.id).get()
      ?: return false

    Log.d(TAG, "CreateBackupWorker: ${workInfo.state.name}")
    return workInfo.state == WorkInfo.State.SUCCEEDED
      || workInfo.state == WorkInfo.State.ENQUEUED
      || workInfo.state == WorkInfo.State.RUNNING
  }
}