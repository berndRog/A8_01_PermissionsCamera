package de.rogallab.mobile.ui.permissions

interface IPermissionText {
   fun getDescription(isPermanentlyDeclined: Boolean): String
}