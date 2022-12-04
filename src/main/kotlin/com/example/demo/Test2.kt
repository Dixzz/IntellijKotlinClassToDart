package com.example.demo

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder

class Test2 : AnAction("Kotlin Data Class to Dart") {
    val messageDelegate: MessageDelegate = MessageDelegate()

    override fun actionPerformed(e: AnActionEvent) {

        DialogBuilder().apply {
            val form = Json2DartForm()
            form.setOnGenerateListener(object : Json2DartForm.OnGenerateClicked {
                override fun onClicked(fileName: String?, json: String?) {
                    println("fileName: $fileName")
                    json?.let { j ->
                        GeneratorDelegate.runGeneration(
                            e,
                            fileName ?: "",
                            j ,
                        )
                    } ?: messageDelegate.showMessage("Json is empty")

                    window.dispose()
                }
            })
            setCenterPanel(form.rootView)
            setTitle("Json2Dart")
            removeAllActions()
            show()
        }
    }
}

