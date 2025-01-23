package com.example.todoapp

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest


import java.net.URI
import java.util.*

//class Todo {
//	var id: String = ""
//	var text: String = ""
//}

@SpringBootTest
@AutoConfigureMockMvc
class TodoappApplicationTests {

	private val client = DynamoDbClient.builder()
		.endpointOverride(URI.create("http://localhost:4566"))
		.credentialsProvider(AnonymousCredentialsProvider.create())
		.region(Region.AP_NORTHEAST_1)
		.build()

	fun deleteAllItems(tableName: String){

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

	fun scanAllItems(tableName: String): List<Map<String, AttributeValue>>{
		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val response = client.scan(request)
		val items = response.items().toList()
		return items
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

//	@Test
//	fun contextLoads() {
//	}


	@Test
	fun testForTest() {
		assertThat(1+2, equalTo(3))
	}

	@Test
	fun `エンドポイントtodoにPOSTすると 200 OK が返る`(){
		mockMvc.perform(post("/todo").content("{\"text\":\"bar\"}")
			.contentType(MediaType.APPLICATION_JSON)
		)
			.andExpect(status().isOk)
	}

	@Test
	fun `todoエンドポイントに foo をPOSTするとテーブルに foo が追加される`() {
		// Setup
		//以下のコードでDBテストする場合はbuild.gradle.ktsにimplementationする必要あり
		deleteAllItems("test")

		// Action
		mockMvc.perform(post("/todo").content("{\"text\":\"bar\"}")
			.contentType(MediaType.APPLICATION_JSON)
		)

		// Check
		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val afterResponse = client.scan(request)
		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(1))
	}

	@Test
	fun `todoエンドポイントに bar をPOSTするとテーブルに bar が追加される`() {
		// Setup
		//以下のコードでDBテストする場合はbuild.gradle.ktsにimplementationする必要あり
		deleteAllItems("test")

		// Action
		mockMvc.perform(post("/todo").content("{\"text\":\"bar\"}")
			.contentType(MediaType.APPLICATION_JSON)
		)
		// Check
		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val afterResponse = client.scan(request)
		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(1))
	}

	@Test
	fun `二つPOSTしたときに それぞれ違うIDで登録させれている`(){
		//Setup
		deleteAllItems("test")

		// Action
		mockMvc.perform(post("/todo").content("{\"text\":\"bar\"}")
			.contentType(MediaType.APPLICATION_JSON)
		)
		mockMvc.perform(post("/todo").content("{\"text\":\"foo\"}")
			.contentType(MediaType.APPLICATION_JSON)
		)
		//Check
		val allItems = scanAllItems("test")
		assertThat(allItems.size, equalTo(2))
	}

	@Test
	fun `todoエンドポイントに GET をリクエストして全てのテーブル情報を返す`(){
		//Setup
		deleteAllItems("test")

		val item = mapOf(
			"PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
			"text" to AttributeValue.fromS("12345")
		)
		val putItemRequest = PutItemRequest.builder()
			.tableName("test")
			.item(item)
			.build()
		client.putItem(putItemRequest)

		mockMvc.perform(get("/todo"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].text").value("12345"))
	}

//	@Test
//	fun `id のパスパラメータを渡すと id にあてはまるアイテムの完了状態が返る`() {
//		val result = mockMvc.perform(get("/todo/{id}", "12345"))
//			.andExpect(status().isOk)
//			//データの特定の値を確認する
//			//.andExpect(content().string("false"))
//			//Booleanで値を取得する方法↓
////			.andExpect(jsonPath("$.isFinished").value(false))
//			.andReturn()
//			result.response.contentAsString
//			println("response-------${result.response.contentAsString}")
//	}
}
