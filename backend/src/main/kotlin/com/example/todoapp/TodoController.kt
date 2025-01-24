package com.example.todoapp

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

class TodoRequest {
    var  text: String = ""
}
class TodoItem {
    //ここでjacksonなんとかすると小文字でpkが返るのを防げるが一旦置き
    var PK: String = ""
    var text: String = ""
    //printした時にデータをちゃんと表示させるためだけの処理
    override fun toString(): String {
        return "TodoItem(PK='$PK', text='$text')"
    }
}

@RestController
class TodoController {

private val client = DynamoDbClient.builder()
    .endpointOverride(URI.create("http://localhost:4566"))
    .credentialsProvider(AnonymousCredentialsProvider.create())
    .region(Region.AP_NORTHEAST_1)
    .build()

fun scanAllItems(tableName: String): List<Map<String, AttributeValue>>{
    val request = ScanRequest.builder()
        .tableName("test")
        .build()
    val response = client.scan(request)
    val items = response.items().toList()
    return items
}
    //⭐️GET METHOD------------------------

    @GetMapping("/todo")
    fun getAllItems(): List<TodoItem> {
        val client = DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4566")) //テスト時などに特定のEPを指定する
//          →参考---https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/region-selection.html
            .credentialsProvider(AnonymousCredentialsProvider.create()) // 匿名認証のため必要
//          →参考---https://docs.aws.amazon.com/ja_jp/sdk-for-java/latest/developer-guide/client-creation-defaults.html
            .region(Region.AP_NORTHEAST_1) //利用位置から近い地域を設定
            .build()
        val request = ScanRequest.builder()
            .tableName("test")
            .build()
        val response = client.scan(request)
        val items = response.items().toList()
        println("items--------$items")
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
    fun getTodoItemByPK(@PathVariable PK: String): ResponseEntity<TodoItem>{
        val allTodoItems = getAllItems()
        val todoItem = allTodoItems.find{it.PK == PK}
        if(todoItem != null){
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
        val item = mapOf(
            //ランダムなUUIDをPKに入れる、この場合は.toString()が必要
            "PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
            "text" to AttributeValue.fromS(todo.text)
        )
        //アイテムを追加するリクエスト
        val putItemRequest = PutItemRequest.builder()
            .tableName("test")
            .item(item)
            .build()
        client.putItem(putItemRequest)
        //追加された状態のテーブルデータを取得
        val scanItemRequest = ScanRequest.builder()
            .tableName("test")
            .build()
        val response = client.scan(scanItemRequest)
        val items = response.items().toList()
        val PK = items[items.size - 1]["PK"]?.s() ?: ""
        return PK
    }

    //⭐️PUT METHOD-------------------------



    //⭐️DELETE METHOD----------------------

    @DeleteMapping("/todo/{PK}")
    fun deleteItem(@PathVariable PK: String): String{
        //削除実行
        val deleteItemRequest = DeleteItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(PK).build()))
            .build()
        client.deleteItem(deleteItemRequest)
        //確認
        val result = getTodoItemByPK(PK)
        println("deletedItem=$result")
        if(result.statusCode == HttpStatus.NOT_FOUND){
            return HttpStatus.OK.toString()
        } else {
            return HttpStatus.NOT_FOUND.toString()
        }
    }


}