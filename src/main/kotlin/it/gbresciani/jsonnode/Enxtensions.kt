package it.gbresciani.jsonnode

fun Any?.asNode(): Node = when (this) {
    null -> Node.Null
    is Node -> this
    is String -> Node.Text(this)
    is Boolean -> Node.Bool(this)
    is Int -> Node.Number(this)
    is Long -> Node.Number(this)
    is Float -> Node.Number(this)
    is Double -> Node.Number(this)
    is List<*> -> Node.Array(this.map(Any?::asNode))
    is kotlin.Array<*> -> Node.Array(this.map(Any?::asNode))
    else -> Node.Null
}