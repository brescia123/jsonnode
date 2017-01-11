package it.gbresciani.jsonnode

import java.util.*

/**
 * Representation of a path in json structure
 *
 * @param path an arbitrary sequence of string that indicate several levels in json structure
 */
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

    /**
     *  Return first element of the path
     */
    fun head() = path.first()

    /**
     *  Return all the path except the first element
     */
    fun tailPath() = NodePath(path.drop(1))

    /**
     *  Return number of single elements in path
     */
    fun size() = path.size

    /**
     *  Return all the path as List
     */
    fun asList() = path.toList()
}