package com.projemanag.utils

import com.projemanag.model.SelectedMembers
import com.projemanag.model.User

object SelectedMembersHelper {

    fun selectedMembersList(
        membersList: ArrayList<User>,
        assignedMembersList: ArrayList<String>
    ): ArrayList<SelectedMembers> {
        // A instance of selected members list.
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        // Here we got the detail list of members and add it to the selected members list as required.
        for (i in membersList.indices) {
            for (j in assignedMembersList) {
                if (membersList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        membersList[i].id,
                        membersList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }
        return selectedMembersList
    }
}