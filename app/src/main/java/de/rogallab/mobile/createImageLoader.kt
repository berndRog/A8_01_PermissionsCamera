package de.rogallab.mobile

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

fun createImageLoader(context: Context): ImageLoader {
   return ImageLoader.Builder(context)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .memoryCache {
         MemoryCache.Builder(context)
            .maxSizePercent(0.10) // 10 % of memory used for caching
            .strongReferencesEnabled(true)
            .build()
      }
      .diskCachePolicy(CachePolicy.ENABLED)
      .diskCache {
         DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(1024L * 1024L * 100L) // 100 MB
            .build()
      }
      .networkCachePolicy(CachePolicy.ENABLED)
      .build()
}
