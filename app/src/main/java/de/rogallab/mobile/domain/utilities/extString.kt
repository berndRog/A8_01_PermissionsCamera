package de.rogallab.mobile.domain.utilities

import kotlin.text.substring

fun String.maxChar(n:Int): String {
   val end = Math.min(this.length, n)
   val result = this.substring(0, end)
   return result
}