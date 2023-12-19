package org.kdu.friendlybackup.api.common

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.kdu.friendlybackup.api.common.CommonApiConstants.EXTRA_API_VERSION
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_CODE
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_CODE_ERROR
import org.kdu.friendlybackup.api.common.CommonApiConstants.RESULT_ERROR
import org.kdu.friendlybackup.api.util.AuthenticationHelper

/**
 * Base class that provides a function that checks for permissions and API versions.
 */
abstract class AbstractAuthService : Service() {
    protected abstract val mBinder : IBinder
    protected abstract val SUPPORTED_API_VERSIONS : List<Int>

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    /**
     * Checks if the data is correct and if the calling process has access rights. This function is
     * meant to be called within the Binder process.
     *
     * @return null if access is granted or an Intent with the RESULT_CODE set to RESULT_CODE_ERROR
     */
    protected fun canAccess(data : Intent?, callerId : Int) : Intent? {
        // check if intent is not null
        if(data == null) {
            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                putExtra(RESULT_ERROR,
                    FriendlyError(
                        FriendlyError.FriendlyErrorCode.INTENT_ERROR,
                        "Intent can not be null!"
                    )
                )
            }
        }

        // check if valid api version
        val version = data.getIntExtra(EXTRA_API_VERSION, -1)
        if(version == -1 || !SUPPORTED_API_VERSIONS.contains(version)) {
            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                putExtra(RESULT_ERROR,
                    FriendlyError(
                        FriendlyError.FriendlyErrorCode.API_VERSION_UNSUPPORTED,
                        "API Version '$version' is unsupported. Valid API versions are: $SUPPORTED_API_VERSIONS")
                    )
            }
        }

        // check if calling uid has permission to access this service
        if(!AuthenticationHelper.authenticate(applicationContext, callerId)) {
            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                putExtra(RESULT_ERROR,
                    FriendlyError(
                        FriendlyError.FriendlyErrorCode.AUTHENTICATION_ERROR,
                        "Authentication failed."
                    )
                )
            }
        }

        // is the action empty?
        if(data.action.isNullOrEmpty()) {
            return Intent().apply {
                putExtra(RESULT_CODE, RESULT_CODE_ERROR)
                putExtra(RESULT_ERROR,
                    FriendlyError(
                        FriendlyError.FriendlyErrorCode.ACTION_ERROR,
                        "Action can not be null or empty."
                    )
                )
            }
        }

        return null
    }
}