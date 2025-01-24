package com.example.todoapp

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
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

@SpringBootTest
@AutoConfigureMockMvc
class TodoappApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	//DB接続
	private val client = DynamoDbClient.builder()
		.endpointOverride(URI.create("http://localhost:4566"))
		.credentialsProvider(AnonymousCredentialsProvider.create())
		.region(Region.AP_NORTHEAST_1)
		.build()
	//全アイテム削除
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
	//全アイテム取得
	fun scanAllItems(tableName: String): List<Map<String, AttributeValue>>{
		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val response = client.scan(request)
		val items = response.items().toList()
		return items
	}

	//⭐️GET METHOD------------------------

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

	@Test
	fun `特定の PK のデータのみを取得する`(){
		//Setup
		deleteAllItems("test")
		//Action
		val PK1 = mockMvc.perform(post("/todo").content("{\"text\":\"１番目\"}")
			.contentType(MediaType.APPLICATION_JSON)
		).andReturn().response.contentAsString
		val PK2 = mockMvc.perform(post("/todo").content("{\"text\":\"２番目\"}")
			.contentType(MediaType.APPLICATION_JSON)
		).andReturn().response.contentAsString
		//Check

		mockMvc.perform(get("/todo/$PK1"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.text").value("１番目"))

	}

	@Test
	fun `PKが存在しない時に NOT FOUND を返す`(){
		deleteAllItems("test")

		mockMvc.perform(get("/todo/1234567890"))
		.andExpect(status().isNotFound)
	}

	//⭐️POST METHOD-----------------------

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
	fun `POSTしたときに 登録したID を返す`() {
		//Setup
		deleteAllItems("test")

		// Action
		val result = mockMvc.perform(
			post("/todo").content("{\"text\":\"bar\"}")
				.contentType(MediaType.APPLICATION_JSON)
		)
			.andReturn()
		val PK = result.response.contentAsString

		mockMvc.perform(get("/todo"))
			.andExpect(status().isOk)
			//TODO：!!!なぜか返り値が小文字のpkになるの調べる
			.andExpect(jsonPath("$[0].pk").value(PK))

	}



	//⭐️PUT METHOD------------------------



	//⭐️DELETE METHOD---------------------

	@Test
	fun `特定のPKのデータを DELETE する`(){
//		setup
		deleteAllItems("test")
		val id1 = mockMvc.perform(post("/todo")
			.content("{\"text\": \"１番目\"}")
			.contentType(MediaType.APPLICATION_JSON)
			).andReturn().response.contentAsString

		val id2 = mockMvc.perform(post("/todo")
			.content("{\"text\": \"２番目\"}")
			.contentType(MediaType.APPLICATION_JSON)
			).andReturn().response.contentAsString
//		action
		mockMvc.perform(delete("/todo/$id1", id1))
//		check
		val allTodoItems = mockMvc.perform(get("/todo"))
			.andReturn().response.contentAsString
		println("all----------$allTodoItems")
		mockMvc.perform(get("/todo/$id1"))
			.andExpect(status().isNotFound)
		mockMvc.perform(get("/todo/$id2"))
			.andExpect(status().isOk)

		// check by Mr.yusuke
//		val result = mockMvc.perform(get("/todo"))
//			.andReturn()
//		val content = result.response.contentAsString
//		val mapper = ObjectMapper()
//		val items = mapper.readValue<List<TodoItem>>(content)
//		assertThat(items.find { it.id == id1 }, equalTo(null))
//		assertThat(items.find { it.id == id2 }, not(equalTo(null)))

	}



}
