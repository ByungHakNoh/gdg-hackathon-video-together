package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData

sealed class FriendStateEvent {
    class SearchFriend(val userId: Int, val friendEmail: String) : FriendStateEvent()
    class AddFriend(val userId: Int, val friendUserData: FriendData) : FriendStateEvent()
    object GetFriendListFromLocal : FriendStateEvent()
}