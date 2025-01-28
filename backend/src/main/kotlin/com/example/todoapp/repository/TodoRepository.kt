package com.example.todoapp.repository

import com.example.todoapp.TodoItem
import com.example.todoapp.TodoRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Value
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import java.net.URI
import java.util.*

interface TodoRepository {
    fun getAllItems(): List<TodoItem>
    fun addNewItem(todoRequest: TodoRequest): String
    fun getTodoItemByPK(PK: String): ResponseEntity<TodoItem>
    fun updateTodoItem(PK: String, todo: TodoRequest): ResponseEntity<String>
    fun deleteItemByPK(PK: String): ResponseEntity<String>
}

@Repository
class DefaultTodoRepository(
    //application.ymlファイルから取得するため@Valueが必要
    @Value("\${aws.dynamodb-url}") dynamoDbUrl: String,
    @Value("\${aws.table-name}") val tableName: String
): TodoRepository{

    val client: DynamoDbClient

    init {
        client = DynamoDbClient.builder()
            .endpointOverride(URI.create(dynamoDbUrl))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()
    }

    override fun getAllItems(): List<TodoItem> {
        val items = client.scan(
            ScanRequest.builder()
            .tableName(tableName)
            .build()
        ).items()
        return items.map{
            TodoItem(
                PK = it["PK"]!!.s(),
                text = it["text"]!!.s(),
            )
        }
    }

    override fun addNewItem(todoRequest: TodoRequest): String {
        val newPK = UUID.randomUUID().toString()
        client.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        "PK" to AttributeValue.fromS(newPK),
                        "text" to AttributeValue.fromS(todoRequest.text)
                    )
                ).build()
        )
        return newPK
    }

    override fun getTodoItemByPK(PK: String): ResponseEntity<TodoItem> {
        TODO("Not yet implemented")
    }

    override fun updateTodoItem(PK: String, todo: TodoRequest): ResponseEntity<String> {
        TODO("Not yet implemented")
    }

    override fun deleteItemByPK(PK: String): ResponseEntity<String> {
        TODO("Not yet implemented")
    }
}

class TodoRepositoryImpl {


    private val client = DynamoDbClient.builder()
        .endpointOverride(URI.create("http://localhost:4566")) //テスト時などに特定のEPを指定する
        .credentialsProvider(AnonymousCredentialsProvider.create()) // 匿名認証のため必要
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
    fun getTodoItemByPK(@PathVariable PK: String): ResponseEntity<TodoItem> {
        val allTodoItems = getAllItems()
        val todoItem = allTodoItems.find { it.PK == PK }
        if (todoItem != null) {
            return ResponseEntity(todoItem, HttpStatus.OK)
        } else {
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        }
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
        //追加したいアイテム
        val newPK = UUID.randomUUID().toString()
        val item = mapOf(
            //ランダムなUUIDをPKに入れる、この場合は.toString()が必要
            "PK" to AttributeValue.fromS(newPK),
            "text" to AttributeValue.fromS(todo.text)
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
    fun updateTodoItem(@PathVariable PK: String, @RequestBody todo: TodoRequest): ResponseEntity<String> {
        val beforeItem = getTodoItemByPK(PK)
        val beforeText = beforeItem.body?.text
        println("beforeText=--------------$beforeText")
        //更新実行
        val updateItemRequest = UpdateItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(PK).build()))
            .attributeUpdates(
                mapOf(
                    "text" to AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(todo.text).build())
                        .action(AttributeAction.PUT)
                        .build(),
                    //複数列ある場合は上記のコードを追加する
//                "他の列名" to AttributeValueUpdate.builder()
//                    .value(AttributeValue.builder().s(todo.他の列名).build())
//                    .action(AttributeAction.PUT)
//                    .build()
                )
            )
            .build()
        client.updateItem(updateItemRequest)
        //確認
        val afterItem = getTodoItemByPK(PK)
        val afterText = afterItem.body?.text
        println("afterText=--------------$afterText")
        if (beforeText != afterText) {
            return ResponseEntity("Update completed", HttpStatus.OK)
        } else {
            return ResponseEntity("Could not update", HttpStatus.INTERNAL_SERVER_ERROR)
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
        val result = getTodoItemByPK(PK)
        println("deletedItem=$result")
        if (result.statusCode == HttpStatus.NOT_FOUND) {
            return HttpStatus.OK.toString()
        } else {
            return HttpStatus.NOT_FOUND.toString()
        }
    }
}
