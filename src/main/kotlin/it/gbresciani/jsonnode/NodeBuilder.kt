package it.gbresciani.jsonnode

import it.gbresciani.jsonnode.Node.ObjectNode

fun root(init: MutableList<Pair<NodePath, Node>>.() -> Unit): ObjectNode {
    val list = mutableListOf<Pair<NodePath, Node>>()
    list.init()
    return list.fold(ObjectNode()) { acc, pair -> acc.with(pair.second, at = pair.first)}
}

fun MutableList<Pair<NodePath, Node>>.n(key: String, value: Any?) {
    add(NodePath(key) to value.asNode())
}

fun MutableList<Pair<NodePath, Node>>.n(key: String, init: MutableList<Pair<NodePath, Node>>.() -> Unit): Unit {
    val list = mutableListOf<Pair<NodePath, Node>>()
    list.init()
    val objectNode = list.fold(ObjectNode()) { acc, pair -> acc.with(pair.second, at = pair.first) }
    add(NodePath(key) to objectNode)
}