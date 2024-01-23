package org.kdu.friendlybackup.api.friendly

import android.content.Context
import java.io.InputStream

/**
 * Interface for the FA. An instance of this class should be passed to the BackupManager on
 * Application start. The logic to restore a backup should be implemented in the
 * {@link #restoreBackup(Context, String)} method.
 */
interface IBackupRestorer {
  fun restoreBackup(context: Context, restoreData: InputStream) : Boolean
}