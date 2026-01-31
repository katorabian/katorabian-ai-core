package com.katorabian.domain

import java.io.File

object Utils {
    fun String.toFile() = File(this)

}