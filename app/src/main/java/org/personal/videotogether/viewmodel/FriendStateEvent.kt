package org.personal.videotogether.viewmodel

import org.personal.videotogether.domianmodel.FriendData

sealed class FriendStateEvent {
    object GetFriendListFromLocal : FriendStateEvent()
    class GetFriendListFromServer(val userId: Int) : FriendStateEvent()
    class SearchFriend(val userId: Int, val friendEmail: String) : FriendStateEvent()
    class AddFriend(val userId: Int, val friendUserData: FriendData) : FriendStateEvent()
}