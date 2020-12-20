package com.projemanag.utils

import com.projemanag.model.SelectedMembers
import com.projemanag.model.User

object MembersHelper {

    fun deselect(mMembersDetailList: ArrayList<User>, user: User) {
        mMembersDetailList.filter { member ->
            member.id == user.id
        }.forEach { member ->
            member.selected = false
        }
    }

    fun flagMemberSelectedStatus(
        mMembersDetailList: ArrayList<User>,
        cardAssignedMembersList: ArrayList<String>
    ) {
        if (cardAssignedMembersList.isNotEmpty()) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            mMembersDetailList.filter { member ->
                member.id.isNotEmpty() && cardAssignedMembersList.contains(member.id)
            }.forEach { member ->
                member.selected = true
            }
        } else {
            mMembersDetailList.forEach { member -> member.selected = false }
        }
    }

    fun selectedMembersList(
        membersList: ArrayList<User>,
        assignedMembersList: ArrayList<String>
    ): ArrayList<SelectedMembers> {

        val result = membersList.filter { user ->
            user.id.isNotEmpty() && assignedMembersList.contains(user.id)
        }
            .map { user -> SelectedMembers(user.id, user.image) }

        return ArrayList(result)
    }
}
