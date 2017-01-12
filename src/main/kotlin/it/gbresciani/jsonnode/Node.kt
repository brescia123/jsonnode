package it.gbresciani.jsonnode

/** Representation of possible object in Json structure
 *
 * Possible values:
 * - Null : is a node with null as value
 * - Text : is a node with text type as value
 * - Bool : is a node with a boolean type as value
 * - Number : is a node with a number type as value
 * - Array : is a node with collection of type Node as value
 * - ObjectNode : is a node with a map<key: String, value: Node> as value where key
 *                represents the name of the field and value represents its value
 */
sealed class Node() : PrettyPrintable {
    object Null : Node() {
        override fun toString() = "null"
        override fun print(indentation: String) = toString()
    }

    class Text(val text: String) : Node() {
        override fun toString() = text
        override fun print(indentation: String) = "\"${toString()}\""
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Text

            if (text != other.text) return false

            return true
        }

        override fun hashCode() = text.hashCode()
    }

    class Bool(val bool: Boolean) : Node() {
        override fun toString() = bool.toString()
        override fun print(indentation: String) = toString()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Bool

            if (bool != other.bool) return false

            return true
        }

        override fun hashCode(): Int {
            return bool.hashCode()
        }
    }

    class Number(val number: kotlin.Number) : Node() {
        override fun toString() = number.toString()
        override fun print(indentation: String) = toString()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Number

            if (number != other.number) return false

            return true
        }

        override fun hashCode(): Int {
            return number.hashCode()
        }
    }

    class Array(val array: List<Node>) : Node() {
        override fun toString() = array.toString()
        override fun print(indentation: String) =
                "${array.foldIndexed("[") { index: Int, acc: String, node: Node ->
                    val isObject = node is ObjectNode
                    "$acc${if (isObject) "\n" else ""}${if (isObject) indentation else ""}${(node as? ObjectNode)?.print(indentation.plus("  ")) ?: node.print()}, "
                }}${if (array.getOrNull(0) is ObjectNode) "\n${indentation.dropLast(2)}" else ""}".dropLast(2) + "]"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Array

            if (array != other.array) return false

            return true
        }

        override fun hashCode(): Int {
            return array.hashCode()
        }


    }

    class ObjectNode(val map: Map<String, Node> = emptyMap()) : Node(), Map<String, Node> by map {

        /**
         * Join an arbitrary value at a specified path with [this]
         *
         * Note:
         * + if path doesn't exists it will create new path with passed value
         * + if path already exists, it will override the old value in path with new value passed
         * + if path partially exists it will add just the new part of path
         *
         * @param value the arbitrary object that must be added
         * @param at [NodePath] that indicate the position where to place the Node
         * @return an [ObjectNode] containing the resultant structure of old ObjectNode with new Node added
         */
        fun with(value: Any?, at: NodePath): ObjectNode = with(value.asNode(), at)

        /**
         * Join an arbitrary value at a specified path with [this]
         *
         * Note:
         * + if path doesn't exists it will create new path with passed value
         * + if path already exists, it will override the old value in path with new value passed
         *
         * @param value the arbitrary object that must be added
         * @param at the single level position where to place the Node
         * @return an [ObjectNode] containing the resultant structure of old ObjectNode with new Node added
         */
        fun with(value: Any?, at: String) = with(value.asNode(), at = NodePath(at))


        /**
         * Join a [Node] at a specified path with [this]
         *
         * Note:
         * + if path doesn't exists it will create new path with passed value
         * + if path already exists, it will override the old value in path with new value passed
         * + if path partially exists it will add just the new part of path
         *
         * @param value the new Node that must be added
         * @param at [NodePath] that indicate the position where to place the Node
         * @return an [ObjectNode] containing the resultant structure of old ObjectNode with new Node added
         */
        fun with(value: Node, at: String) = with(value, at = NodePath(at))

        /**
         * Let merge a new [ObjectNode] with [this]
         *
         * Note:
         * + if value is [null] the method will return [this]
         *
         * @param value the [ObjectNode] that must be merged with [this]
         * @return an [ObjectNode] containing the resultant structure
         */
        fun merge(value: ObjectNode?) = if (value == null)
            this
        else
            with(value, NodePath())


        /**
         * Join a Node value at a specified path with [this]
         *
         * Note:
         * + if path doesn't exists it will create new path with passed value
         * + if path already exists, it will override the old value in path with new value passed
         * + if path partially exists it will add just the new part of path
         *
         * @param value the new Node that must be added
         * @param at [NodePath] that indicate the position where to place the Node
         * @return an [ObjectNode] containing the resultant structure of old ObjectNode with new Node added
         */
        fun with(value: Node, at: NodePath): ObjectNode {

            val temp = mutableMapOf(*map.toList().toTypedArray())
            val path = at

            if (path.size() == 0 && value !is ObjectNode) return this
            if (path.size() == 0 && value is ObjectNode) {
                return value.toList().fold(this) { acc: ObjectNode, pair: Pair<String, Node> ->
                    acc.with(pair.second, at = NodePath(pair.first))
                }
            }

            val head = path.head()
            if (path.size() == 1) {
                if (value !is ObjectNode) {
                    temp.put(head, value)
                    return ObjectNode(temp)
                }
            }

            val node = temp[head]
            val tailPath = path.tailPath()
            if (temp.containsKey(head) && node is ObjectNode) {
                temp.put(head, node.with(value, at = tailPath))
            } else {
                temp.put(head, empty().with(value, at = tailPath))
            }
            return ObjectNode(temp)
        }

        /**
         * Return, if exists, the [Node] at specified path otherwise return null
         *
         * @param path collection of Strings that compose the path to find
         * @return [Node] at specified path
         */
        fun getNode(vararg path: String) = getNode(NodePath(*path))

        /**
         * Return, if exists, the [Node] at specified path otherwise return null
         *
         * @param path [NodePath] to find
         * @return [Node] at specified path
         */
        fun getNode(path: NodePath): Node? {
            val pathSize = path.size()
            if (pathSize == 0) return null

            val head = path.head()
            if (!containsKey(head)) return null
            if (pathSize == 1) return get(head)

            val node = get(head)
            val tailPath = path.tailPath()
            if (node !is ObjectNode) return node
            else return node.getNode(tailPath)
        }

        /**
         * Return a new [ObjectNode] created from values present in [this] at specified
         * paths
         *
         * @param paths arbitrary number of [NodePath] to find and retrieve
         * @return new [ObjectNode] created
         */
        fun extract(vararg paths: NodePath) = paths.fold(Node.ObjectNode()) { acc, path ->
            getNode(path)?.let { acc.with(it, at = path) } ?: acc
        }

        companion object {
            fun empty() = ObjectNode(emptyMap())
        }

        override fun print(indentation: String): String =
                "${toList().foldIndexed("{") { index: Int, acc: String, pair: Pair<String, Node> ->
                    val (key, node) = pair
                    "$acc\n$indentation\"$key\": ${(node as? PrettyPrintable)?.print(indentation.plus("  ")) ?: node.print()}"
                }}\n${indentation.dropLast(2)}}"

        override fun toString(): String = map.toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as ObjectNode

            if (map != other.map) return false

            return true
        }

        override fun hashCode(): Int {
            return map.hashCode()
        }


    }
}