package com.example.todoapp

import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

class TodoRequest {
    var  text: String = ""
}
class TodoItem {
    var PK: String = ""
    var text: String = ""
    //printした時にデータをちゃんと表示させるためだけの処理
    override fun toString(): String {
        return "TodoItem(PK='$PK', text='$text')"
    }
}


//data class Todo (
//    var id: String,
//    var todo: String,
//    var isFinished: Boolean
//)
//
//val todoRepository = listOf(
//    Todo(
//        id = "12345",
//        todo = "studyAboutSQL",
//        isFinished = false
//    ),
//    Todo(
//        id = "67890",
//        todo = "studyAboutDB",
//        isFinished = true
//    )
//)

@RestController
class TodoController {

    //⭐️GET METHOD------------------------

//    @GetMapping("/todo/{id}")
//    fun getTodoById(@PathVariable id: String): Boolean {
//        //!!!kotlinでは対象のデータをitで表す
//        //!!!kotlinでは===は参照の等価性を、==はオブジェクトの内容の等価性をチェックする
//        val todoItem = todoRepository.find { it.id == id }
//        //printlnで改行して次を表示、printのみでは横並びで繋がって表示され見にくくなる
//        print("todoRepositoryを表示-------")
//        println(todoRepository)
//        println("todoItemを表示-------")
//        println(todoItem)
//        println("todoItem.isFinishedを表示-------${todoItem!!.isFinished}")
//
//        return todoItem.isFinished
//    }


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

//        この書き方の時はdata classにする必要がある
//        val todoItems = items.map{
//            TodoItem(
//                PK = it["PK"]?.s() ?: "",
//                text = it["text"]?.s() ?: ""
//            )
//        }

        println("resultItems-------$resultItems")
        return resultItems
    }

    //⭐️POST METHOD------------------------

    @PostMapping("/todo")
    fun addNewItem(@RequestBody todo: TodoRequest) {
        //追加したいアイテム
        val item = mapOf(
            //ランダムなUUIDをPKに入れる、この場合は.toString()が必要
            "PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
            "text" to AttributeValue.fromS(todo.text)
        )
        //DBと接続
        val client = DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()
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
        println(items)
    }

}