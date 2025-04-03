import com.microej.gradle.plugins.TestTarget

/*
 * Kotlin
 *
 * Copyright 2025 HAJJI Amin
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */
plugins {
    id("com.microej.gradle.application") version "1.0.0"
}

group = "com.microej"
version = "1.0.0"

microej {
    applicationEntryPoint = "com.microej.weatherreport.Main"
    // Uncomment to use "prod" architecture when using a VEE Port (defaults to "eval")
    // architectureUsage = "prod"
    produceVirtualDeviceDuringBuild()
}

dependencies {
    implementation("ej.api:edc:1.3.5")
    implementation("ej.api:bon:1.4.3")
    implementation("ej.api:net:1.1.4")
    implementation("ej.api:ssl:2.2.3")
    implementation("ej.api:microui:3.4.0")
    implementation("ej.api:drawing:1.0.4")

    implementation("ej.library.runtime:service:1.1.1")

    //UI Libraries
    implementation("ej.library.ui:widget:5.1.0")
    implementation("ej.library.ui:mwt:3.5.1")

    //REST Libraries
    implementation("ej.library.eclasspath:httpsclient:1.3.0")
    implementation("ej.library.iot:restclient:1.1.0")
    implementation("ej.library.eclasspath:logging:1.2.1")
    implementation("ej.library.service:location:2.1.0")

    //This is used in case the application is run in standalone, in a multiapp firmware, the Connectivity Manager is provided by the firmware.
    implementation("ej.library.iot:connectivity:2.1.0")

    /*
     * To use a VEE Port published in an artifact repository use this VEE Port dependency.
     */
    microejVee("com.microej.veeport.st.stm32f7508-dk:R0OUY_eval:2.3.0")
}
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            microej.useMicroejTestEngine(this)

            dependencies {
                implementation(project())
                implementation("ej.api:edc:1.3.7")
                implementation("ej.library.test:junit:1.10.1")
                runtimeOnly("org.junit.platform:junit-platform-launcher:1.8.2")
            }

            targets {
                all {
                    testTask.configure {
                        doFirst {
                            systemProperties["microej.testsuite.properties.S3.cc.activated"] = "true"
                            systemProperties["microej.testsuite.properties.S3.cc.thread.period"] = "15"
                            systemProperties["microej.testsuite.properties.S3.DisableClassFileVersionCheck"] = "true"
                        }
                    }
                }
            }
        }
    }
}