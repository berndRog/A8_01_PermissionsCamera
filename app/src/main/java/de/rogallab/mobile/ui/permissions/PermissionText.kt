package de.rogallab.mobile.ui.permissions


import android.content.Context
import de.rogallab.mobile.R

class PermissionCamera(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {

   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         _context.getString(R.string.declinedCamera)
      } else {
         _context.getString(R.string.permissionCamera)
      }
   }
}

class PermissionRecordAudio(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {
   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         _context.getString(R.string.declinedAudio)
      } else {
         _context.getString(R.string.permissionAudio)
      }
   }
}


class PermissionExternalStorage(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {
   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         _context.getString(R.string.declinedExternalStorage)
      } else {
         _context.getString(R.string.permissionExternalStorage)
      }
   }
}

class PermissionCoarseLocation(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {
   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         _context.getString(R.string.declinedCoarseLocation)
      } else {
         _context.getString(R.string.permissionCoarseLocation)
      }
   }
}

class PermissionFineLocation(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {
   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         _context.getString(R.string.declinedFineLocation)
      } else {
         _context.getString(R.string.permissionFineLocation)
      }
   }
}

class PermissionDefault(
   private val _context: Context,
   private val _permission: String
) : IPermissionText {
   override fun getDescription(isPermanentlyDeclined: Boolean): String {
      return if (isPermanentlyDeclined) {
         "Unfortunately you have permanently denied the $_permission. " +
            "You can now only grant the permission via app settings."
      } else {
         "This app needs access to $_permission to provide its functionality."
      }
   }
}

//class PermissionPhoneCall : IPermissionText {
//   override fun getDescription(context: Context, isPermanentlyDeclined: Boolean): String {
//      return if (isPermanentlyDeclined) {
//         "Es scheint als hätten Sie den Zugriff auf Anrufen mehrfach abgelehnt. " +
//            "Sie können diese Entscheidung nur über die App Einstellungen ändern."
//      } else {
//         "Die App erfordert den Zugriff auf das Telefon, um einen Anruf durchführen zu können."
//      }
//   }
//}


