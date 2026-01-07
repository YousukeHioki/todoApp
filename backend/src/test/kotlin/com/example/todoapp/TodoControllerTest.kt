package com.example.todoapp

import com.example.todoapp.repository.DefaultTodoRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import com.example.todoapp.repository.TodoRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
      listOf(TodoItem("1234567890", "Hello!", false))
    )

    mockMvc.perform(get("/todo"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].pk").value("1234567890"))
      .andExpect(jsonPath("$[0].text").value("Hello!"))
      .andExpect(jsonPath("$[0].completed").value(false))
  }

  @Test
  fun addNewItem() {
    Mockito.`when`(mockTodoRepository.addNewItem(any()))
      .thenReturn("1234567890")

    mockMvc.perform(
      post("/todo").contentType("application/json")
        .content("""{"text": "Hello!"}""".trimIndent()
        )
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$").value("1234567890"))

    verify(mockTodoRepository)
      .addNewItem(TodoRequest("Hello!"))
  }

  @Test
  fun getTodoItemByPK() {
    Mockito.`when`(mockTodoRepository.addNewItem(TodoRequest("Hey!")))
      .thenReturn("1234567890")

    Mockito.`when`(mockTodoRepository.getTodoItemByPK("1234567890"))
      .thenReturn(TodoItem("1234567890", "Hey!", false))

    mockMvc.perform(get("/todo/1234567890"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.pk").value("1234567890"))
      .andExpect(jsonPath("$.text").value("Hey!"))
      .andExpect(jsonPath("$.completed").value(false))
  }

  @Test
  fun updateTodoItem() {
    val updateTodo = TodoRequest("Hi!")

    Mockito.`when`(mockTodoRepository.updateTodoItem("1234567890", updateTodo))
      .thenReturn(TodoItem("1234567890", "Hi", false))

    mockMvc.perform(
      put("/todo/1234567890").contentType("application/json")
        .content(
          """
                 {"text": "Hi!"}
             """.trimIndent()
        )
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.pk").value("1234567890"))
      .andExpect(jsonPath("$.text").value("Hi!"))

  }

  @Test
  fun `削除が成功した時にステータス OK が返る`() {
    Mockito.`when`(mockTodoRepository.deleteItemByPK(any()))
      .thenReturn(true)

    mockMvc.perform(delete("/todo/1234567890"))
      .andExpect(status().isOk)
  }

  @Test
  fun `削除する項目が存在しない場合は 404 エラーを返す`() {
    Mockito.`when`(mockTodoRepository.deleteItemByPK(any()))
      .thenReturn(false)

    mockMvc.perform(delete("/todo/1234567890"))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `完了ステータスをトグルできる`() {
    val currentItem = TodoItem("1234567890", "Test task", false)
    val updatedItem = TodoItem("1234567890", "Test task", true)
    
    Mockito.`when`(mockTodoRepository.getTodoItemByPK("1234567890"))
      .thenReturn(currentItem)
    
    Mockito.`when`(mockTodoRepository.toggleCompletion("1234567890", true))
      .thenReturn(updatedItem)
    
    mockMvc.perform(patch("/todo/1234567890/complete"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.pk").value("1234567890"))
      .andExpect(jsonPath("$.text").value("Test task"))
      .andExpect(jsonPath("$.completed").value(true))
  }

  @Test
  fun `存在しないタスクの完了ステータスをトグルしようとすると 404 エラーを返す`() {
    Mockito.`when`(mockTodoRepository.getTodoItemByPK("nonexistent"))
      .thenReturn(null)
    
    mockMvc.perform(patch("/todo/nonexistent/complete"))
      .andExpect(status().isNotFound)
  }

}
