package org.kdu.friendlybackup.api.friendly

import android.app.Activity
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class LockManager {
  private val lock = AtomicBoolean(false)

  fun lock() {
    lock.set(true)
  }

  fun release() {
    lock.set(false)
  }

  private var shownView: WeakReference<Activity?> = WeakReference(null)

  fun register(obs: Activity) {
    shownView = WeakReference(obs)
  }

  fun unregister() {
    shownView = WeakReference(null)
  }
}