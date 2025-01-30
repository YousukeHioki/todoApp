package com.example.todoapp

import com.example.todoapp.repository.DefaultTodoRepository
import com.example.todoapp.repository.TodoRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

class TodoRepositoryTest {

  private val tableName: String = "test"

  //全アイテム削除
  private fun deleteAllItems(tableName: String) {
    val request = ScanRequest.builder()
      .tableName(tableName)
      .build()
    //this.は省略可能
    val beforeResponse = client.scan(request)
    val beforeItems = beforeResponse.items().toList()

    for (item in beforeItems) {
      val deleteRequest = DeleteItemRequest.builder()
        .tableName(tableName)
        .key(mapOf("PK" to item["PK"])) // .key({ PK: item.PK })
        .build()
      client.deleteItem(deleteRequest)
    }
  }

  lateinit var todoRepository: TodoRepository;
  lateinit var client: DynamoDbClient

  @BeforeEach
  fun beforeEach() {
    var url = "http://localhost:4566"
    client = DynamoDbClient.builder()
      .endpointOverride(URI.create(url))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            "aaa",
            "aaa"
          )
        )
      )
      .region(Region.AP_NORTHEAST_1)
      .build()
    todoRepository = DefaultTodoRepository(
      url,
      tableName
    )
  }

  @AfterEach
  fun afterEach() {
    deleteAllItems(tableName)
  }

  @Test
  fun `全アイテム取得時に Hello! が値に入っているアイテムが返る`() {
    val uuid = UUID.randomUUID().toString()
    client.putItem(
      PutItemRequest.builder()
        .tableName(tableName)
        .item(
          mapOf(
            "PK" to fromS(uuid),
            "text" to fromS("Hello!")
          )
        )
        .build()
    )

    val result = todoRepository.getAllItems()

    assertEquals(
      listOf(
        TodoItem(
          uuid,
          "Hello!",
        )
      ),
      result
    )
  }

  @Test
  fun `PKでアイテムを取得した時に Hello! があたいに入っているアイテムを返す`() {
    val uuid = UUID.randomUUID().toString()
    client.putItem(
      PutItemRequest.builder()
        .tableName(tableName)
        .item(
          mapOf(
            "PK" to fromS(uuid),
            "text" to fromS("Hello!")
          )
        )
        .build()
    )

    val result = todoRepository.getTodoItemByPK(uuid)
    assertEquals(
      TodoItem(
        uuid,
        "Hello!",
      ),
      result
    )
  }

  @Test
  fun `アイテム追加時に付与される正しい PK(UUID) が返る`() {
    val uuid = UUID.fromString("b5b086ae-57e1-4d46-8eea-19471f75b101")
    val mockStatic = Mockito.mockStatic(UUID::class.java)
    mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)

    val resultPK = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )

    assertEquals(
      "b5b086ae-57e1-4d46-8eea-19471f75b101",
      resultPK
    )

    mockStatic.close()
  }

  @Test
  fun `アイテム追加時に 追加されたアイテム と Getしたアイテムが等しい`() {

    val resultPK = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )

    val resultResponse = client.getItem(
      GetItemRequest.builder()
        .tableName(tableName)
        .key(mapOf("PK" to fromS(resultPK)))
        .build()
    )
    assertEquals(
      mapOf(
        "PK" to fromS(resultPK),
        "text" to fromS("Hello!")
      ),
      resultResponse.item()
    )
  }

  @Test
  fun `更新前のtext と 更新後のtext が違っている`() {
//    val uuid = UUID.fromString("b5b086ae-57e1-4d46-8eea-19471f75b101")
//    val mockStatic = Mockito.mockStatic(UUID::class.java)
//    mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)

    val pk = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )
    val beforeResponse = client.getItem(
      GetItemRequest.builder()
        .tableName(tableName)
        .key(mapOf("PK" to fromS(pk)))
        .build()
    ).item()
    val beforeText = beforeResponse["text"]!!.s()
    println("beforeText----------$beforeText")

    todoRepository.updateTodoItem(pk, TodoRequest("Hi!!"))

    val afterResponse = client.getItem(
      GetItemRequest.builder()
        .tableName(tableName)
        .key(mapOf("PK" to fromS(pk)))
        .build()
    ).item()
    val afterText = afterResponse["text"]!!.s()
    println("afterText----------$afterText")

    assertNotEquals(beforeText, afterText)

  }

  @Test
  fun `アイテムの削除に成功した時に true を返す`() {

    val PK = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )

    val deleteItemResponse = todoRepository.deleteItemByPK(PK)

    assertEquals(deleteItemResponse, true)

  }


  @Test
  fun `削除したアイテムを検索して存在しなければ null を返す`() {

    val PK = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )

    val deleteItemResponse = todoRepository.deleteItemByPK(PK)

    val getItemResponse = todoRepository.getTodoItemByPK(PK)

    assertNull(getItemResponse)

  }

  @Test
  fun `削除したアイテムを検索して存在しなければ true 存在していれば false を返す`() {

    val PK = todoRepository.addNewItem(
      TodoRequest("Hello!")
    )

    val deleteItemResponse = todoRepository.deleteItemByPK(PK)

    val getItemResponse = todoRepository.getTodoItemByPK(PK)

    if (getItemResponse == null) {
      assertEquals(deleteItemResponse, true)
    } else {
      assertEquals(deleteItemResponse, false)
    }

  }

}
