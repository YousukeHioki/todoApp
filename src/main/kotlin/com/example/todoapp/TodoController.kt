package com.example.todoapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

data class Todo (
    var id: String,
    var todo: String,
    var isFinished: Boolean
)

val todoRepository = listOf(
    Todo(
        id = "12345",
        todo = "studyAboutSQL",
        isFinished = false
    ),
    Todo(
        id = "67890",
        todo = "studyAboutDB",
        isFinished = true
    )
)


@RestController
class TodoController {

    @PostMapping("/todo")
    fun addTodoItem(): String {
        return "OK"
    }

    @GetMapping("/todo/{id}")
    fun getTodoById(@PathVariable id: String): Boolean {
        //!!!kotlinでは対象のデータをitで表す
        //!!!kotlinでは===は参照の等価性を、==はオブジェクトの内容の等価性をチェックする
        val todoItem = todoRepository.find { it.id == id }
        //printlnで改行して次を表示、printのみでは横並びで繋がって表示され見にくくなる
        println("todoRepository全て表示↓")
        println(todoRepository)
        println("todoRepositoryの一つ目のみ表示↓")
        println(todoRepository[0])
        println("todoItemを表示↓")
        println(todoItem)
        println("todoItem!!.isFinishedを表示↓")
        //TODO：!!はnullが入らないようにするっぽい、?の場合は何か調べる
        println(todoItem!!.isFinished)


        return todoItem.isFinished
    }

}