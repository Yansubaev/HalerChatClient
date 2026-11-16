package com.ians.halerchat.core.model

data class Room(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val ownerName: String,
    val createdAt: String,
    val memberCount: Int,
    val visibility: String,
    val inviteToken: String?,
    val accessCode: String?
)
