package it.gbresciani.jsonnode

import java.util.*

class NodePath(private vararg val path: String) {
    constructor(path: List<String>) : this(*path.toTypedArray())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as NodePath
        if (!Arrays.equals(path, other.path)) return false
        return true
    }

    override fun hashCode() = Arrays.hashCode(path)
    fun head() = path.first()
    fun tailPath() = NodePath(path.drop(1))
    fun size() = path.size
    fun asList() = path.toList()
}