package com.sebaslogen.resacaapp.sample.utils

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class FlakyTestRetryRule(private val retryCount: Int) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                for (i in 1..retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        println("${description.displayName}: run $i failed")
                    }
                }
                println("${description.displayName}: giving up after $retryCount failures")
                throw caughtThrowable!!
            }
        }
    }
}
