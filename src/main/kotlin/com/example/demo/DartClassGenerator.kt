package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstNode
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.common.klass.KlassIdentifier
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import java.io.File
import java.util.*

class DartClassGenerator {
    companion object {
        private fun process(ast: Ast, nodesToProcessStack: Stack<NodeWrapper>) {
            when (ast) {
                is KlassDeclaration -> {
//                println("key ${ast.keyword}")
                    if (ast.keyword == "class") {
                        println("class: ${ast.identifier?.rawName}")
                        nodesToProcessStack.push(
                            NodeWrapper(
                                fieldName = ast.identifier?.rawName,
                                type = ast.keyword
                            )
                        )
                    }
                    if (ast.keyword == "val" || ast.keyword == "var") {
                        val lastNode = nodesToProcessStack.peek()
                        val items = Stack<String>()
                        ast.type.forEach {
                            processIdentifier(it, items)
                        }
                        var type = ""
                        while (items.isNotEmpty()) {
                            type = "<${items.pop()}$type>"
                        }

                        if (type.contains("<"))
                            type = type.removePrefix("<")
                        if (type.contains(">"))
                            type = type.removeSuffix(">")

//                    println("val: ${ast.identifier?.rawName}, $type")

                        println("----------------")
                        val astJsonFromMapper = ObjectMapper().writeValueAsString(ast)
                        println("astJsonFromMapper: $astJsonFromMapper")
                        println("----------------")
                        lastNode.children.add(NodeWrapper(fieldName = ast.identifier?.rawName, type = type))
                    }
                    ast.children.forEach {
                        process(it, nodesToProcessStack)
                    }
                }

                is AstNode -> {
                    ast.children.forEach {
                        process(it, nodesToProcessStack)
                    }
                }
            }
        }

        private fun processIdentifier(astIdentifier: KlassIdentifier, items: Stack<String>) {
            items.push(getType(astIdentifier.identifier))
            astIdentifier.parameter.forEach {
                processIdentifier(it, items)
            }
        }

        fun generateFromJson(json: String, fileToWriteOn: File, fileName: String) {
            val nodesToProcessStack = Stack<NodeWrapper>()
            val astSource = AstSource.String(description = "json", content = json)
            val ff = KotlinGrammarAntlrKotlinParser.parseKotlinFile(
                astSource
            )
            ff.summary(attachRawAst = false).onSuccess { astList ->
                astList.forEach {
                    process(it, nodesToProcessStack)
                }

            }.onFailure { errors ->
                errors.forEach(::println)
            }

            val ogFile = File(fileToWriteOn, "$fileName.dart")
            val sb = StringBuilder()

            while (nodesToProcessStack.isNotEmpty()) {
                while (nodesToProcessStack.isNotEmpty()) {
                    val node = nodesToProcessStack.pop()

                    val fields = mutableListOf<String?>()
                    val fromJsonBuilder = createConstructorStart(node)
                    val toJsonBuilder = createSerializerStart()

                    sb.appendLine("class ${node.fieldName} {")
                    node.children.forEach {
                        fields.add("this.${it.fieldName}")
                        sb.appendLine("late ${it.type} ${it.fieldName};")
                        fromJsonBuilder.appendLine("\tthis.${it.fieldName} = map['${it.fieldName}'];")
                        toJsonBuilder.appendLine("\t'${it.fieldName}':${it.fieldName},")
                    }
                    fromJsonBuilder.append("\n}")
                    toJsonBuilder.append("\t\n};")
                    toJsonBuilder.append("\n}")
                    sb.appendLine("${node.fieldName}(${fields.joinToString(", ")});")
                    sb.appendLine("$fromJsonBuilder")
                    sb.appendLine("$toJsonBuilder")
                    sb.appendLine("}")
                    fields.clear()
                }
            }
            ogFile.writeText(sb.toString())
        }


        private fun extractPackageName(dir: File): String {
            val absolutePath = dir.absolutePath

            val splitted = absolutePath.split("\\")
            println("absolutePath: $absolutePath")
            val libIndex = splitted.indexOf("lib")
            if (libIndex == -1) {
                throw IllegalStateException("lib not found")
            }
            val fold = splitted
                .subList(libIndex + 1, splitted.size)
                .fold(StringBuilder()) { builder, s -> builder.append(s).append("/") }
            return "package:${splitted[libIndex - 1]}/$fold"
        }

        private fun createConstructorStart(nodeWrapper: NodeWrapper) =
            StringBuilder()
                .appendLine("\n${nodeWrapper.fieldName}.fromJsonMap(Map<String, dynamic> map){ ")


        private fun createSerializerStart() =
            StringBuilder()
                .appendLine("Map<String, dynamic> toJson() {")
                .appendLine("\treturn {")

        private fun getType(ogType: String): String = when (ogType) {
            "Int" -> "int"
            "Boolean" -> "bool"
            "Array" -> "List"
            "Float", "Double" -> "double"
            "Any" -> "dynamic"
            else -> ogType
        }
    }
}
