package com.example.todoapp.repository

import com.example.todoapp.TodoItem
import com.example.todoapp.TodoRequest
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import java.net.URI
import java.util.*

interface TodoRepository {
    fun getAllItems(): List<TodoItem>
    fun getTodoItemByPK(PK: String): TodoItem?
    fun addNewItem(todoRequest: TodoRequest): String
    fun updateTodoItem(PK: String, todo: TodoRequest): TodoItem?
    fun deleteItemByPK(PK: String): Boolean
}

@Repository
class DefaultTodoRepository(
    //application.ymlファイルから取得するため@Valueが必要
    @Value("\${aws.dynamodb-url}") dynamoDbUrl: String,
    @Value("\${aws.table-name}") val tableName: String
) : TodoRepository {

    val client: DynamoDbClient
//    val newPK = UUID.randomUUID().toString()


    init {
        val builder = DynamoDbClient.builder()
            .region(Region.AP_NORTHEAST_1)

        if (dynamoDbUrl != "default") {
            builder.endpointOverride(URI.create(dynamoDbUrl))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("aaa", "aaa")))
        }

        client = builder.build()
    }
    override fun getAllItems(): List<TodoItem> {
        val items = client.scan(
            ScanRequest.builder()
                .tableName(tableName)
                .build()
        ).items()
        return items.map {
            TodoItem(
                PK = it["PK"]!!.s(),
                text = it["text"]!!.s(),
            )
        }
    }

    override fun getTodoItemByPK(PK: String): TodoItem? {
        val response = client.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        "PK" to fromS(PK)
                    )
                )
                .build()
        )

        return if (response.hasItem()) {
            TodoItem(response.item()["PK"]!!.s(), response.item()["text"]!!.s())
        } else {
            return null
        }
    }
    override fun addNewItem(todoRequest: TodoRequest): String {
        //TODO:これをprivate valにするとテストが通らない、なぜ？確認
        val newPK = UUID.randomUUID().toString()
        val item = mapOf(
            "PK" to fromS(newPK),
            "text" to fromS(todoRequest.text)
        )

        client.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    item
                ).build()
        )
        return newPK
    }

    override fun updateTodoItem(PK: String, todo: TodoRequest): TodoItem? {
        //追加したいアイテム
        val item = mapOf(
            //ランダムなUUIDをPKに入れる
            "PK" to fromS(PK),
            "text" to fromS(todo.text)
        )
        //アイテムを追加するリクエスト
        val putItemRequest = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build()
        client.putItem(putItemRequest)

			val updatedItem = getTodoItemByPK(PK)

			return if(updatedItem != null) {
        TodoItem(updatedItem.PK, updatedItem.text)
			} else {
				null
			}
        //追加された状態のテーブルデータを取得
        //!!!この返り値PKはUUIDのため、新しく保存されたものが下に追加されるとは限らず正しく返せない
        //→データが登録順などで並ぶ場合には有効な記述
//        val scanItemRequest = ScanRequest.builder()
//            .tableName("test")
//            .build()
//        val response = client.scan(scanItemRequest)
//        val items = response.items().toList()
//        val PK = items[items.size - 1]["PK"]?.s() ?: ""
//        return PK
    }

    override fun deleteItemByPK(PK: String): Boolean {
        val deleteItemRequest = DeleteItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(PK).build()))
            .build()
        client.deleteItem(deleteItemRequest)
        //確認
        val deletedItem = getTodoItemByPK(PK)
        println("deletedItem--------$deletedItem")
        return deletedItem == null
    }
}

class TodoRepositoryImpl {


    private val client = DynamoDbClient.builder()
        .endpointOverride(URI.create("http://localhost:4566")) //テスト時などに特定のEPを指定する
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("aaa", "aaa")))
        .region(Region.AP_NORTHEAST_1) //利用位置から近い地域を設定
        .build()

    //⭐️GET METHOD------------------------

    @GetMapping("/todo")
    fun getAllItems(): List<TodoItem> {

        val request = ScanRequest.builder()
            .tableName("test")
            .build()
        val response = client.scan(request)
        val items = response.items().toList()
        val resultItems = mutableListOf<TodoItem>()
        items.forEach { item ->
            val resultPK = item["PK"]?.s() ?: ""
            val resultText = item["text"]?.s() ?: ""
            //TodoItemを初期化し新しいTodoItemを作成
            val resultItem = TodoItem()
            resultItem.PK = resultPK
            resultItem.text = resultText
            resultItems.add(resultItem)
        }
        return resultItems
    }

    @GetMapping("/todo/{PK}")
    //ResponseEntityで
    fun getTodoItemByPK(@PathVariable PK: String): TodoItem? {
        val allTodoItems = getAllItems()
        val todoItem = allTodoItems.find { it.PK == PK }
        println("todoItem-----$todoItem")
        return todoItem
    }


    //⭐️POST METHOD------------------------

    @PostMapping("/todo")
    fun addNewItem(@RequestBody todo: TodoRequest): String {
        //追加したいアイテム
        val newPK = UUID.randomUUID().toString()
        val item = mapOf(
            //ランダムなUUIDをPKに入れる、この場合は.toString()が必要
            "PK" to fromS(newPK),
            "text" to fromS(todo.text)
        )
        //アイテムを追加するリクエスト
        val putItemRequest = PutItemRequest.builder()
            .tableName("test")
            .item(item)
            .build()
        client.putItem(putItemRequest)
        return newPK
        //追加された状態のテーブルデータを取得
        //!!!この返り値PKはUUIDのため、新しく保存されたものが下に追加されるとは限らず正しく返せない
        //→データが登録順などで並ぶ場合には有効な記述
//        val scanItemRequest = ScanRequest.builder()
//            .tableName("test")
//            .build()
//        val response = client.scan(scanItemRequest)
//        val items = response.items().toList()
//        val PK = items[items.size - 1]["PK"]?.s() ?: ""
//        return PK
    }

    //⭐️PUT METHOD-------------------------

    //記述した特定の列の値のみしか変更できない
    @PutMapping("/todo/{PK}")
    fun updateTodoItem(@PathVariable PK: String, @RequestBody todo: TodoRequest): TodoItem? {
        val beforeItem = getTodoItemByPK(PK)
        val beforeText = beforeItem?.text
        println("beforeText=--------------$beforeText")
//        更新実行
        val updateItemRequest = UpdateItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(PK).build()))
            .attributeUpdates(
                mapOf(
                    "text" to AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(todo.text).build())
                        .action(AttributeAction.PUT)
                        .build(),
                )
            )
            .build()
        client.updateItem(updateItemRequest)
        //確認
        val afterItem = getTodoItemByPK(PK)
        val afterText = afterItem?.text
        println("afterText=--------------$afterText")
        return if (beforeText != afterText) {
            TodoItem(afterItem!!.PK, afterItem.text)
        } else {
            null
        }
    }


    //⭐️DELETE METHOD----------------------

    @DeleteMapping("/todo/{PK}")
    fun deleteItem(@PathVariable PK: String): String {
        //削除実行
        val deleteItemRequest = DeleteItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(PK).build()))
            .build()
        client.deleteItem(deleteItemRequest)
        //確認
        val deletedItem = getTodoItemByPK(PK)
        println("deletedItem-------$deletedItem")
        return if (deletedItem != null) {
            HttpStatus.OK.toString()
        } else {
            HttpStatus.NOT_FOUND.toString()
        }
    }
}
