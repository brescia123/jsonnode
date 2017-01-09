package it.gbresciani.jsonnode

import io.kotlintest.properties.Gen
import io.kotlintest.specs.ShouldSpec
import it.gbresciani.jsonnode.Node.Bool
import it.gbresciani.jsonnode.Node.Text


class ExtensionsKtTest : ShouldSpec() {
    init {
        "Any.asNode" {
            should("create a Null node if called on null value") {
                null.asNode() shouldBe Node.Null
            }

            should("return the same Node if called on a Node") {
                //TODO
            }

            should("create a Text node with the right value if called on a String") {
                forAll(Gen.string()) { string ->
                    val node = string.asNode()
                    node is Text && node.text == string
                }
            }

            should("create a Bool node with the right value if called on a Boolean") {
                forAll(Gen.bool()) { bool ->
                    val node = bool.asNode()
                    node is Bool && node.bool == bool
                }
            }
        }

        "Node.at" {
            should("create the an Object node with the given value at the given key") {
                val objectNode = 3.asNode() at "key"
                objectNode["key"] shouldBe Node.Number(3)
            }
        }
    }
}
