// IBackupService.aidl
package org.kdu.friendlybackup.api;

interface IBackupService {

    ParcelFileDescriptor performRestore();
    void performBackup(in ParcelFileDescriptor input);

    Intent send(in Intent data);
}
