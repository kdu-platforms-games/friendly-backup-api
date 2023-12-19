package org.kdu.friendlybackup.api.friendly

import android.content.Context
import java.io.OutputStream

/**
 * Interface for the FA. An instance of this class should be passed to the BackupManager on
 * Application start. The logic to create a backup should be implemented in the
 * {@link #createBackup(Context)} method.
 */
interface IBackupCreator {
    fun writeBackup(context: Context, outputStream: OutputStream) : Boolean
}