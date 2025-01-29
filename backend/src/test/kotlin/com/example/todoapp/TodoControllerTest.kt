package com.example.todoapp

import com.example.todoapp.repository.DefaultTodoRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import com.example.todoapp.repository.TodoRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

//import org.junit.jupiter.api.Assertions.*

 class TodoControllerTest {

     lateinit var mockTodoRepository: TodoRepository
     lateinit var mockMvc: MockMvc
     lateinit var todo: TodoRequest

     @BeforeEach
     fun setUp() {
         mockTodoRepository = Mockito.mock(TodoRepository::class.java)
         mockMvc = MockMvcBuilders
             .standaloneSetup(TodoController(mockTodoRepository))
             .build()
     }

     @Test
     fun getAllItems() {
//         mockTodoRepositoryのgetAllItemsを呼ぶ時
         Mockito.`when`(
             mockTodoRepository.getAllItems()
//             listOf(TodoItem)を返して欲しい
         ).thenReturn(
             listOf(TodoItem("1234567890", "Hello!"))
         )

         mockMvc.perform(get("/todo"))
         .andExpect(status().isOk)
             .andExpect(jsonPath("$[0].pk").value("1234567890"))
             .andExpect(jsonPath("$[0].text").value("Hello!"))
     }

     @Test
     fun addNewItem() {
         Mockito.`when`(mockTodoRepository.addNewItem(any()))
             .thenReturn("1234567890")

         mockMvc.perform(post("/todo").contentType("application/json")
             .content("""
                 {"text": "Hello!"}
             """.trimIndent())
         )
         .andExpect(status().isOk)
             .andExpect(jsonPath("$").value("1234567890"))

         verify(mockTodoRepository)
             .addNewItem(TodoRequest("Hello!"))
     }

     @Test
     fun getTodoItemByPK(){
         Mockito.`when`(mockTodoRepository.getTodoItemByPK("1234567890"))
//             .thenReturn(ResponseEntity(
//                 TodoItem("1234567890", "Hello!")
//             )
//         )
     }

     @Test
     fun updateTodoItem(){
         val updateTodo = TodoRequest("Hi!")

         Mockito.`when`(mockTodoRepository.updateTodoItem("1234567890", updateTodo))
             .thenReturn(ResponseEntity(HttpStatus.OK))
         mockMvc.perform(
             put("/todo/1234567890").contentType("application/json")
             .content("""
                 {"text": "Hi!"}
             """.trimIndent())
         )
             .andExpect(status().isOk)

//         val updatedTodoItem = mockMvc.perform(get("/todo/1234567890"))
//         println("updatedItem------$updatedTodoItem")
//        mockMvc.perform(get("/todo/1234567890"))
//             .andExpect(jsonPath("$.text").value("Hi!"))

//         verify(mockTodoRepository, times(1))
//             .updateTodoItem("1234567890", TodoRequest("Hi!"))

     }

     @Test
     fun deleteItemByPK(){
         Mockito.`when`(mockTodoRepository.deleteItemByPK("1234567890"))
         .thenReturn(ResponseEntity(HttpStatus.OK))

         mockMvc.perform(
             delete("/todo/1234567890")
         ).andExpect(status().isOk)

//         val deletedItem = mockMvc.perform(get("/todo/1234567890"))
//             .andReturn().response.contentAsString
//         println("deleted------ $deletedItem")
     }


 }
