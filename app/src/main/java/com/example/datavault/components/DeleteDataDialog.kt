package com.example.datavault.components

import android.content.Context
import android.widget.Toast
import com.example.datavault.models.DataModelRetrieve
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteDataDialog {
    fun notifyUser(context: Context, dataContent: DataModelRetrieve) {
        val name = dataContent.appName

        MaterialAlertDialogBuilder(context)
            .setTitle("Deleting $name")
            .setMessage("Do you really want to delete $name?")
            .setNeutralButton("CANCEL") {dialog, _ -> dialog.cancel() }
            .setPositiveButton("DELETE") {_, _ ->
                Toast.makeText(
                    context, "Successfully deleted this data",
                    Toast.LENGTH_SHORT
                ).show()
            }.show()
    }
}