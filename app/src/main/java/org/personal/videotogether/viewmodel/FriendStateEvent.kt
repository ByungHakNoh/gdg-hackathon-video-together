package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData

sealed class FriendStateEvent {
    object GetFriendListFromLocal : FriendStateEvent()
    data class GetFriendListFromServer(val userId: Int) : FriendStateEvent()
    data class SearchFriend(val userId: Int, val friendEmail: String) : FriendStateEvent()
    data class AddFriend(val userId: Int, val friendUserData: FriendData) : FriendStateEvent()
}