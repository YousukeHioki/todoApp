package com.example.todoapp

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TodoController {

    @PostMapping("/todo")
    fun addTodoItem(): String {
        return "OK"
    }
}