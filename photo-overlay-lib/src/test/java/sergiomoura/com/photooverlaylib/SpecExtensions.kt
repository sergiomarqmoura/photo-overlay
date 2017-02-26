package sergiomoura.com.photooverlaylib

import org.jetbrains.spek.api.dsl.Dsl
import org.jetbrains.spek.api.dsl.Pending
import rx.Scheduler
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.plugins.RxJavaPlugins
import rx.plugins.RxJavaSchedulersHook
import rx.schedulers.Schedulers

inline fun Dsl.rxGroup(description: String, pending: Pending = Pending.No,
                       crossinline body: Dsl.() -> Unit) {
    group(description, pending) {
        val rxJavaSchedulersHook = object : RxJavaSchedulersHook() {
            override fun getIOScheduler(): Scheduler {
                return Schedulers.immediate()
            }

            override fun getNewThreadScheduler(): Scheduler {
                return Schedulers.immediate()
            }

        }

        val rxAndroidSchedulersHook = object : RxAndroidSchedulersHook() {
            override fun getMainThreadScheduler(): Scheduler {
                return Schedulers.immediate()
            }
        }

        beforeEach {
            RxAndroidPlugins.getInstance().reset()
            RxAndroidPlugins.getInstance().registerSchedulersHook(rxAndroidSchedulersHook)

            RxJavaPlugins.getInstance().reset()
            RxJavaPlugins.getInstance().registerSchedulersHook(rxJavaSchedulersHook)
        }

        body()

        afterEach {
            RxAndroidPlugins.getInstance().reset()
            RxJavaPlugins.getInstance().reset()
        }
    }
}