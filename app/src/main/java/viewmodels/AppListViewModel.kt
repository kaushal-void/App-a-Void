package com.example.app_a_void.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    private val _apps = MutableLiveData<List<ApplicationInfo>>()
    val apps: LiveData<List<ApplicationInfo>> = _apps

    init {
        fetchInstalledApps()
    }

    private fun fetchInstalledApps() {
        val pm = getApplication<Application>().packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check for package visibility compliance on Android 11 and above
            pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)
        } else {
            // Continue using the original method on earlier versions of Android
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }

        _apps.postValue(installedApps)
    }
}
