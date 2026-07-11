package com.jeet.androidreminderapp.domain.use_case

import com.jeet.androidreminderapp.domain.model.Task
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class EvaluateSmartReminderUseCase @Inject constructor() {

    operator fun invoke(task: Task, nowMillis: Long = System.currentTimeMillis()): Decision {
        val elapsedMinutes =
            task.lastTriggeredAt?.let { (nowMillis - it) / 60_000 } ?: Long.MAX_VALUE
        val shouldCheck = elapsedMinutes >= task.checkFrequencyMinutes

        // Frequent triggers (user near the geofence a lot) -> check more often (min 5 min).
        // Rare triggers -> relax the interval up to a max of 60 min to save battery.
        val nextFrequency = when {
            task.triggerCount >= 5 -> max(5, task.checkFrequencyMinutes - 5)
            task.triggerCount == 0 && elapsedMinutes > 120 -> min(
                60,
                task.checkFrequencyMinutes + 10
            )

            else -> task.checkFrequencyMinutes
        }

        return Decision(shouldCheckNow = shouldCheck, nextCheckFrequencyMinutes = nextFrequency)
    }
}


data class Decision(val shouldCheckNow: Boolean, val nextCheckFrequencyMinutes: Int)