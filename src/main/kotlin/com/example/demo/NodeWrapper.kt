package com.example.demo

data class NodeWrapper(
    val fieldName: String?,
    val type: String?,
    val children: MutableList<NodeWrapper> = mutableListOf()
)