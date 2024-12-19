package de.rogallab.mobile.domain.utilities

fun splitUrl(url: String): Pair<String, String> {
   val fileName = url.substringAfterLast('/')
   val extension = fileName.substringAfterLast('.', "")
   return Pair(fileName, extension)
}