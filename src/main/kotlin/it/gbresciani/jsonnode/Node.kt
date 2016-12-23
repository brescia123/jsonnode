package it.gbresciani.jsonnode

sealed class Node() {
    object Null : Node() {
        override fun toString() = "null"
    }

    class Text(val text: String) : Node() {
        override fun toString() = "\"$text\""
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Text

            if (text != other.text) return false

            return true
        }

        override fun hashCode(): Int {
            return text.hashCode()
        }
    }

    class Bool(val bool: Boolean) : Node() {
        override fun toString() = bool.toString()
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

    class Array(val array: List<Node>) : Node(), PrettyPrintable {
        override fun toString() = array.toString()
        override fun print(indentation: String) =
                "${array.foldIndexed("[") { index: Int, acc: String, node: Node ->
                    val isObject = node is ObjectNode
                    "$acc${if (isObject) "\n" else ""}${if (isObject) indentation else ""}${(node as? ObjectNode)?.print(indentation.plus("  ")) ?: node}, "
                }}${if (this.array.getOrNull(0) is ObjectNode) "\n${indentation.dropLast(2)}" else ""}".dropLast(2) + "]"

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

    class ObjectNode(val map: Map<String, Node> = emptyMap()) : Node(), PrettyPrintable, Map<String, Node> by map {

        fun with(value: Any?, at: NodePath): ObjectNode = with(value.asNode(), at)
        fun with(value: Any?, at: String) = with(value.asNode(), at = NodePath(at))
        fun with(value: Node, at: String) = with(value, at = NodePath(at))
        fun merge(value: ObjectNode) = with(value, NodePath())

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

        fun getNode(vararg path: String) = getNode(NodePath(*path))

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

        fun extract(vararg paths: NodePath) = paths.fold(Node.ObjectNode()) { acc, path ->
            acc.with(getNode(path), at = path)
        }

        companion object {
            fun empty() = ObjectNode(emptyMap())
        }

        override fun print(indentation: String): String =
                "${toList().foldIndexed("{") { index: Int, acc: String, pair: Pair<String, Node> ->
                    val (key, node) = pair
                    "$acc\n$indentation\"$key\": ${(node as? PrettyPrintable)?.print(indentation.plus("  ")) ?: node}"
                }}\n${indentation.dropLast(2)}}"

        override fun toString(): String = print()

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