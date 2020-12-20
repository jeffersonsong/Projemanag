package com.projemanag.utils

import com.projemanag.model.SelectedMembers
import com.projemanag.model.User

object SelectedMembersHelper {

    fun selectedMembersList(
        membersList: ArrayList<User>,
        assignedMembersList: ArrayList<String>
    ): ArrayList<SelectedMembers> {
        val assignedMembersSet = HashSet(assignedMembersList)

        val result = membersList.filter { user ->
            user.id.isNotEmpty() && assignedMembersSet.contains(user.id)
        }
            .map { user -> SelectedMembers(user.id, user.image) }

        return ArrayList(result)
    }
}
