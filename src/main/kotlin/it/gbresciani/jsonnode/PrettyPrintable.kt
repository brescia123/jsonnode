package it.gbresciani.jsonnode

interface PrettyPrintable {
    fun print(indentation: String = "  "): String
}