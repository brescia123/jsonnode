package it.gbresciani.jsonnode

import io.kotlintest.properties.Gen
import io.kotlintest.properties.Gen.Companion.oneOf
import it.gbresciani.jsonnode.Node.*
import it.gbresciani.jsonnode.Node.Array
import it.gbresciani.jsonnode.Node.Number
import java.util.*

private val RANDOM = Random()

interface CustomGen {

    companion object {

        val valueNodeGenerators = listOf(textNode(), boolNode(), numberNode())

        fun node(): Gen<Node> = object : Gen<Node> {
            override fun generate() = oneOf(valueNodeGenerators + objectNode() + arrayNode()).generate().generate()
        }

        fun valueNode(): Gen<Node> = object : Gen<Node> {
            override fun generate() = oneOf(valueNodeGenerators).generate().generate()
        }

        fun textNode(): Gen<Text> = object : Gen<Text> {
            override fun generate() = Text(Gen.string().generate())
        }

        fun boolNode(): Gen<Bool> = object : Gen<Bool> {
            override fun generate() = Bool(Gen.bool().generate())
        }

        fun numberNode(): Gen<Number> = object : Gen<Number> {
            override fun generate(): Number {
                val number = oneOf(listOf(Gen.int(), Gen.long(), Gen.float(), Gen.double())).generate().generate()
                return Node.Number(number as kotlin.Number)
            }
        }

        fun arrayNode(maxSize: Int = 3): Gen<Array> = object : Gen<Array> {
            override fun generate(): Array {
                val generator = oneOf(valueNodeGenerators).generate()
                return if (maxSize > 0) Array((0..RANDOM.nextInt(maxSize)).map { objectNode(0).generate() })
                else Array((0..RANDOM.nextInt(3)).map { generator.generate() })

            }
        }

        fun objectNode(maxDepth: Int = 2): Gen<ObjectNode> = object : Gen<ObjectNode> {
            override fun generate(): ObjectNode {
                val keysSize = RANDOM.nextInt(9) + 1
                val depthKey = RANDOM.nextInt(keysSize)
                return (0..keysSize)
                        .map { Gen.string().generate() }
                        .foldIndexed(ObjectNode()) { index, objectNode, key ->
                            if (depthKey == index && maxDepth > 0)
                                objectNode.with(objectNode(maxDepth - 1).generate(), at = key)
                            else
                                objectNode.with(valueNode().generate(), at = key)
                        }
            }
        }
    }
}
