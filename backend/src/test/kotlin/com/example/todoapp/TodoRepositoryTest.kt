package com.example.todoapp

import com.example.todoapp.repository.DefaultTodoRepository
import com.example.todoapp.repository.TodoRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
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
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()
        todoRepository = DefaultTodoRepository(
            url,
            "test"
        )
    }

    @AfterEach
    fun afterEach() {
        deleteAllItems("test")
    }

    @Test
    fun getAllItems() {
        val uuid = UUID.randomUUID().toString()
        client.putItem(
            PutItemRequest.builder()
                .tableName("test")
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
    fun addNewItem() {
        val uuid = UUID.fromString("b5b086ae-57e1-4d46-8eea-19471f75b101")
        val mockStatic = Mockito.mockStatic(UUID::class.java)
        mockStatic.`when`<UUID> {UUID.randomUUID()}.thenReturn(uuid)

        val resultPK = todoRepository.addNewItem(
            TodoRequest("Hello!")
        )

        assertEquals(
            "b5b086ae-57e1-4d46-8eea-19471f75b101",
            resultPK
        )

        val resultResponse = client.getItem(
            GetItemRequest.builder()
                .tableName("test")
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
        mockStatic.close()
    }

}
