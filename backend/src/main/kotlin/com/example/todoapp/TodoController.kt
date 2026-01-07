package com.example.todoapp

import com.example.todoapp.repository.TodoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class TodoRequest(
  var text: String = ""
)


data class TodoItem(
  var PK: String = "",
  var text: String = "",
  var completed: Boolean = false
)


@RestController
class TodoController(val todoRepository: TodoRepository) {

  @GetMapping("/todo")
  fun getAllItems(): ResponseEntity<List<TodoItem>> {
    return ResponseEntity.ok(todoRepository.getAllItems())
  }

  @GetMapping("/todo/{PK}")
  fun getTodoItemByPK(@PathVariable PK: String): ResponseEntity<TodoItem?> {
    val todoOrNull = todoRepository.getTodoItemByPK(PK)
    return if (todoOrNull!= null) ResponseEntity.ok(todoOrNull)
           else throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }


  //⭐️POST METHOD------------------------

  @PostMapping("/todo")
  fun addNewItem(@RequestBody todo: TodoRequest): ResponseEntity<String?> {
    val pk = todoRepository.addNewItem(todo)
    return ResponseEntity.ok(pk)
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
  ): ResponseEntity<TodoItem> {
    val response = todoRepository.updateTodoItem(PK, todo)
    val updatedItem = TodoItem(PK, todo.text, response?.completed ?: false);
    return ResponseEntity.ok(updatedItem)
  }

  //⭐️PATCH METHOD for toggling completion status
  @PatchMapping("/todo/{PK}/complete")
  fun toggleTodoCompletion(@PathVariable PK: String): ResponseEntity<TodoItem> {
    val currentItem = todoRepository.getTodoItemByPK(PK)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo item not found")
    
    val updatedItem = todoRepository.toggleCompletion(PK, !currentItem.completed)
      ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update completion status")
    
    return ResponseEntity.ok(updatedItem)
  }

  //⭐️DELETE METHOD----------------------

  @DeleteMapping("/todo/{PK}")
  fun deleteItem(@PathVariable PK: String): ResponseEntity<String> {
    val response = todoRepository.deleteItemByPK(PK)

    return if(response) {
      ResponseEntity.status(HttpStatus.OK).build()
    } else {
      ResponseEntity.notFound().build()
    }
  }


}
