package com.example.todoapp

import com.example.todoapp.repository.TodoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI
import java.util.*

data class TodoRequest (
    var text: String = ""
)


data class TodoItem (

    var PK: String = "",
    var text: String = ""
)


@RestController
class TodoController(val todoRepository: TodoRepository) {

    @GetMapping("/todo")
    fun getAllItems(): List<TodoItem> {
        return todoRepository.getAllItems()
    }

    @GetMapping("/todo/{PK}")
    fun getTodoItemByPK(@PathVariable PK: String): ResponseEntity<TodoItem> {
        return ResponseEntity(HttpStatus.OK)
    }
//        この書き方の時はdata classにする必要がある
//        val todoItems = items.map{
//            TodoItem(
//                PK = it["PK"]?.s() ?: "",
//                text = it["text"]?.s() ?: ""
//            )
//        }


    //⭐️POST METHOD------------------------

    @PostMapping("/todo")
    fun addNewItem(@RequestBody todo: TodoRequest): String {
        val pk = todoRepository.addNewItem(todo)
        return pk
        //追加された状態のテーブルデータを取得
        //!!!この返り値PKはUUIDのため、新しく保存されたものが下に追加されるとは限らず正しく返せない
//        val scanItemRequest = ScanRequest.builder()
//            .tableName("test")
//            .build()
//        val response = client.scan(scanItemRequest)
//        val items = response.items().toList()
//        val PK = items[items.size - 1]["PK"]?.s() ?: ""
//        return PK
    }

    //⭐️PUT METHOD-------------------------

    //特定の列の値のみしか
    @PutMapping("/todo/{PK}")
    fun updateTodoItem(
        @PathVariable PK: String,
        @RequestBody todo: TodoRequest
    ): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.OK)
    }

//    💡カギさんコード
//    fun updateTodoItem(updatedItem: TodoItem) {
//        val todoKey = Key.builder()
//            .partitionValue(updatedItem.id.toString())
//            .build()
//        if (todoItemDynamoDbTable.getItem(todoKey) != null) {
//            todoItemDynamoDbTable.updateItem(updatedItem)
//        } else {
//            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found")
//        }
//    }


    //⭐️DELETE METHOD----------------------

    @DeleteMapping("/todo/{PK}")
    fun deleteItem(@PathVariable PK: String): String {
        return ""
    }


}
