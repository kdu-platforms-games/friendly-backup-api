package org.kdu.friendlybackup.api.friendly

object BackupManager {
    @JvmStatic var backupCreator : IBackupCreator? = null
    @JvmStatic var backupRestorer : IBackupRestorer? = null
}